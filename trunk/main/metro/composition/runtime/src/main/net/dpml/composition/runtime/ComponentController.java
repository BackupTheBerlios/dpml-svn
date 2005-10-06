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

package net.dpml.composition.runtime;

import java.util.Map;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Vector;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.Observable;

import javax.swing.event.ChangeEvent;

import net.dpml.logging.Logger;

import net.dpml.component.model.ComponentModel;

import net.dpml.component.runtime.Service;
import net.dpml.component.runtime.Available;
import net.dpml.component.runtime.AvailabilityException;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.Container;
import net.dpml.component.runtime.DuplicateKeyException;
import net.dpml.component.runtime.Manager;
import net.dpml.component.control.ControllerException;
import net.dpml.component.control.ControllerRuntimeException;
import net.dpml.component.control.Disposable;

import net.dpml.composition.control.CompositionController;

import net.dpml.state.Operation;
import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.Transition;
import net.dpml.state.UnknownTransitionException;
import net.dpml.state.UnknownOperationException;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;

/**
 * The ComponentController class is a controller of a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentController extends LoggingHandler
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Map m_handlers = new Hashtable();
    private final LifecycleHandler m_lifecycleHandler;

    private final URI m_uri;
    private final CompositionController m_controller;
    private final Logger m_logger;

    //--------------------------------------------------------------------------
    // contructor
    //--------------------------------------------------------------------------

    public ComponentController( Logger logger, CompositionController controller )
    {
        m_controller = controller;
        m_logger = logger;

        m_lifecycleHandler = new LifecycleHandler( logger, controller );
        m_uri = controller.getURI();
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    public URI getURI()
    {
        return m_uri;
    }

    public String getName()
    {
        return getClass().getName();
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    public Object resolve( ComponentHandler component ) throws Exception
    {
        return resolve( component, true );
    }

    public Object resolve( ComponentHandler component, boolean policy ) throws Exception
    {
        if( component instanceof ComponentHandler )
        {
            ComponentHandler handler = (ComponentHandler) component;
            if( policy )
            {
                return handler.getProxy();
            }
            else
            {
                if( false == handler.isInitialized() )
                {
                    initialize( handler );
                }
                return getInstance( handler );
            }
        }
        else
        {
            final String error = 
              "Unsupported component implementation class."
              + "\nComponent: " + component.getLocalURI()
              + "\nClass: " + component.getClass().getName()
              + "\nMethod: resolve/2";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Release a proxy reference to a component.
    * 
    * @param proxy the proxy to release
    */
    public void release( Object proxy )
    {
        if( null == proxy )
        {
            return;
        }
        if( Proxy.isProxyClass( proxy.getClass() ) )
        {
            Object handler = Proxy.getInvocationHandler( proxy );
            if( handler instanceof Disposable )
            {
                Disposable disposable = (Disposable) handler;
                disposable.dispose();
            }
        }
    }

   /**
    * Issue a request to the service to prepare for operations.
    * @exception AvailabilityException if the service cannot be made available
    */
    public void prepare( ComponentHandler component ) throws AvailabilityException
    {
        try
        {
            initialize( component );
        }
        catch( Throwable e )
        {
            final String error = 
              "Componet could not be brought to an available state."
              + "\nComponent URI: " + component.getLocalURI();
            throw new AvailabilityException( error, e );
        }
    }

   /**
    * Initialization of the manager by a controller.
    * If the root state is not terminal the implementation will invoke a 
    * transiton named "initalize".  If the transition results in a modified 
    * state, the implementation will continue to recursivly invoke 
    * initialize operations until a non-initializing state is established 
    * as the current state.
    *
    * @param component the component handler
    * @exception IllegalStateException if an error occurs during validation 
    * @exception Exception if an error is raised by a handler assigned to 
    *  and invoked initialization transition
    */
    public synchronized void initialize( ComponentHandler component ) throws Exception
    {
        if( component.isInitialized() )
        {
            return;
        }
        getLogger().debug( "initialization of " + component.getLocalURI() );
        if( component instanceof Container )
        {
            Container container = (Container) component;
            Component[] providers = container.getStartupSequence();
            if( providers.length > 0 )
            {
                getLogger().info( "startup sequence (" + providers.length + ")" );
            }
            for( int i=0; i<providers.length; i++ )
            {
                Component provider = providers[i];
                getLogger().info( " (" + i + ") " + providers[i] );
                Available available = (Available) provider;
                try
                {
                    getLogger().debug( "preparing service" + provider.getURI() );
                    available.prepare();
                }
                catch( AvailabilityException e )
                {
                    URI uri = getURI();
                    final String error = 
                      "Failed to initialize component due to non-availability of a dependent service."
                      + "\nComponent: " + component.getLocalURI()
                      + "\nService Provider: " + provider.getURI();
                    throw new ControllerException( uri, error, e );
                }
            }
        }

        Object instance = getInstance( component );
        List visited = new LinkedList();
        Class subject = component.getDeploymentClass();
        StateMachine machine = component.getStateMachine();
        machine.initialize( instance );
        component.setInitialized( true );
    }

    private Object getInstance( ComponentHandler component )
    {
        Object instance = component.getLocalInstance();
        if( null == instance)
        {
            try
            {
                instance = m_lifecycleHandler.incarnate( component );
                component.setLocalInstance( instance );
            }
            catch( Exception e )
            {
                URI uri = getURI();
                final String error = 
                  "Unable to establish a component instance."
                  + "\nComponent: " + component.getLocalURI()
                  + "\nClass: " + component.getDeploymentClass();
                throw new ControllerRuntimeException( uri, error, e );
            }
        }
        return instance;
    }
    
   /**
    * Apply a transition identified by a supplied transition key.  The 
    * implementation will attempt to locate the transition relative to the current
    * state (taking into account the State search semantics) from which
    * a invocation handler is resolve and invoked.  If the transition is successful
    * the target transition state declared by the transition will be assigned as the 
    * current current state and all listeners will be notified of a state change.
    * If the transition resolved from the supplied key references a target state 
    * that is the same as the current state the transition will not be invoked and
    * the method will return false.
    * 
    * @param key the transition key
    * @return the state established by the transition
    * @exception if a error occurs in transition execution
    */
    public State apply( ComponentHandler component, String key ) throws Exception
    {
        if( false == component.isInitialized() )
        {
            initialize( component );
        }
        StateMachine machine = component.getStateMachine();
        Object instance = getInstance( component );
        machine.apply( key, instance );
        return machine.getState();
    }

   /**
    * Execute an operation identified by a supplied operation key.  The 
    * implementation will attempt to locate the operation relative to the current
    * state (taking into account the State search semantics) from which
    * a invocation handler is resolve and invoked.
    * 
    * @param key the operation key
    * @exception if a error occurs in transition execution
    */
    public void execute( ComponentHandler component, String key ) throws Exception
    {
        if( false == component.isInitialized() )
        {
            initialize( component );
        }
        StateMachine machine = component.getStateMachine();
        Object instance = getInstance( component );
        machine.execute( key, instance );
    }

    private java.util.logging.Logger getJavaLoggerForURI( URI uri )
    {
        String path = uri.getSchemeSpecificPart();
        if( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        path.replace( '/', '.' );
        return java.util.logging.Logger.getLogger( path );
    }

   /**
    * Handles a request for termination by a controller.  The implementation 
    * will attempt to locate a transition named 'terminate' and if located, 
    * will apply that transition.  If the result of the transition if a new
    * non-terminal state the procedure will be repeated.  If the current state
    * is a terminal state the operation simply returns.
    */
    public synchronized void terminate( ComponentHandler component )
    {
        if( false == component.isInitialized() )
        {
            return;
        }
        Service[] shutdown = component.getShutdownSequence();
        for( int i=0; i<shutdown.length; i++ )
        {
            Service provider = shutdown[i];
            if( provider instanceof Manager )
            {
                Manager manager = (Manager) provider;
                try
                {
                    manager.terminate();
                }
                catch( Throwable e )
                {
                    URI uri = getURI();
                    final String error = 
                      "Failed to terminate a subsidiary part."
                      + "\nContainer: " + component.getLocalURI()
                      + "\nComponent: " + getRemoteComponentURI( manager );
                    getLogger().warn( error, e );
                }
            }
        }
        StateMachine machine = component.getStateMachine();
        Object instance = getInstance( component );
        machine.terminate( instance );
    }

    private URI getRemoteComponentURI( Component component )
    {
        try
        {
            return component.getURI();
        }
        catch( RemoteException e )
        {
            return null;
        }
    }
}
