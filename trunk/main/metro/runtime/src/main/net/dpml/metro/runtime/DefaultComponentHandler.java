/* 
 * Copyright 2005-2006 Stephen J. McConnell.
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
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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

import net.dpml.component.Controller;
import net.dpml.component.ActivationPolicy;
import net.dpml.component.Disposable;
import net.dpml.component.ControlException;
import net.dpml.component.Component;
import net.dpml.component.Provider;
import net.dpml.component.Service;
import net.dpml.component.ModelListener;
import net.dpml.component.ModelEvent;

import net.dpml.lang.Version;

import net.dpml.metro.ComponentModel;
import net.dpml.metro.ComponentHandler;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.Priority;

import net.dpml.state.State;

import net.dpml.util.Logger;
import net.dpml.util.EventQueue;


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
 * {@link Provider} will be deloyed on activation of the handler otherwise the component 
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
   Provider instance = handler.getProvider(); // instance controller
   Object value = instance.getValue( true ); // service instance
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 * @see LifestylePolicy
 * @see CollectionPolicy
 * @see ActivationPolicy
 * @see ComponentModel
 * @see Provider
 */
class DefaultComponentHandler extends UnicastEventSource 
  implements Component, ComponentHandler, Disposable, ModelListener
{
    //--------------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------------

    private final Provider m_parent;
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
    private final PropertyChangeSupport m_support;
    private final Map m_cache = new Hashtable(); // context overloading entry/value cache
    private final Map m_map = new Hashtable(); // symbolic value map
    private final boolean m_flag; // locally managed
    
    //--------------------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------------------
    
    private boolean m_active = false;
    
    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------
    
    DefaultComponentHandler( 
      final EventQueue queue, 
      final Provider parent, final ClassLoader classloader, final Logger logger, 
      final ComponentController control, final ComponentModel model, boolean flag )
      throws RemoteException, ControlException
    {
        super( queue, logger );
        
        m_parent = parent;
        
        m_classloader = classloader;
        m_controller = control;
        m_model = model;
        m_path = model.getContextPath();
        m_flag = flag;
        
        String classname = model.getImplementationClassName();
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "creating new component handler [" + m_path + "] for class [" + classname + "]" );
        }
        
        m_support = new PropertyChangeSupport( this );
        model.addModelListener( this );
        
        try
        {
            m_class = control.loadComponentClass( classloader, classname );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load component class: "
              + classname;
            throw new ControllerException( error, e );
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
            throw new ControllerException( error, e );
        }
        
        m_graph = m_type.getStateGraph();
        
        try
        {
            m_services = control.loadServices( this );
        }
        catch( ControlException e )
        {
            final String error = 
              "Unable to load a service class declared in component type: "
              + classname;
            throw new ControllerException( error, e );
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
            throw new ControllerException( error, e );
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
            CollectionPolicy collection = model.getCollectionPolicy();
            m_holder = new SingletonHolder( collection );
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
            if( m_type.getInfo().isThreadSafe() )
            {
                CollectionPolicy collection = model.getCollectionPolicy();
                m_holder = new SingletonHolder( collection );
            }
            else
            {
                m_holder = new ThreadHolder();
            }
        }
        
        if( getLogger().isDebugEnabled() )
        {
            String lifestyleName = m_holder.getName();
            getLogger().debug( 
              "established " 
              + lifestyleName
              + " lifestyle handler for [" 
              + classname 
              + "]" );
        }
    }

   /**
    * Return the logging channel assigned to the event source.
    * @return the logging channel
    */
    Logger getLogger()
    {
        return super.getLogger();
    }
    
    //--------------------------------------------------------------------------
    // ComponentContext
    //--------------------------------------------------------------------------
    
   /**
    * Return the current controller.
    * @return the root system controller
    */
    public Controller getController()
    {
        return m_controller.getCompositionController();
    }
    
    //--------------------------------------------------------------------------
    // ModelListener
    //--------------------------------------------------------------------------
    
   /**
    * Notification from the component model of a change to the model.
    * @param event the model change event
    */
    public void modelChanged( ModelEvent event )
    {
        String feature = event.getFeature();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        m_support.firePropertyChange( feature, oldValue, newValue );
    }
    
    //--------------------------------------------------------------------------
    // ComponentHandler
    //--------------------------------------------------------------------------
    
   /**
    * Return a <tt>Provider</tt>. The value returned will be a function 
    * of the lifestyle policy implemented by the component.
    * 
    * @return the <tt>Provider</tt>
    * @exception InvocationTargetException if the request triggers the construction
    *   of a new provider instance and the provider raises an error during creation
    *   or activation
    * @exception ControlException if a control related error occurs
    */
    public Provider getProvider() throws InvocationTargetException, IOException
    {
        commission();
        return m_holder.getProvider();
    }
    
   /**
    * Return the component model assigned to the handler.
    * @return the component model
    */
    public ComponentModel getComponentModel()
    {
        return m_model;
    }
    
   /**
    * Return a mutible context map.
    *
    * @return the context map
    */
    public Map getContextMap()
    {
        return m_cache;
    }
    
   /**
    * Return a mutible context map.
    *
    * @return the context map
    */
    public Map getSymbolMap()
    {
        return m_map;
    }

    //--------------------------------------------------------------------------
    // Component
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
        return m_holder.getProviderCount();
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
    * Get the activation policy.  If the activation policy is STARTUP, an implementation
    * a handler shall immidiately activation a runtime instance.  If the policy is on DEMAND
    * an implementation shall defer activiation until an explicit request is received.  If 
    * the policy if SYSTEM activation may occur at the discretion of an implementation.
    *
    * @return the activation policy
    * @exception RemoteException if a remote exception occurs
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    public ActivationPolicy getActivationPolicy() throws RemoteException
    {
        return m_model.getActivationPolicy();
    }
    
    //--------------------------------------------------------------------------
    // Commissionable
    //--------------------------------------------------------------------------
    
   /**
    * Activate the component handler.  If the component declares an activate on 
    * startup policy then a new instance will be created and activated.
    *
    * @exception ControlException if an activation error occurs
    * @exception InvocationTargetException if the client component raises an error
    */
    public synchronized void commission() throws ControlException, InvocationTargetException
    {
        if( isActive() )
        {
            return;
        }

        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "commissioning" );
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
                m_holder.getProvider().getValue( false );
            }
            m_active = true;
        }
        catch( RemoteException e )
        {
            getLogger().warn( "activation failed due to a remote exception", e );
            decommission();
            final String error = 
              "Remote exception raised while attempting to access component activation policy.";
            throw new ControllerException( error, e );
        }
        catch( ControlException e )
        {
            getLogger().warn( "activation failed due to a control exception", e );
            decommission();
            throw e;
        }
        catch( InvocationTargetException e )
        {
            getLogger().warn( "activation failed due to a client initated invocation exception", e );
            decommission();
            throw e;
        }
        catch( Throwable e )
        {
            getLogger().warn( "activation failed due to a unexpected exception", e );
            decommission();
        }
        finally
        {
            if( !m_active )
            {
                getLogger().warn( "activation failed" );
                decommission();
            }
        }
    }
    
   /**
    * Deactivate the component.
    */
    public synchronized void decommission()
    {
        if( !isActive() )
        {
            return;
        }
        
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "decommissioning" );
        }
        
        //
        // deactivate all of the subsidiary components
        //
        
        m_holder.decommission();
        m_active = false;
        getLogger().debug( "decommissioned" );
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
    public void processEvent( EventObject event )
    {
        // TODO
    }
    
    //--------------------------------------------------------------------------
    // DefaultComponentHandler
    //--------------------------------------------------------------------------
    
   /**
    * Return the array of services provider by the handler.
    * @return the service array
    */
    public Service[] getServices()
    {
        return m_services;
    }
    
    ComponentController getComponentController()
    {
        return m_controller;
    }
    
   /**
    * Add a property change listener.  This method is used by a provider
    * to register a component implement property change listener. The component
    * handler is responsible for the propergation of context change events
    * to the registered listeners.
    * @param listener the propery change listener
    */
    void addPropertyChangeListener( PropertyChangeListener listener )
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "adding property change listener: [" + listener + "]" );
        }
        m_support.addPropertyChangeListener( listener );
    }
    
   /**
    * Remove a property change listener.
    * @param listener the propery change listener
    */
    void removePropertyChangeListener( PropertyChangeListener listener )
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "removing property change listener: [" + listener + "]" );
        }
        m_support.removePropertyChangeListener( listener );
    }
    
   /**
    * Dispose of the component handler.  
    */
    public void dispose()
    {
        synchronized( getLock() )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "initiating disposal" );
            }
            decommission();
            try
            {
                m_model.removeModelListener( this );
            }
            catch( RemoteException e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "ignoring model listener removal remote error", e );
                }
            }
            if( m_flag )
            {
                if( m_model instanceof Disposable )
                {
                    Disposable disposable = (Disposable) m_model;
                    disposable.dispose();
                }
            }
            super.dispose();
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "disposal complete" );
            }
        }
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
        
    Provider getParentProvider()
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

    protected void finalize() throws Throwable
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "finalizing component handler" );
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
    private DefaultProvider createDefaultProvider() 
      throws InvocationTargetException, IOException
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "creating new provider" );
        }
        try
        {
            EventQueue queue = getEventQueue();
            return new DefaultProvider( queue, this, getLogger() );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to create provider due to a remote exception.";
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
        * Return an <tt>DefaultProvider</tt> taking into account the component 
        * lifestyle policy.
        * @return the <tt>Provider</tt> manager
        * @exception ControlException of a controller error occurs
        * @exception InvocationTargetException if a client implementation error occurs
        */
        abstract DefaultProvider getProvider() throws IOException, InvocationTargetException;
       
       /**
        * Return the number of instances handled by the holder.
        * @return the instance count
        */
        abstract int getProviderCount();
        
       /**
        * Dispose of the holder and all managed instances.
        */
        void decommission()
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
        
       /**
        * Return the holder lifestyle policy name.
        * @return the policy value as a string
        */
        abstract String getName();
    }
    
   /**
    * Singleton holder class.  The singleton holder mains a single 
    * <tt>Provider</tt> of a component relative to the component model 
    * identity within the scope of the controller.  References to the 
    * singleton instance will be shared across mutliple threads.
    */
    private class SingletonHolder extends Holder
    {
        private final CollectionPolicy m_collection;
        private Reference m_reference;
        
       /**
        * Creation of a new singleton holder.
        */
        SingletonHolder( CollectionPolicy policy )
        {
            if( policy.equals( CollectionPolicy.SYSTEM ) )
            {
                if( null == m_parent ) 
                {
                    if( getLogger().isTraceEnabled() )
                    {
                        getLogger().trace( "assigning hard (system) collection policy" );
                    }
                    m_collection = CollectionPolicy.HARD;
                }
                else
                {
                    if( getLogger().isTraceEnabled() )
                    {
                        getLogger().trace( "assigning soft (system) collection policy" );
                    }
                    m_collection = CollectionPolicy.SOFT;
                }
            }
            else
            {
                if( getLogger().isDebugEnabled() )
                {
                    String name = policy.getName();
                    getLogger().debug( "assigning " + name + " collection policy" );
                }
                m_collection = policy;
            }

            m_reference = createReference( null );
        }
        
        DefaultProvider getProvider() throws IOException, InvocationTargetException
        {
            DefaultProvider provider = (DefaultProvider) m_reference.get();
            if( null == provider )
            {
                provider = createDefaultProvider();
                m_reference = createReference( provider );
                return provider;
            }
            else
            {
                return provider;
            }
        }
        
        void decommission()
        {
            if( !isDisposed() )
            {
                DefaultProvider provider = (DefaultProvider) m_reference.get();
                if( provider != null )
                {
                    provider.dispose();
                }
                m_reference.clear();
                super.decommission();
            }
        }
        
        int getProviderCount()
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
        
        String getName()
        {
            return "singleton";
        }
        
       /**
        * Constructs a reference that reflects the component cololection policy.
        * @param object the initial reference value
        * @return the reference
        */
        private Reference createReference( Object object )
        {
            if( m_collection.equals( CollectionPolicy.SOFT ) )
            {
                return new SoftReference( object );
            }
            else if( m_collection.equals( CollectionPolicy.WEAK ) )
            {
                return new WeakReference( object );
            }
            else if( m_collection.equals( CollectionPolicy.HARD ) )
            {
                return new HardReference( object );
            }
            else
            {
                final String error = 
                  "Supplied collection policy is abstract: " + m_collection;
                throw new IllegalArgumentException( error );
            }
        }
    }
    
   /**
    * Transient holder class.  The transient holder provides support for 
    * the transient lifestyle ensuing the creation of a new <tt>Provider</tt>
    * per request.
    */
    private class TransientHolder extends Holder
    {
        private final WeakHashMap m_providers = new WeakHashMap(); // transients
        
        DefaultProvider getProvider() throws IOException, InvocationTargetException
        {
            DefaultProvider provider = createDefaultProvider();
            m_providers.put( provider, null );
            return provider;
        }
        
        void decommission()
        {
            if( !isDisposed() )
            {
                DefaultProvider[] providers = getAllProviders();
                for( int i=0; i<providers.length; i++ )
                {
                    DefaultProvider provider = providers[i];
                    m_providers.remove( provider );
                    provider.dispose();
                }
                super.decommission();
            }
        }
        
        int getProviderCount()
        {
            return m_providers.size();
        }
        
        String getName()
        {
            return "transient";
        }
        
        private DefaultProvider[] getAllProviders()
        {
            return (DefaultProvider[]) m_providers.keySet().toArray( new DefaultProvider[0] );
        }
    }

   /**
    * The ThreadHolder class provides support for the per-thread lifestyle
    * policy within which new <tt>Provider</tt> creation is based on a single
    * <tt>Provider</tt> per thread.
    */
    private class ThreadHolder extends Holder
    {
        private final ThreadLocalHolder m_threadLocalHolder = new ThreadLocalHolder();
        
        DefaultProvider getProvider() throws IOException, InvocationTargetException
        {
            return (DefaultProvider) m_threadLocalHolder.get();
        }
        
        void decommission()
        {
            if( !isDisposed() )
            {
                m_threadLocalHolder.decommission();
                super.decommission();
            }
        }
        
        int getProviderCount()
        {
            return m_threadLocalHolder.getProviderCount();
        }

        String getName()
        {
            return "per-thread";
        }
        
        private DefaultProvider[] getAllProviders()
        {
            return m_threadLocalHolder.getAllProviders();
        }
    }

   /**
    * Internal thread local holder for the per-thread lifestyle holder.
    */
    private class ThreadLocalHolder extends ThreadLocal
    {
        private final WeakHashMap m_providers = new WeakHashMap(); // per thread instances
        
        protected Object initialValue()
        {
            try
            {
                DefaultProvider provider = createDefaultProvider();
                m_providers.put( provider, null );
                return provider;
            }
            catch( Exception e )
            {
                final String error = 
                  "Per-thread lifestyle policy handler encountered an error "
                  + "while attempting to establish provider.";
                throw new ControllerRuntimeException( error, e );
            }
        }
        
        int getProviderCount()
        {
            return m_providers.size();
        }
        
        DefaultProvider[] getAllProviders()
        {
            return (DefaultProvider[]) m_providers.keySet().toArray( new DefaultProvider[0] );
        }
        
        void decommission()
        {
            DefaultProvider[] providers = getAllProviders();
            for( int i=0; i<providers.length; i++ )
            {
                DefaultProvider provider = providers[i];
                provider.dispose();
                m_providers.remove( provider );
            }
        }
    }
    
   /**
    * A reference class that implements hard reference semantics.
    */
    private static class HardReference extends SoftReference
    {
        private final Object m_referent; // hard reference
        
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
