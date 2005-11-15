/* 
 * Copyright 2005 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.composition.engine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Map;
import java.util.Hashtable;
import java.util.WeakHashMap;

import net.dpml.component.info.Type;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.info.CollectionPolicy;
import net.dpml.component.info.ServiceDescriptor;
import net.dpml.component.model.ComponentModel;
import net.dpml.component.control.Disposable;

import net.dpml.logging.Logger;

import net.dpml.part.ActivationPolicy;
import net.dpml.part.Handler;
import net.dpml.part.HandlerException;
import net.dpml.part.HandlerRuntimeException;
import net.dpml.part.ControlException;
import net.dpml.part.ControlRuntimeException;
import net.dpml.part.Instance;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.state.impl.DefaultStateMachine;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;

/**
 * <p>Runtime handler for a component.  The component handler maintains an internal 
 * map of all instances derived from the component model based on the LifestylePolicy
 * declared by the model.  If the lifestyle policy is <tt>SINGLETON</tt> 
 * a single instance is shared between all concurrent requests.  If the policy is 
 * <tt>TRANSIENT</tt> (the default) a new instance is created per request. 
 * If the policy is <tt>THREAD</tt> and single instance is created per 
 * thread. In all cases, the lifetime of a supplied instance is a function of the collection 
 * policy declared by the component model.  For <tt>SINGLETON</tt> models the collection 
 * policies of <tt>HARD</tt>, <tt>SOFT</tt> and <tt>WEAK</tt> are rigorously respected.  
 * Components employing a <tt>THREAD</tt> lifestle policy are will be referenced under a 
 * weak reference to a thread local valiable containing a <tt>HARD</tt>, <tt>SOFT</tt> or 
 * <tt>WEAK</tt> reference to the component instance. For transient lifestyles, the 
 * implementation employs a WeakHashMap irrespective of the declared collection policy in 
 * order to avoid potential memory leaks arrising from non-disposal of consumed instances. 
 * If a component model declares an activation policy of <tt>STARTUP</tt> a new 
 * {@link Instance} will be deloyed on activation of the handler otherwise the component 
 * will be deloyed on <tt>DEMAND</tt> in response to a service request.</p>
 * 
 * <p><image src="doc-files/composition-handler-uml.png" border="0"/></p>
 *
 * <p>A component handler is created using a part handler and component model. The 
 * following example demonstrates the creation of a component model using a part-based
 * deployment template which in-turn is supplied as an argument when creating a new 
 * component handler.  Separation of context and handler creation enables the creation 
 * and management of a component model in a separate JVM from the runtime handler and 
 * centralization of shared context information across multiple handlers.</p>
 * <pre>
   PartHandler controller = Part.DEFAULT_HANDLER;
   Part part = controller.loadPart( url );
   Context context = controller.createContext( part ); // management info
   Handler handler = controller.createHandler( context ); // runtime control for the type
   handler.activate();
   Instance instance = handler.getInstance(); // runtime control for the instance
   Object value = instance.getValue( true );
 * </pre>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @see LifestylePolicy
 * @see CollectionPolicy
 * @see ActivationPolicy
 * @see ComponentModel
 * @see Instance
 */
public class ComponentHandler extends UnicastEventSource implements Handler, Disposable
{
    //--------------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final ComponentController m_controller;
    private final ComponentModel m_model;
    private final State m_graph;
    private final ClassLoader m_classloader;
    private final Class m_class;
    private final Type m_type;
    private final Class[] m_services;
    private final String m_path;
    private final URI m_uri;
    private final Holder m_holder;
    
    private final Map m_map = new Hashtable(); // symbolic value map
    private final Map m_cache = new Hashtable(); // context entry/value cache
    private final Map m_handlers = new Hashtable(); // part handlers
    
    //--------------------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------------------

    private boolean m_active = false;
    
    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------
    
    ComponentHandler( 
      final ClassLoader classloader, final Logger logger, 
      final ComponentController control, final ComponentModel model )
      throws RemoteException
    {
        super();
        
        m_classloader = classloader;
        m_logger = logger;
        m_controller = control;
        m_model = model;
        m_path = model.getContextPath();
        m_graph = model.getStateGraph();
        
        String classname = model.getImplementationClassName();
        try
        {
            m_class = control.loadComponentClass( classloader, classname );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load component class: "
              + classname;
            throw new HandlerRuntimeException( error, e );
        }
        
        try
        {
            m_type = control.loadType( m_class );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load component type: "
              + classname;
            throw new HandlerRuntimeException( error, e );
        }
        
        try
        {
            m_services = control.loadServiceClasses( this );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load a service class declared in component type: "
              + classname;
            throw new HandlerRuntimeException( error, e );
        }
        
        try
        {
            m_uri = new URI( "component:" + m_path );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to construct the component uri using the path [" 
              + m_path + "]";
            throw new ControlRuntimeException( error, e );
        }
        
        String name = model.getName();
        File work = control.getWorkDirectory( this );
        File temp = control.getTempDirectory( this );
        m_map.put( "name", name );
        m_map.put( "path", m_path );
        m_map.put( "work", work.toString() );
        m_map.put( "temp", temp.toString() );
        m_map.put( "uri", m_uri.toASCIIString() );
        
        LifestylePolicy lifestyle = model.getLifestylePolicy();
        if( lifestyle.equals( LifestylePolicy.SINGLETON ) )
        {
            m_holder = new SingletonHolder();
        }
        else if( lifestyle.equals( LifestylePolicy.TRANSIENT ) )
        {
            m_holder = new TransientHolder();
        }
        else if( lifestyle.equals( LifestylePolicy.THREAD ) )
        {
            m_holder = new ThreadHolder();
        }
        else
        {
            final String error = 
              "Unsuppported lifestyle policy: " + lifestyle;
            throw new UnsupportedOperationException( error );
        }
        
        // TODO: setup the classloader to be the PROTECTED loader
        
        String[] keys = model.getPartKeys();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            try
            {
                ComponentModel partModel = model.getComponentModel( key );
                Handler handler = control.createComponentHandler( classloader, partModel );
                m_handlers.put( key, handler );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Invalid part key ["
                  + key
                  + "] in component ["
                  + m_path
                  + "]";
                throw new ControlRuntimeException( error, e );
            }
        }
        
        getLogger().debug( "component controller [" + this + "] established" );
    }
    
    //--------------------------------------------------------------------------
    // Handler
    //--------------------------------------------------------------------------
    
   /**
    * Return the number of instances currently under management.  If the component
    * is a singleton the value returned will be between zero and 1 (depending on the 
    * activated status of the handler.  If the component is transient, the instance
    * count will reflect the number of instances currently referenced.
    *
    * @return the instance count.
    */
    public int size()
    {
        return m_holder.getInstanceCount();
    }
    
   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    */
    public boolean isActive()
    {
        return m_active;
    }

   /**
    * Activate the component handler.  If the component declares an activate on 
    * startup policy then a new instance will be created and activated.
    *
    * @exception Exception if an activation error occurs
    */
    public synchronized void activate() throws HandlerException, InvocationTargetException
    {
        if( isActive() )
        {
            return;
        }
        
        getLogger().debug( "initiating activation" );
        try
        {
            if( m_model.getActivationPolicy().equals( ActivationPolicy.STARTUP ) )
            {
                m_holder.getInstance();
                m_active = true;
            }
        }
        catch( RemoteException e )
        {
            deactivate();
            final String error = 
              "Remote exception raised while attempting to access component activation policy.";
            throw new HandlerException( error, e );
        }
    }
    
   /**
    * Deactivate the component.
    * @exception Exception if an activation error occurs
    */
    public synchronized void deactivate()
    {
        if( !isActive() )
        {
            return;
        }
        
        getLogger().debug( "initiating deactivation" );
        m_holder.deactivate();
        m_active = false;
    }
    
    public Instance getInstance() throws InvocationTargetException, HandlerException
    {
        if( isActive() )
        {
            return m_holder.getInstance();
        }
        else
        {
            final String error = 
              "Component handler ["
              + this
              + "] is not active.";
            throw new IllegalStateException( error );
        }
    }
    
    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

    protected void processEvent( EventObject event )
    {
    }
    
    //--------------------------------------------------------------------------
    // ComponentHandler
    //--------------------------------------------------------------------------
    
    public void dispose()
    {
        synchronized( getLock() )
        {
            m_holder.dispose();
            super.dispose();
        }
    }
    
    Handler getPartHandler( String key ) throws UnknownKeyException
    {
        Handler handler = (Handler) m_handlers.get( key );
        if( null == handler )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return handler;
        }
    }
    
    // this will be needed as soon as we sort parent child relationships
    
    //Handler getPartHandler( ServiceDescriptor service )
    //{
    //Handler[] handlers = (Handler[]) m_handlers.values().toArray( new Handler[0] );
    //for( int i=0; i<handlers.length; i++ )
    //{
    //    Handler handler = handlers[i];
    //    // need an interface here
    //}
    //}
    
    Object getContextValue( String key ) throws ControlException
    {
        return m_controller.getContextValue( this, key );
    }

    State getStateGraph()
    {
        return m_graph;
    }
    
    Class getImplementationClass()
    {
        return m_class;
    }
    
    ClassLoader getClassLoader()
    {
        return m_classloader;
    }
    
    Handler getParentHandler()
    {
        return null;
    }
    
   /**
    * Returns the component type.
    * @return the type descriptor
    */
    Type getType()
    {
        return m_type;
    }
    
    Map getContextMap()
    {
        return m_map;
    }
    
    String getPath()
    {
        return m_path;
    }
    
    ComponentModel getComponentModel()
    {
        return m_model;
    }
    
    Class[] getServiceClassArray()
    {
        return m_services;
    }
    
    Object createNewObject() throws ControlException, InvocationTargetException
    {
        return m_controller.createInstance( this );
    }
    
    //--------------------------------------------------------------------------
    // internal
    //--------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------

    public String toString()
    {
        try
        {
            return "component:" + m_path + " (" + m_model.getImplementationClassName() + ")";
        }
        catch( RemoteException e )
        {
            return "handler:" + getClass().getName();
        }
    }
    
    //--------------------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------------------

   /**
    * Apply best efforts to construct a fully validated activated instance holder.
    * @return the instance holder
    * @exception HandlerException if the construction of the instance was not successfull
    * @exception InvocationTargetException if a error was raised by the external implementation
    */
    private DefaultInstance createDefaultInstance() throws InvocationTargetException, HandlerException
    {
        try
        {
            return new DefaultInstance( this, getLogger() );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to create instance holder due to a remote exception.";
            throw new HandlerException( error, e );
        }
    }

    private abstract class Holder
    {
        abstract void deactivate();
        abstract DefaultInstance getInstance() throws HandlerException, InvocationTargetException;
        abstract int getInstanceCount();
        abstract void dispose();
    }
    
    private class SingletonHolder extends Holder
    {
        private Reference m_reference;
        
        SingletonHolder()
        {
            m_reference = createReference( null );
        }
        
        DefaultInstance getInstance() throws HandlerException, InvocationTargetException
        {
            if( m_reference == null )
            {
                throw new IllegalStateException( "disposed" );
            }
            
            DefaultInstance instance = (DefaultInstance) m_reference.get();
            if( null == instance )
            {
                instance = createDefaultInstance();
                m_reference = createReference( instance );
                return instance;
            }
            else
            {
                return instance;
            }
        }
        
        void deactivate()
        {
            DefaultInstance instance = (DefaultInstance) m_reference.get();
            if( instance != null )
            {
                instance.deactivate();
            }
        }
        
        void dispose()
        {
            deactivate();
            DefaultInstance instance = (DefaultInstance) m_reference.get();
            if( instance != null )
            {
                instance.dispose();
            }
            m_reference.clear();
        }
        
        int getInstanceCount()
        {
            if( null != m_reference.get() )
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }
    
    private class TransientHolder extends Holder
    {
        private WeakHashMap m_instances = new WeakHashMap(); // transients
        
        DefaultInstance getInstance() throws HandlerException, InvocationTargetException
        {
            DefaultInstance instance = createDefaultInstance();
            m_instances.put( instance, null );
            return instance;
        }
        
        void deactivate()
        {
            DefaultInstance[] instances = getAllInstances();
            for( int i=0; i<instances.length; i++ )
            {
                DefaultInstance instance = instances[i];
                instance.deactivate();
            }
        }
        
        void dispose()
        {
            deactivate();
            DefaultInstance[] instances = getAllInstances();
            for( int i=0; i<instances.length; i++ )
            {
                DefaultInstance instance = instances[i];
                m_instances.remove( instance );
                instance.dispose();
            }
        }
        
        int getInstanceCount()
        {
            return m_instances.size();
        }
        
        private DefaultInstance[] getAllInstances()
        {
            return (DefaultInstance[]) m_instances.keySet().toArray( new DefaultInstance[0] );
        }
    }

    private class ThreadHolder extends Holder
    {
        private ThreadLocalHolder m_threadLocalHolder = new ThreadLocalHolder();
        
        DefaultInstance getInstance() throws HandlerException, InvocationTargetException
        {
            return (DefaultInstance) m_threadLocalHolder.get();
        }
        
        void deactivate()
        {
            m_threadLocalHolder.deactivate();
        }
        
        void dispose()
        {
            m_threadLocalHolder.dispose();
        }
        
        int getInstanceCount()
        {
            return m_threadLocalHolder.getInstanceCount();
        }
        
        private DefaultInstance[] getAllInstances()
        {
            return m_threadLocalHolder.getAllInstances();
        }
    }

    private class ThreadLocalHolder extends ThreadLocal
    {
        private WeakHashMap m_instances = new WeakHashMap(); // per thread instances
        
        protected Object initialValue()
        {
            try
            {
                DefaultInstance instance = createDefaultInstance();
                m_instances.put( instance, null );
                return instance;
            }
            catch( Exception e )
            {
                final String error = 
                  "Per-thread lifestyle policy handler encountered an error while attempting to establish instance.";
                throw new HandlerRuntimeException( error, e );
            }
        }
        
        int getInstanceCount()
        {
            return m_instances.size();
        }
        
        DefaultInstance[] getAllInstances()
        {
            return (DefaultInstance[]) m_instances.keySet().toArray( new DefaultInstance[0] );
        }
        
        void deactivate()
        {
            DefaultInstance[] instances = getAllInstances();
            for( int i=0; i<instances.length; i++ )
            {
                DefaultInstance instance = instances[i];
                instance.deactivate();
            }
        }
        
        void dispose()
        {
            deactivate();
            DefaultInstance[] instances = getAllInstances();
            for( int i=0; i<instances.length; i++ )
            {
                DefaultInstance instance = instances[i];
                instance.dispose();
                m_instances.remove( instance );
            }
        }
    }
    
    private Reference createReference( Object object )
    {
        try
        {
            CollectionPolicy policy = m_model.getCollectionPolicy();
            if( policy.equals( CollectionPolicy.SYSTEM ) || policy.equals( CollectionPolicy.SOFT ) )
            {
                return new SoftReference( object );
            }
            else if( policy.equals( CollectionPolicy.WEAK ) )
            {
                return new WeakReference( object );
            }
            else
            {
                return new HardReference( object );
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Reference object creating failure due to a remote exception.";
            throw new HandlerRuntimeException( error, e );
        }
    }
    
    private static class HardReference extends SoftReference
    {
        private Object m_referent;
        
        public HardReference( Object referent )
        {
            super( referent );
            m_referent = referent;
        }
        
        public Object get()
        {
            return m_referent;
        }
    }
}
