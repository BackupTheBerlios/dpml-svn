/* 
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.composition.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.Vector;

import net.dpml.activity.Executable;
import net.dpml.activity.Startable;

import net.dpml.component.runtime.ResourceUnavailableException;
import net.dpml.component.control.LifecycleException;
import net.dpml.component.runtime.Consumer;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.Container;
import net.dpml.component.runtime.ClassLoadingContext;
import net.dpml.component.runtime.ComponentException;
import net.dpml.component.runtime.ComponentRuntimeException;
import net.dpml.component.runtime.TypeClassNotFoundException;
import net.dpml.component.runtime.ServiceClassNotFoundException;
import net.dpml.component.runtime.ComponentNotFoundException;
import net.dpml.component.runtime.AvailabilityEvent;
import net.dpml.component.runtime.AvailabilityListener;
import net.dpml.component.runtime.Service;
import net.dpml.component.runtime.Available;
import net.dpml.component.runtime.AvailabilityException;
import net.dpml.component.runtime.Manager;

import net.dpml.composition.control.CompositionController;

import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ContextDirective;
import net.dpml.component.info.ServiceDescriptor;
import net.dpml.component.info.Type;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.info.PartReference;
import net.dpml.component.info.EntryDescriptor;

import net.dpml.composition.event.EventProducer;
import net.dpml.composition.event.WeakEventProducer;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.Configurable;
import net.dpml.configuration.impl.DefaultConfiguration;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.Parameterizable;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.part.Control;
import net.dpml.part.DelegationException;
import net.dpml.part.Part;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.PartHandlerNotFoundException;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.impl.DefaultState;
import net.dpml.state.impl.DefaultStateMachine;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.component.info.ActivationPolicy;

/**
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public abstract class ComponentHandler extends WeakEventProducer 
  implements Container, Available, Manager, Consumer, 
  ClassLoadingContext, Configurable, Parameterizable
{
    private final Map m_proxies = new WeakHashMap();
    private final DependencyGraph m_dependencies = new DependencyGraph();

    private final Logger m_logger;
    private final Component m_parent;
    private final ComponentDirective m_profile;
    private final CompositionController m_controller;
    private final ClassLoader m_classloader;
    private final URI m_uri;
    private final ComponentController m_componentController;
    private final ContextMap m_context;
    private final State m_graph;
    private final StateMachine m_machine;
    private final PartsTable m_parts;
    private final LifestylePolicy m_lifestyle;

    private State m_state;
    private boolean m_initialized = false;
    private Configuration m_configuration;
    private Parameters m_parameters;

    private Type m_type;
    private Class m_class;
    private Class[] m_interfaces;

    private boolean m_disposed = false;
    private Object m_instance;

    public ComponentHandler( 
      Logger logger, CompositionController controller, ClassLoader classloader, 
      URI uri, ComponentDirective profile, Component parent ) 
      throws ComponentException, PartHandlerNotFoundException, DelegationException, RemoteException
    {
        super();

        m_logger = logger;
        m_controller = controller;
        m_classloader = classloader;
        m_profile = profile;
        m_uri = uri;

        m_parent = parent;
        m_componentController = controller.getComponentController();
        m_class = loadComponentClass( classloader, profile );
        
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try
        {
            m_type = Type.decode( getClass().getClassLoader(), m_class );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load component type: " + m_class.getName();
            throw new ComponentException( error, e );
        }
        
        m_lifestyle = profile.getLifestylePolicy();
        m_interfaces = loadServiceClasses( classloader, m_type );

        m_parts = new PartsTable( this );

        DependencyGraph graph = new DependencyGraph();
        m_dependencies.addChild( graph );
        m_context = new ContextMap( this, parent, graph );
        m_graph = resolveStateGraph( m_class );
        m_machine = new DefaultStateMachine( m_graph );
        m_configuration = m_profile.getConfiguration();
        m_parameters = m_profile.getParameters();

        //
        // Get the set of parts within this part and add them as nested models 
        // within this model.
        //

        PartReference[] parts = m_type.getPartReferences();
        for( int i=0; i<parts.length; i++ )
        {
            PartReference reference = parts[i];
            String key = reference.getKey();
            Part part = reference.getPart();
            addComponent( key, part );
        }

        //
        // Build the context model.  The initial population of the context 
        // model is establised by reading in the set of PartReference instances
        // contained within the ContextDirective.  Each part reference is located
        // using a context entry key.  The part reference holds the part 
        // that will be used to construct a context entry.  The context 
        // entry getValue() operation is used to resolve the 
        // context value accessible via the context key.
        //

        ContextDirective context = profile.getContextDirective();
        EntryDescriptor[] entries = m_type.getContextDescriptor().getEntryDescriptors();
        for( int i=0; i<entries.length; i++ )
        {
            EntryDescriptor entry = entries[i];
            final String key = entry.getKey();
            Part part = context.getPartDirective( key );
            if( null == part )
            {
                if( entry.isRequired() )
                {
                    final String error =
                      "Unresolved context entry."
                      + "\nComponent: " + getLocalURI()
                      + "\nContext Key: " + key;
                    throw new ComponentRuntimeException( error );
                }
            }
            else
            {
                getContextMap().addEntry( key, part );
            }
        }
    }

   /**
    * Get the activation policy for the control.
    *
    * @return the activation policy
    * @see ActivationPolicy#SYSTEM
    * @see ActivationPolicy#STARTUP
    * @see ActivationPolicy#DEMAND
    */
    public ActivationPolicy getActivationPolicy()
    {
        return m_profile.getActivationPolicy();
    }

   /**
    * Add a component to the collection of components managed by the container.
    *
    * @param key the key under which the component will be referenced
    * @param uri a part uri
    * @return the component
    */
    public Component addComponent( String key, URI uri ) 
      throws IOException, ComponentException, PartNotFoundException, 
      DelegationException, PartHandlerNotFoundException
    {
        Part part = getController().loadPart( uri );
        return addComponent( key, part );
    }

   /**
    * Add a component to the collection of components managed by the container.
    *
    * @param part a part
    * @param key the key under which the component will be referenced
    * @return the component
    */
    public Component addComponent( String key, Part part ) 
      throws ComponentException, DelegationException, PartHandlerNotFoundException, RemoteException
    {
        Component component = getPartsTable().addComponent( key, part );
        m_dependencies.add( component );
        return component;
    }

   /**
    * Return the set of components managed by this container.
    * @return an array of management components
    */
    public Component[] getComponents()
    {
        return getPartsTable().getComponents();
    }

   /**
    * Return the part that defines this component.
    * @return the component part definition
    */
    public Part getDefinition()
    { 
        return m_profile;
    }

   /**
    * Issue a request to the service to prepare for operations.
    * @exception AvailabilityException if the service cannot be made available
    */
    public void prepare() throws AvailabilityException
    {
        getComponentController().prepare( this );
    }

   /**
    * Return an array of service descriptors corresponding to 
    * the service contracts that the service publishes.
    * @return the service descriptor array
    */
    public ServiceDescriptor[] getDescriptors()
    {
        return m_type.getServiceDescriptors();
    }

   /**
    * Return the enclosing parent component.
    * @return the parent component or null if this is a top-level component
    */
    Component getParent()
    {
        return m_parent;
    }

   /**
    * Return the startup sequence for the set of components contained 
    * within the container.
    * @return the startup sequence
    */
    public Component[] getStartupSequence()
    {
        return m_dependencies.getStartupGraph();
    }

   /**
    * Return the shudown sequence for the set of components contained 
    * within the container.
    * @return the shutdown sequence
    */
    public Component[] getShutdownSequence()
    {
        return m_dependencies.getShutdownGraph();
    }

   /**
    * Return an array of components providing services to this component.
    * @return the provider component array
    */
    public Component[] getProviders()
    {
        return m_context.getProviders();
    }

   /**
    * Return the component assigned as provider for the supplied context key.
    * @param key the context key
    * @return the provider component
    */
    public Component getProvider( String key )
    {
        return m_context.getProvider( key );
    }

    public void setProvider( String key, Part part )
      throws ComponentException, PartHandlerNotFoundException, DelegationException, RemoteException
    {
        m_context.setProvider( key, part );
    }

   /**
    * TODO: Grabbing the first service as the API hook is not a very reliable way
    * of establishing the classloader for sibling components.  Instead we need a part
    * to explicitly declare the classname that represents the API anchor. 
    */
    public ClassLoader getClassLoader()
    {
        ServiceDescriptor[] services = getDescriptors();
        if( services.length > 0 )
        {
            ServiceDescriptor service = services[0]; // eeek!
            String classname = service.getClassname();
            try
            {
                Class c = m_classloader.loadClass( classname );
                return c.getClassLoader();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error - unable to load a service class declared by component."
                  + "\nComponent: " + getDeploymentClass().getName()
                  + "\nService Class: " + classname;
                throw new ComponentRuntimeException( error, e );
            }
        }
        return m_classloader;
    }

   /**
    * Return the short name of this component.
    * @return the component name
    */
    public String getName()
    {
        return m_profile.getName();
    }

    Type getType()
    {
        return m_type;
    }

    Class getDeploymentClass()
    {
        return m_class;
    }

    LifestylePolicy getLifestylePolicy()
    {
        return m_lifestyle;
    }

    CompositionController getController()
    {
        return m_controller;
    }

    public void configure( Configuration configuration )
    {
        m_configuration = configuration;
    }

    Configuration getConfiguration()
    {
        if( null == m_configuration )
        {
            return new DefaultConfiguration( "configuration", null );
        }
        else
        {
            return m_configuration;
        }
    }

    public void parameterize( Parameters parameters )
    {
        m_parameters = parameters;
    }

    Parameters getParameters()
    {
        if( null == m_parameters )
        {
            return DefaultParameters.EMPTY_PARAMETERS;
        }
        else
        {
            return m_parameters;
        }
    }

   /**
    * Return the availability status of the model.
    * @return the availability status
    */
    public boolean isOperational()
    {
        return true;
    }

    public void addAvailabilityListener( AvailabilityListener listener ) 
    {
        super.addListener( listener );
    }

    public void removeAvailabilityListener( AvailabilityListener listener )
    {
        super.removeListener( listener );
    }

    public void addStateListener( StateListener listener )
    {
        super.addListener( listener );
    }

    public void removeStateListener( StateListener listener )
    {
        super.removeListener( listener );
    }

    protected void processEvent( EventObject event )
    {
        if( event instanceof AvailabilityEvent )
        {
            AvailabilityEvent e = (AvailabilityEvent) event;
            EventListener[] listeners = listeners();
            for( int i=0; i<listeners.length; i++ )
            {
                EventListener listener = listeners[i];
                if( listener instanceof AvailabilityListener )
                {
                    AvailabilityListener availabilityListener = (AvailabilityListener) listener;
                    availabilityListener.availabilityChanged( e );
                }
            }
        }
        else if( event instanceof StateEvent )
        {
            StateEvent e = (StateEvent) event;
            EventListener[] listeners = listeners();
            for( int i=0; i<listeners.length; i++ )
            {
                EventListener listener = listeners[i];
                if( listener instanceof StateListener )
                {
                    StateListener stateListener = (StateListener) listener;
                    try
                    {
                        stateListener.stateChanged( e );
                    }
                    catch( Throwable t )
                    {
                        final String error =
                        "Listener raised an exception in response to a state change notification.";
                        m_logger.warn( error, t );
                    }
                }
            }
        }
        else
        {
            final String error =
              "Event type not supported."
              + "\nEvent Class: " + event.getClass().getName();
            m_logger.warn( error );
        }
    }

    public ComponentController getComponentController()
    {
        return m_componentController;
    }

   /**
    * Return an instance of the component type represented 
    * by the supplied model using the default proxy policy.
    * 
    * @return the resolved instance
    */
    public Object resolve() throws Exception
    {
        return getComponentController().resolve( this, true );
    }

   /**
    * Return an instance of the component type represented 
    * by the supplied model.
    * 
    * @param policy the proxy creation policy
    * @return the resolved instance
    */
    public Object resolve( boolean policy ) throws Exception
    {
        return getComponentController().resolve( this, policy );
    }

   /**
    * Initialize the component.  
    */
    public void initialize() throws Exception
    {
        m_componentController.initialize( this );
    }

   /**
    * Applies a state transition identified by a supplied transition key.
    *
    * @param key the key identifying the transition to apply to the component's controller
    * @return the state resulting from the transition
    * @exception if a transition error occurs
    */
    public State apply( String key ) throws Exception
    {
        return m_componentController.apply( this, key );
    }

   /**
    * Executes an operation identified by a supplied operation key.
    *
    * @param key the key identifying the operation to execute 
    * @exception if a transition error occurs
    */
    public void execute( String key ) throws Exception
    {
        m_componentController.execute( this, key );
    }

   /**
    * Termination of the component.
    */
    public void terminate()
    {
        getLogger().debug( "terminating" );
        m_componentController.terminate( this );
    }

    public boolean isInitialized()
    {
        return m_initialized;
    }

    public void setInitialized( boolean flag )
    {
        m_initialized = flag;
        AvailabilityEvent event = new AvailabilityEvent( this, flag );
        super.enqueueEvent( event );
    }

    public Object getLocalInstance()
    {
        return m_instance;
    }

    public void setLocalInstance( Object instance )
    {
        m_instance = instance;
    }

   /**
    * Return the current state. The current state is the function of 
    * the initialization and subsequent transition actions applied to 
    * the state model.  The current state established the active 
    * state chain as the sequence of states from the current state to 
    * root state.  Any transitions defined in the active state chain 
    * are candidates for execution.  Transitions that are logically closer to the
    * current state override transitions declare higher up in the 
    * state chain. This method is exposed via the ServiceManager 
    * interface to the controlling application (possibly the enclossing 
    * component or the model controller).
    *
    * @return the current state
    */
    public synchronized State getState()
    {
        return m_machine.getState();
    }

    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return a uri identifiying the instance.
    *
    * @return the instance uri
    */
    public URI getURI() throws RemoteException
    {
        return getLocalURI();
    }

    URI getLocalURI()
    {
        return m_uri;
    }

   /**
    * Create, register and return a proxy to the instance managed by this
    * holder.  The implementation will register the proxy as a weak reference
    * key in an internal table of proxies.  
    *
    * @return the proxy reflecting the service interfaces declared by the component type
    */
    public Object getProxy()
    {
        Appliance appliance = new Appliance( this );
        Object proxy = Proxy.newProxyInstance( m_classloader, m_interfaces, appliance );
        //m_proxies.put( proxy, this );
        return proxy;
    }

   /**
    * Return the implementation instance managed by this handler.
    * @return the implementation instance
    */
    public Object getValue()
    {
        if( false == isInitialized() )
        {
            String spec = getLocalURI().toString();
            throw new ResourceUnavailableException( spec );
        }
        else
        {
            return getLocalInstance();
        }
    }

   /**
    * Returns an map containing context values keyed by context entry key.
    *
    * @return the context map for the instance managed by this component
    */
    public ContextMap getContextMap()
    {
        return m_context;
    }

    public PartsTable getPartsTable()
    {
        return m_parts;
    }

   /**
    * Disposal sets the disposed flag on this holder to true and triggers 
    * etherialization of any proxy references that this holder has relative 
    * to the instance it is managing.  On completing the implementation will
    * remove the instance for the model's instance table and trigger instance
    * etherialization.
    */
    public void dispose()
    {
        if( m_disposed ) 
        {
            return;
        }

        try
        {
            //Object instance = getLocalInstance();
            m_componentController.terminate( this );
        }
        catch( IllegalStateException e )
        {
            // pass
        }

        //getCompositionModel().getComponentTable().remove( this );
        //synchronized( m_proxies )
        //{
        //    m_disposed = true;
        //    CompositionModel model = getCompositionModel();
        //    Object[] proxies = (Object[]) m_proxies.keySet().toArray( new Object[0] );
        //    for( int i=0; i<proxies.length; i++ )
        //    {
        //        Object proxy = proxies[i];
        //        m_componentController.release( proxy );
        //    }
        //    m_proxies.clear();
        //    getCompositionModel().getComponentTable().remove( this );
        //    try
        //    {
        //        Object instance = getLocalInstance();
        //        m_componentController.release( instance );
        //    }
        //    catch( IllegalStateException e )
        //    {
        //        // pass
        //    }
        //}
    }

   /**
    * Called by a proxy as a result of finalization or explicit disposal request.
    * If there are no further proxies to this instance then the implementation 
    * will invoke self dispose.
    * 
    * @param proxy the proxy to remove from the set of proxies referncing the
    *   instance handled by this holder
    * @see #dispose
    */
    public void release( Object proxy )
    {
        //synchronized( m_proxies )
        //{
        //    m_proxies.remove( proxy );
        //    if( m_proxies.isEmpty() )
        //    {
        //        //dispose();
        //        System.out.println( "NO PROXIES LEFT" );
        //    }
        //}
    }

    public String toString()
    {
        return getLocalURI().toString();
    }

   /**
    * Finalization of the holder automaticaly triggers self disposal.
    * @see #dispose
    */
    protected void finalize()
    {
        m_logger.debug( "component model finalization in " + getLocalURI() );
        dispose();
    }

    private State resolveStateGraph( Class subject )
    {
        if( Executable.class.isAssignableFrom( subject ) )
        {
            return loadState( Executable.class );
        }
        else if( Startable.class.isAssignableFrom( subject ) )
        {
            return loadState( Startable.class );
        }
        else
        {
            return loadState( subject );
        }
    }
    
    State loadState( Class subject )
    {
        String resource = subject.getName().replace( '.', '/' ) + ".xgraph";
        try
        {
            URL url = subject.getClassLoader().getResource( resource );
            if( null == url )
            {
                return new DefaultState( "" );
            }
            else
            {
                InputStream input = url.openConnection().getInputStream();
                return DefaultStateMachine.load( input );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to load component state graph resource [" 
              + resource 
              + "].";
            throw new ComponentRuntimeException( error, e );
        }
    }
    
   /**
    * Return the state-machine for this component.
    * @return the state-machine
    */
    public StateMachine getStateMachine()
    {
        return m_machine;
    }

    private Class loadComponentClass( ClassLoader classloader, ComponentDirective profile )
    {
        final String classname = profile.getClassname();
        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException cnfe )
        {
            throw new TypeClassNotFoundException( classname );
        }
        catch( NoClassDefFoundError e )
        {
            final String error = 
              "Cannot load component class due to a missing dependent class."
              + "\nDeployment Class: " + classname
              + "\nMissing Class: " + e.getMessage()
              + "\n\n" + classloader.toString();
            throw new ComponentRuntimeException( error, e );
        }
    }

    private Class[] loadServiceClasses( ClassLoader classloader, Type type )
    {
        ServiceDescriptor[] services = m_type.getServiceDescriptors();
        Class[] interfaces = new Class[ services.length ];
        for( int i=0; i<interfaces.length; i++ )
        {
            ServiceDescriptor service = services[i];
            interfaces[i] = loadServiceClass( classloader, service );
        }
        return interfaces;
    }

    private Class loadServiceClass( ClassLoader classloader, ServiceDescriptor service )
    {
        final String classname = service.getClassname();
        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException cnfe )
        {
            String type = m_class.getName();
            throw new ServiceClassNotFoundException( type, classname );
        }
    }
}
