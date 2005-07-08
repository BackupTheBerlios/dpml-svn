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

import net.dpml.composition.control.CompositionController;

import net.dpml.part.control.ControlException;
import net.dpml.part.control.ControllerRuntimeException;
import net.dpml.part.control.Disposable;
import net.dpml.part.control.Component;
import net.dpml.part.control.Container;
import net.dpml.part.control.DuplicateKeyException;
import net.dpml.part.state.State;
import net.dpml.part.state.Transition;
import net.dpml.part.state.ResourceUnavailableException;
import net.dpml.part.state.NoSuchTransitionException;
import net.dpml.part.state.NoSuchOperationException;
import net.dpml.part.state.ValidationException;
import net.dpml.part.state.Operation;
import net.dpml.part.state.StateEvent;
import net.dpml.part.state.StateListener;
import net.dpml.part.state.NoSuchHandlerException;
import net.dpml.part.state.RecursiveInitializationException;
import net.dpml.part.state.RecursiveTerminationException;

/**
 * The ComponentController class is a controller of a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentController extends LoggingHandler implements Manager
{
    private final Map m_handlers = new Hashtable();
    private final LifecycleHandler m_lifecycleHandler;

    private final URI m_uri;
    private final CompositionController m_controller;
    private final Logger m_logger;

    public ComponentController( Logger logger, CompositionController controller )
    {
        m_controller = controller;
        m_logger = logger;

        m_lifecycleHandler = new LifecycleHandler( logger, controller );
        m_uri = controller.getURI();
    }

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

    public Object resolve( Component component, boolean policy ) throws Exception
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
              + "\nComponent: " + component.getURI()
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
    * Initialization of the manager by a controller.
    * If the root state is not terminal the implementation will invoke a 
    * transiton named "initalize".  If the transition results in a modified 
    * state, the implementation will continue to recursivly invoke 
    * initialize operations until a non-initializing state is established 
    * as the current state.
    *
    * @return the initialized object
    * @exception IllegalStateException if an error occurs during validation 
    * @exception Exception if an error is raised by a handler assigned to 
    *  and invoked initialization transition
    */
    public void initialize( Component component ) throws Exception
    {
        if( component instanceof ComponentHandler )
        {
            ComponentHandler entry = (ComponentHandler) component;
            initializeComponent( entry );
        }
        else
        {
            final String error = 
              "Unsupported component implementation class."
              + "\nComponent: " + component.getURI()
              + "\nClass: " + component.getClass().getName()
              + "\nMethod: initialize/1";
            throw new IllegalArgumentException( error );
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
    * @return the initialized object
    * @exception IllegalStateException if an error occurs during validation 
    * @exception Exception if an error is raised by a handler assigned to 
    *  and invoked initialization transition
    */
    private synchronized void initializeComponent( ComponentHandler component ) throws Exception
    {
        if( component.isInitialized() )
        {
            return;
        }

        getLogger().debug( "initialization of " + component.getURI() );
        if( component instanceof Container )
        {
            Container container = (Container) component;

            Component[] parts = container.getStartupSequence();
            for( int i=0; i<parts.length; i++ )
            {
                Component part = parts[i];
                try
                {
                    getLogger().debug( "initializing part" + part.getURI() );
                    part.initialize();
                }
                catch( Throwable e )
                {
                    URI uri = getURI();
                    final String error = 
                      "Failed to initialize component due to a part initaliation failure."
                      + "\nContainer: " + component.getURI()
                      + "\nComponent: " + part.getURI();
                    throw new ControlException( uri, error, e );
                }
            }
        }

        Object instance = getInstance( component );
        List visited = new LinkedList();
        State graph = component.getStateGraph();
        Class subject = component.getDeploymentClass();
        validate( subject, graph );
        boolean flag = true;
        while( flag  )
        {
            State state = component.getCurrentState();
            Transition initialization = state.getInitialization();
            if( null == initialization )
            {
                flag = false;
            }
            else if( false == visited.contains( state.getName() ) )
            {
                visited.add( state.getName() );
                flag = applyTransition( component, initialization );
            }
            else
            {
                boolean first = true;
                StringBuffer buffer = new StringBuffer();
                buffer.append( "Initialization sequence aborted." );
                buffer.append( "\nReason: detection of a recursive initialization sequence" );
                Iterator iterator = visited.iterator();
                while( iterator.hasNext() )
                {
                     String name = (String) iterator.next();
                     if( first )
                     {
                         buffer.append( "\n  State [" + name + "]" );
                         first = false;
                     }
                     else
                     {
                         buffer.append( " --> [" + name + "]" );
                     }
                }
                buffer.append( " --> [" + state.getName() + "]" );
                String error = buffer.toString();
                visited.clear();
                throw new RecursiveInitializationException( error );
            }
        }
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
                  + "\nComponent: " + component.getURI()
                  + "\nClass: " + component.getDeploymentClass();
                throw new ControllerRuntimeException( uri, error, e );
            }
        }
        return instance;
    }

    private void validate( Class subject, State state ) throws ValidationException
    {
        if( null == state )
        {
            throw new NullPointerException( "state" );
        }
        Transition init = state.getInitialization();
        if( null != init )
        {
            URI uri = init.getHandlerURI();
            try
            {
                validateHandlerURI( subject, uri );
            }
            catch( ValidationException e )
            {
                final String error = 
                  "Initialization handler uri validation failure."
                  + "\nState: " + state.getName()
                  + "\nInitialization Handler URI: " + uri
                  + "\nMessage: " + e.getMessage();
                throw new ValidationException( error, e.getCause() );
            }
        }
        String[] operations = state.getLocalOperationNames();
        for( int i=0; i<operations.length; i++ )
        {
            String key = operations[i];
            Operation operation = state.getNamedOperation( key );
            URI uri = operation.getHandlerURI();
            try
            {
                validateHandlerURI( subject, uri );
            }
            catch( ValidationException e )
            {
                final String error = 
                  "Operation handler uri validation failure."
                  + "\nState: " + state.getName()
                  + "\nOperation Key: " + key
                  + "\nOperation Handler URI: " + uri
                  + "\nMessage: " + e.getMessage();
                throw new ValidationException( error, e.getCause() );
            }
        }
        String[] transitions = state.getLocalTransitionNames();
        for( int i=0; i<transitions.length; i++ )
        {
            String key = transitions[i];
            Transition transition = state.getNamedTransition( key );
            URI uri = transition.getHandlerURI();
            try
            {
                validateHandlerURI( subject, uri );
            }
            catch( ValidationException e )
            {
                final String error = 
                  "Operation handler uri validation failure."
                  + "\nState: " + state.getName()
                  + "\nOperation Key: " + key
                  + "\nOperation Handler URI: " + uri
                  + "\nMessage: " + e.getMessage();
                throw new ValidationException( error, e.getCause() );
            }
        }
        Transition terminator = state.getTerminator();
        if( null != terminator )
        {
            URI uri = terminator.getHandlerURI();
            try
            {
                validateHandlerURI( subject, uri );
            }
            catch( ValidationException e )
            {
                final String error = 
                  "Terminator handler uri validation failure."
                  + "\nState: " + state.getName()
                  + "\nTerminator Handler URI: " + uri
                  + "\nMessage: " + e.getMessage();
                throw new ValidationException( error );
            }
        }
    }

    private void validateHandlerURI( Class c, URI uri ) throws ValidationException
    {
        if( null == uri )
        {
            return;
        }
        String scheme = uri.getScheme();
        String spec = uri.getSchemeSpecificPart();
        
        if( "method".equals( scheme ) )
        {
            if( "null".equals( spec ) )
            {
                return;
            }
            else
            {
                Method method = locateMethod( c, spec );
            }
        }
        else
        {
            final String error = 
              "handler scheme '" 
              + scheme 
              + "' not supported";
            throw new ValidationException( error );
        }
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
    public State apply( Component component, String key ) throws Exception
    {
        if( component instanceof ComponentHandler )
        {
            ComponentHandler entry = (ComponentHandler) component;
            if( false == entry.isInitialized() )
            {
                initialize( entry );
            }
            Transition transition = getNamedTransition( entry, key );
            applyTransition( entry, transition );
            return component.getState();
        }
        else
        {
            final String error = 
              "Unsupported component implementation class."
              + "\nComponent: " + component.getURI()
              + "\nClass: " + component.getClass().getName()
              + "\nMethod: apply/2";
            throw new IllegalArgumentException( error );
        }
    }

    private Transition getNamedTransition( ComponentHandler component, String key )
    {
        State state = component.getState();
        try
        {
            return state.getNamedTransition( key );
        }
        catch( NoSuchTransitionException e )
        {
            String classname = component.getDeploymentClass().getName();
            final String error = 
              "Invalid transition key [" 
              + key 
              + "] in component [" 
              + classname 
              + "/" 
              + state.getName() 
              + "]"
              + "\nState: " + state.list();
            throw new IllegalArgumentException( error ); 
        }
    }

   /**
    * Apply a transition identified by a supplied transition key.  The 
    * implementation will attempt to locate the transition relative to the current
    * state (taking into account the State search semantics) from which
    * a invocation handler is resolve and invoked.  If the transition is successful
    * the target transition state declared by the transtion will be assigned as the 
    * current current state and all listeners will be notified of a state change.
    * If the transition resolved from the supplied key references a target state 
    * that is the same as the current state the transition will not be invoked and
    * the method will return false.
    * 
    * @param key the transition key
    * @return TRUE if the transition was executed
    * @exception if a error occurs in transition execution
    */
    private boolean applyTransition( ComponentHandler entry, Transition transition ) throws Exception
    {
        if( null == entry )
        {
            throw new NullPointerException( "entry" );
        }
        if( null == transition )
        {
            throw new NullPointerException( "transition" );
        }

        getInstance( entry );
        State state = entry.getCurrentState();
        URI handler = transition.getHandlerURI();
        State target = transition.getTransitionTarget();

        if( null != handler )
        {
            if( getLogger().isDebugEnabled() )
            {
                final String message = 
                  "applying ["
                  + handler
                  + "] to state [" 
                  + state.getName()
                  + "]";
                getLogger().debug( message );
            }
            execution( entry, handler, state, target );
        }

        if( state != target )
        {
            entry.setState( target );
            return true;
        }
        else
        {
            return false;
        }
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
    public void execute( Component component, String key ) throws Exception
    {
        if( component instanceof ComponentHandler )
        {
            ComponentHandler entry = (ComponentHandler) component;
            if( false == entry.isInitialized() )
            {
                initialize( entry );
            }
            Operation operation = getNamedOperation( entry, key );
            URI handler = operation.getHandlerURI();
            if( null == handler )
            {
                return;
            }
            else
            {
                if( getLogger().isDebugEnabled() )
                {
                    final String message = 
                      "executing operation ["
                      + handler.toString()
                      + "]";
                    getLogger().debug( message );
                }
                State state = entry.getCurrentState();
                execution( entry, handler, state, null );
            }
        }
        else
        {
            final String error = 
              "Unsupported component implementation class."
              + "\nComponent: " + component.getURI()
              + "\nClass: " + component.getClass().getName()
              + "\nMethod: execute/2";
            throw new IllegalArgumentException( error );
        }
    }

    private Operation getNamedOperation( ComponentHandler component, String key )
    {
        State state = component.getState();
        try
        {
            return state.getNamedOperation( key );
        }
        catch( NoSuchOperationException e )
        {
            String classname = component.getDeploymentClass().getName();
            final String error = 
              "Invalid operation key [" 
              + key 
              + "] in component [" 
              + classname 
              + "/" 
              + state.getName() 
              + "]"
              + "\nState: " + state.list();
            throw new IllegalArgumentException( error ); 
        }
    }
   
   /**
    * Internal utility method that handles the resolution of a handler based on 
    * the object assigned to the Transition handler attribute.  If the 
    * handler is an instance of URI, the following schemes will be evaluated:
    *
    * <ol>
    *   <li>part:[key] - invocation of the handle method on the component's part
    *       referenced by the scheme specific part of the supplied uri</li>
    *   <li>handler:[key] - invocation of the handle method on a class identified by 
    *       the scheme specific part of the uri</li>
    *   <li>method:[key] - invocation of the handle method against the component
    *       instance</li>
    * </ol>
    * 
    * @param handler the object describing the handler
    * @param state the current state
    * @param target the target of the transition 
    * @excetion Exception of an invocation or handler error occurs
    */
    private void execution( ComponentHandler entry, URI uri, State state, State target ) throws Exception
    {
        if( null == uri )
        {
            return;
        }

        String scheme = uri.getScheme();

        if( "method".equals( scheme ) )
        {
            //
            // invoke the named method on the component instance
            //

            String spec = uri.getSchemeSpecificPart();
            if( "null".equals( spec ) )
            {
                return;
            }
            else
            {
                Object instance = getInstance( entry );
                Class c = instance.getClass(); 
                Method method = locateMethod( c, spec );
                Object[] args = null;
                try
                {
                    Class[] parameters = method.getParameterTypes();
                    args = resolveArguments( entry, parameters, state, target );
                }
                catch( Throwable e )
                {
                    URI controller = getURI();
                    final String error = 
                      "Unable to apply transition due to an error while resolving transition method parameters."
                      + "\nComponent: " + entry.getURI()
                      + "\nTransition URI: " + uri
                      + "\nCurrent state: " + state
                      + "\nTarget state: " + target;
                    throw new ControlException( controller, error, e );
                }

                try
                {
                    method.invoke( instance, args );
                }
                catch( InvocationTargetException e )
                {
                    URI curi = getURI();
                    Throwable cause = e.getCause();
                    final String error = 
                      "Component raised an exception while applying transition method ["
                      + instance.getClass().getName() + "#" + method.getName() + "]";
                    throw new ControlException( curi, error, cause );
                }
            }
        }
        else
        {
            final String error = 
              "Scheme not recognized."
              + "\nScheme: " + scheme
              + "\nURI: " + uri;
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Internal utility to populate method arguments using the current state, target state
    * (in the case of transitions), assigned logging channel, and the active component 
    * instance.
    *
    * @param parameters the array of method parameter arguments
    * @param state the current state
    * @param target the transition target state (possible null when handling operations)
    * @return the populated object array
    */
    private Object[] resolveArguments( ComponentHandler entry, Class[] parameters, State state, State target )
    {
        Object instance = getInstance( entry );
        boolean firstStateAssigned = false;
        Object[] args = new Object[ parameters.length ];
        for( int i=0; i<parameters.length; i++ )
        {
            Class c = parameters[i];
            if( java.util.logging.Logger.class.isAssignableFrom( c ) )
            {
                URI uri = entry.getURI();
                args[i] = getJavaLoggerForURI( uri );
            }
            else if( Logger.class.isAssignableFrom( c ) )
            {
                args[i] = entry.getLogger();
            }
            else if( State.class.isAssignableFrom( c ) )
            {
                if( false == firstStateAssigned )
                {
                    args[i] = state;
                    firstStateAssigned = true;
                }
                else
                {
                    if( null != target )
                    {
                        args[i] = target; // its a transition
                    }
                    else
                    {
                        args[i] = state; // fallback to using current state
                    }
                }
            }
            else if( instance.getClass().isAssignableFrom( c ) )
            {
                args[i] = instance;
            }
            else
            {
                final String error =
                  "Could not resolve a argument value for a parameter."
                  + "\nParameter type: " + c.getName()
                  + "\nPosition: " + i
                  + "\nComponent: " + instance.getClass().getName();
                throw new RuntimeException( error );
            }
        }
        return args;
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
    public synchronized void terminate( Component component )
    {
        if( component instanceof ComponentHandler )
        {
            ComponentHandler entry = (ComponentHandler) component;
            try
            {
                executeTermination( entry );
            }
            catch( RecursiveTerminationException e ) 
            {
                // recusive termination path
                String error = e.getMessage();
                getLogger().warn( error, e );
            }
            finally
            {
                entry.setInitialized( false );
                entry.setState( entry.getStateGraph() );
            }
        }
        else
        {
            final String error = 
              "Unsupported component implementation class."
              + "\nComponent: " + component.getURI()
              + "\nClass: " + component.getClass().getName()
              + "\nMethod: terminate/1";
            throw new IllegalArgumentException( error );
        }
    }

    synchronized void executeTermination( ComponentHandler component ) throws RecursiveTerminationException
    {
        if( false == component.isInitialized() )
        {
            return;
        }

        if( component instanceof Container )
        {
            Container container = (Container) component;
            Component[] parts = container.getShutdownSequence();
            for( int i=0; i<parts.length; i++ )
            {
                Component part = parts[i];
                try
                {
                    part.terminate();
                }
                catch( Throwable e )
                {
                    URI uri = getURI();
                    final String error = 
                      "Failed to terminate a subsidiary part."
                      + "\nContainer: " + component.getURI()
                      + "\nComponent: " + part.getURI();
                    getLogger().warn( error, e );
                }
            }
        }
    
        List visited = new LinkedList();
        boolean flag = true;
        while( flag )
        {
            State state = component.getCurrentState();
            Transition terminator = state.getTerminator();
            if( null == terminator )
            {
                return;
            }
            else if( !visited.contains( state.getName() ) )
            {
                try
                {
                    visited.add( state.getName() );
                    flag = applyTransition( component, terminator );
                }
                catch( Exception e )
                {
                    flag = false;
                    getLogger().warn( "Ignoring termination handler error", e );
                }
            }
            else
            {
                boolean first = true;
                StringBuffer buffer = new StringBuffer();
                buffer.append( "Termination sequence aborted." );
                buffer.append( "\nReason: detection of a recursive termination sequence" );
                Iterator iterator = visited.iterator();
                while( iterator.hasNext() )
                {
                     String name = (String) iterator.next();
                     if( first )
                     {
                         buffer.append( "\n  State [" + name + "]" );
                         first = false;
                     }
                     else
                     {
                         buffer.append( " --> [" + name + "]" );
                     }
                }
                buffer.append( " --> [" + state.getName() + "]" );
                String error = buffer.toString();
                visited.clear();
                throw new RecursiveTerminationException( error );
            }
        }
    }

   /**
    * Utility to locate athe first public method with the supplied name in the 
    * supplied class.
    * @param c the class to introspect
    * @param operation the method name
    * @return the first method found  
    * @exception ValidationException if the class does not delcare the 
    *    the named method
    */
    private Method locateMethod( Class c, String operation ) throws ValidationException
    {
        Method[] methods = c.getMethods();

        ArrayList list = new ArrayList();
        for( int i=0; i<methods.length; i++ )
        {
            Method method = methods[i];
            String name = method.getName();
            if( operation.equals( name ) )
            {
                list.add( method );
            }
        }
        Method[] candidates = (Method[]) list.toArray( new Method[0] );
        if( candidates.length == 1 )
        {
            return candidates[0];
        }
        else if( candidates.length > 1 )
        {
            for( int i=0; i<candidates.length; i++ )
            {
                Method method = candidates[i];
                int n = method.getParameterTypes().length;
                if( n == 0 )
                {
                    return method;
                }
            }

            final String error =
              "Component implementation class contains duplicate transition methods."
              + "\nTransition method: " + c.getName() + "#" + operation;
            throw new ValidationException( error );
        }
        else
        {
            final String error = 
              "Class does not implement a public transition method '" + operation 
              + "'."
              + "\nClass: " + c.getName();
            throw new ValidationException( error );
        }
    }
}
