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

package net.dpml.metro.runtime;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.Map;
import java.util.Hashtable;
import java.util.WeakHashMap;
import java.util.logging.Level;

import net.dpml.metro.info.Type;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.Priority;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.model.ComponentModel;

import net.dpml.logging.Logger;

import net.dpml.part.ActivationPolicy;
import net.dpml.part.Component;
import net.dpml.part.ControlException;
import net.dpml.part.Disposable;
import net.dpml.part.Instance;
import net.dpml.part.Service;
import net.dpml.part.ServiceNotFoundException;
import net.dpml.part.Version;
import net.dpml.part.Context;

import net.dpml.state.State;

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
   Controller controller = Controller.STANDARD; // system controller
   Directive part = controller.loadDirective( uri );
   Context context = controller.createContext( part ); // management info
   Component handler = controller.createComponent( context ); // runtime controller
   handler.activate();
   Instance instance = handler.getInstance(); // instance controller
   Object value = instance.getValue( true ); // service instance
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 * @see LifestylePolicy
 * @see CollectionPolicy
 * @see ActivationPolicy
 * @see ComponentModel
 * @see Instance
 */
public class ComponentHandler extends UnicastEventSource implements Component, Context, Disposable
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
    private final DefaultService[] m_services;
    private final String m_path;
    private final URI m_uri;
    private final Holder m_holder;
    private final Component m_parent;
    
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
      final Component parent, final ClassLoader classloader, final Logger logger, 
      final ComponentController control, final ComponentModel model )
      throws RemoteException
    {
        super();
        
        m_parent = parent;
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
            throw new ControllerRuntimeException( error, e );
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
            throw new ControllerRuntimeException( error, e );
        }
        
        try
        {
            m_services = control.loadServices( this );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load a service class declared in component type: "
              + classname;
            throw new ControllerRuntimeException( error, e );
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
            throw new ControllerRuntimeException( error, e );
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
        
        // At this point the component handler is fully established with respect to 
        // its own logic as a simple component. Before completing initialization we 
        // need to establish all of the component parts that are children of this 
        // component.
        
        String[] keys = m_model.getPartKeys();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            try
            {
                ComponentModel context = model.getComponentModel( key );
                Component handler = control.createComponentHandler( this, classloader, context );
                m_handlers.put( key, handler );
            }
            catch( UnknownKeyException e )
            {
                final String error = 
                  "Invalid part key ["
                  + key
                  + "] in component ["
                  + m_path
                  + "]";
                throw new ControllerRuntimeException( error, e );
            }
            catch( Exception e )
            {
                final String error = 
                  "Internal error while atrempting to create a subsidiary part ["
                  + key
                  + "] in component ["
                  + m_path
                  + "]";
                throw new ControllerRuntimeException( error, e );
            }
        }
        
        getLogger().debug( "component controller [" + this + "] established" );
    }
    
    //--------------------------------------------------------------------------
    // Context
    //--------------------------------------------------------------------------
    
   /**
    * Return a mutible context map.
    *
    * @return the context map
    */
    public Map getContextMap()
    {
        return m_cache;
    }
    
    //--------------------------------------------------------------------------
    // Component
    //--------------------------------------------------------------------------
    
   /**
    * Return a mutible context map.
    *
    * @return the context map
    */
    public Map getSymbolMap()
    {
        return m_map;
    }
    
   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service definition
    * @return a component matching the serivce definiton
    * @exception ServiceNotFoundException if no component found
    * @exception RemoteException if a remote exception occurs
    */
    public Component lookup( Service service ) throws ServiceNotFoundException, RemoteException
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( 
              "lookup  in [" 
              + this 
              + "] for [" 
              + service.getServiceClass().getName() 
              + "]." );
        }
        Component[] components = (Component[]) m_handlers.values().toArray( new Component[0] );
        for( int i=0; i<components.length; i++ )
        {
            Component component = components[i];
            if( component.isaCandidate( service ) )
            {
                return component;
            }
        }
        if( m_parent != null )
        {
            return m_parent.lookup( service );
        }
        else
        {
            String classname = service.getServiceClass().getName();
            throw new ServiceNotFoundException( CompositionController.CONTROLLER_URI, classname );
        }
    }
    
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
    * @exception ControlException if an activation error occurs
    * @exception InvocationTargetException if the client component raises an error
    */
    public synchronized void activate() throws ControlException, InvocationTargetException
    {
        if( isActive() )
        {
            return;
        }
        
        // setup and subsidiary logging channels declared by the component type
        
        try
        {
            String path = m_controller.getPathForLogger( this );
            CategoryDirective[] categories = m_model.getCategoryDirectives();
            for( int i=0; i<categories.length; i++ )
            {
                CategoryDirective category = categories[i];
                String spec = path + "." + category.getName();
                java.util.logging.Logger log = java.util.logging.Logger.getLogger( spec );
                Priority priority = category.getPriority();
                if( null != priority )
                {
                    if( Priority.ERROR.equals( priority ) )
                    {
                        log.setLevel( Level.SEVERE );
                    }
                    else if( Priority.WARN.equals( priority ) )
                    {
                        log.setLevel( Level.WARNING );
                    }
                    else if( Priority.INFO.equals( priority ) )
                    {
                        log.setLevel( Level.INFO );
                    }
                    else if( Priority.DEBUG.equals( priority ) )
                    {
                        log.setLevel( Level.FINE );
                    }
                }
                
                // TODO: set category target
                
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception raised while attempting to access component logging categories.";
            throw new ControllerException( error, e );
        }
        
        try
        {
            if( m_model.getActivationPolicy().equals( ActivationPolicy.STARTUP ) )
            {
                getLogger().debug( "activating" );
                m_holder.getInstance();
            }
            
            //
            // activate the children
            //
            
            Component[] components = (Component[]) m_handlers.values().toArray( new Component[0] );
            for( int i=0; i<components.length; i++ )
            {
                Component component = components[i];
                component.activate();
            }
            
            m_active = true;
        }
        catch( RemoteException e )
        {
            deactivate();
            final String error = 
              "Remote exception raised while attempting to access component activation policy.";
            throw new ControllerException( error, e );
        }
        finally
        {
            if( !m_active )
            {
                getLogger().warn( "activation failed" );
                deactivate();
            }
        }
    }
    
   /**
    * Deactivate the component.
    */
    public synchronized void deactivate()
    {
        if( !isActive() )
        {
            return;
        }
        
        getLogger().debug( "deactivating" );
        
        //
        // dispose of all of the instances managed by this component
        //
        
        m_holder.dispose();
        
        //
        // deactivate all of the subsidiary components
        //
        
        try
        {
            Component[] components = (Component[]) m_handlers.values().toArray( new Component[0] );
            for( int i=0; i<components.length; i++ )
            {
                Component component = components[i];
                try
                {
                    component.deactivate();
                }
                catch( RemoteException e )
                {
                    final String message = 
                      "Ignoring remote exception raised during deactivation.";
                    getLogger().warn( message, e );
                }
            }
        }
        finally
        {
            m_active = false;
        }
    }
    
   /**
    * Return an <tt>Instance</tt> holder. The value returned will be a function 
    * of the lifestyle policy implemented by the component.
    * 
    * @return the <tt>Instance</tt> manager
    * @exception InvocationTargetException if the request triggers the construction
    *   of a new provider instance and the provider raises an error during creation
    *   or activation
    * @exception ControlException if a control related error occurs
    */
    public Instance getInstance() throws InvocationTargetException, ControlException
    {
        if( isActive() )
        {
            return m_holder.getInstance();
        }
        else
        {
            activate();
            return m_holder.getInstance();
            //final String error = 
            //  "Component handler ["
            //  + this
            //  + "] is not active.";
            //throw new ControllerException( error );
        }
    }
    
   /**
    * Return the array of services provider by the handler.
    * @return the service array
    */
    public Service[] getServices()
    {
        return m_services;
    }

   /**
    * Return true if this handler is a candidate for the supplied service definition.
    * @param service the service definition
    * @return true if this is a candidate
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isaCandidate( Service service ) throws RemoteException
    {
        Class clazz = service.getServiceClass();
        Version version = service.getVersion();
        DefaultService[] services = m_services;
        for( int i=0; i<services.length; i++ )
        {
            DefaultService s = services[i];
            Version v = s.getVersion();
            Class c = s.getServiceClass();
            if( v.complies( version ) && clazz.isAssignableFrom( c ) )
            {
                return true;
            }
        }
        return false;
    }
    
    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

   /**
    * Process the supplied event.
    * @param event the event object
    */
    protected void processEvent( EventObject event )
    {
    }
    
    //--------------------------------------------------------------------------
    // ComponentHandler
    //--------------------------------------------------------------------------
    
   /**
    * Dispose of the component handler.  
    */
    public void dispose()
    {
        synchronized( getLock() )
        {
            m_holder.dispose();
            super.dispose();
        }
    }
    
   /**
    * Return a subsidiary component.
    * @param key the subsidiary component key
    * @return the subsidiary component
    * @exception UnknownKeyException if the key does not match 
    *   any of the internal components managed by this component
    */
    Component getPartHandler( String key ) throws UnknownKeyException
    {
        Component handler = (Component) m_handlers.get( key );
        if( null == handler )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return handler;
        }
    }
    
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
    
    Component getParentHandler()
    {
        return m_parent;
    }
    
   /**
    * Returns the component type.
    * @return the type descriptor
    */
    Type getType()
    {
        return m_type;
    }
    
    String getPath()
    {
        return m_path;
    }
    
    ComponentModel getComponentModel()
    {
        return m_model;
    }
    
    Object createNewObject() throws ControlException, InvocationTargetException
    {
        return m_controller.createInstance( this );
    }
    
    Class[] getServiceClassArray()
    {
        Class[] classes = new Class[ m_services.length ];
        for( int i=0; i<classes.length; i++ )
        {
            classes[i] = m_services[i].getServiceClass();
        }
        return classes;
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

   /**
    * Return a string representation of this component.
    * @return the string value
    */
    public String toString()
    {
        try
        {
            return "component:" + m_path + " (" + m_model.getImplementationClassName() + ")";
        }
        catch( RemoteException e )
        {
            return "component:" + getClass().getName();
        }
    }
    
    //--------------------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------------------

   /**
    * Apply best efforts to construct a fully validated activated instance holder.
    * @return the instance holder
    * @exception ControlException if the construction of the instance was not successfull
    * @exception InvocationTargetException if a error was raised by the external implementation
    */
    private DefaultInstance createDefaultInstance() 
      throws InvocationTargetException, ControlException
    {
        try
        {
            return new DefaultInstance( this, getLogger() );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to create instance holder due to a remote exception.";
            throw new ControllerException( error, e );
        }
    }

   /**
    * Abstract holder class that serves as the base class for holders dealing
    * with variouse lifestyle policies.
    */
    private abstract class Holder
    {
        private boolean m_disposed = false;
        
       /**
        * Return an <tt>I(nstance</tt> taking into account the component 
        * lifestyle policy.
        * @return the <tt>Instance</tt> manager
        * @exception ControlException of a controller error occurs
        * @exception InvocationTargetException if a client implementation error occurs
        */
        abstract DefaultInstance getInstance() throws ControlException, InvocationTargetException;
       
       /**
        * Return the number of instances handled by the holder.
        * @return the instance count
        */
        abstract int getInstanceCount();
        
       /**
        * Dispose of the holder and all managed instances.
        */
        void dispose()
        {
            if( isDisposed() )
            {
                return;
            }
            else
            {
                m_disposed = true;
            }
        }
        
       /**
        * Return the disposed state of the holder.
        */
        boolean isDisposed()
        {
            return m_disposed;
        }
    }
    
   /**
    * Singleton holder class.  The singleton holder mains a single 
    * <tt>Instance</tt> of a component relative to the component model 
    * identity within the scope of the controller.  References to the 
    * singleton instance will be shared across mutliple threads.
    */
    private class SingletonHolder extends Holder
    {
        private Reference m_reference;
        
       /**
        * Creation of a new singleton holder.
        */
        SingletonHolder()
        {
            m_reference = createReference( null );
        }
        
        DefaultInstance getInstance() throws ControlException, InvocationTargetException
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
        
        void dispose()
        {
            if( !isDisposed() )
            {
                DefaultInstance instance = (DefaultInstance) m_reference.get();
                if( instance != null )
                {
                    instance.dispose();
                }
                m_reference.clear();
                super.dispose();
            }
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
    
   /**
    * Transient holder class.  The transient holder provides support for 
    * the transient lifestyle ensuing the creation of a new <tt>Instance</tt>
    * per request.
    */
    private class TransientHolder extends Holder
    {
        private WeakHashMap m_instances = new WeakHashMap(); // transients
        
        DefaultInstance getInstance() throws ControlException, InvocationTargetException
        {
            DefaultInstance instance = createDefaultInstance();
            m_instances.put( instance, null );
            return instance;
        }
        
        void dispose()
        {
            if( !isDisposed() )
            {
                DefaultInstance[] instances = getAllInstances();
                for( int i=0; i<instances.length; i++ )
                {
                    DefaultInstance instance = instances[i];
                    m_instances.remove( instance );
                    instance.dispose();
                }
                super.dispose();
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

   /**
    * The ThreadHolder class provides support for the per-thread lifestyle
    * policy within which new <tt>Instance</tt> creation is based on a single
    * <tt>Instance</tt> per thread.
    */
    private class ThreadHolder extends Holder
    {
        private ThreadLocalHolder m_threadLocalHolder = new ThreadLocalHolder();
        
        DefaultInstance getInstance() throws ControlException, InvocationTargetException
        {
            return (DefaultInstance) m_threadLocalHolder.get();
        }
        
        void dispose()
        {
            if( !isDisposed() )
            {
                m_threadLocalHolder.dispose();
                super.dispose();
            }
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

   /**
    * Internal thread local holder for the per-thread lifestyle holder.
    */
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
                throw new ControllerRuntimeException( error, e );
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
        
        void dispose()
        {
            DefaultInstance[] instances = getAllInstances();
            for( int i=0; i<instances.length; i++ )
            {
                DefaultInstance instance = instances[i];
                instance.dispose();
                m_instances.remove( instance );
            }
        }
    }
    
   /**
    * Constructs a reference that reflects the component cololection policy.
    * @param object the initial reference value
    * @return the reference
    */
    private Reference createReference( Object object )
    {
        //
        // if this is a top-level component then set the collection policy
        // to hard otherwise we'll loose the instance because nothing is 
        // referencing it directly
        //
        
        try
        {
            CollectionPolicy policy = m_model.getCollectionPolicy();
            
            //
            // if an explicit collection policy is defined then apply it now
            // otherwise use SOFT collection as the SYSTEM default
            //
            
            if( policy.equals( CollectionPolicy.SYSTEM ) )
            {
                if( null == m_parent ) 
                {
                    return new HardReference( object );
                }
                else
                {
                    return new SoftReference( object );
                }
            }
            else if( policy.equals( CollectionPolicy.SOFT ) )
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
            throw new ControllerRuntimeException( error, e );
        }
    }
    
   /**
    * A reference class that implements hard reference semantics.
    */
    private static class HardReference extends SoftReference
    {
        private Object m_referent;
       
       /**
        * Creation of a new hard reference.
        * @param referent the referenced object
        */
        public HardReference( Object referent )
        {
            super( referent );
            m_referent = referent;
        }
        
       /**
        * Return the referent.
        * @return the referent object
        */
        public Object get()
        {
            return m_referent;
        }
    }
}
