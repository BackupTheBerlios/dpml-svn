/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.component.Provider;
import net.dpml.component.Component;
import net.dpml.component.ControlException;
import net.dpml.component.Service;
import net.dpml.component.ServiceNotFoundException;
import net.dpml.component.Disposable;
import net.dpml.component.Status;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;

import net.dpml.state.State;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.state.UnknownTransitionException;
import net.dpml.state.UnknownOperationException;

import net.dpml.lang.UnknownKeyException;

import net.dpml.util.Logger;
import net.dpml.util.EventQueue;

/**
 * The DefaultProvider class maintains the state of a single instance.  On creation
 * of a new DefaultProvider the implementation constructs a proxy based on the 
 * service classes declared within the component type, establishes the instance, and
 * executes initialization based on the state graph associated with the 
 * object.  If a request is made by the container for the disposal of an instance of 
 * this class, the implementation will execute a formal termination sequence on the 
 * instance using the state graph declarations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultProvider extends UnicastEventSource implements Provider, InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component handler.
    */
    private final DefaultComponentHandler m_handler;
    
   /**
    * The state machine that tracks the instance state.
    */
    private final DefaultStateMachine m_machine;
    
   /**
    * Internal parts of the component.
    */
    private final PartsManager m_parts;
    
   /**
    * The proxied instance.
    */
    private final Object m_proxy;

    //private final StateEventPropergator m_propergator;
    
    //-------------------------------------------------------------------
    // mutable state
    //-------------------------------------------------------------------
    
    private Status m_status = Status.INSTANTIATED;

   /**
    * The raw instance.
    */
    private Object m_value;

   /**
    * Tag used within logging messages.
    */
    private String m_tag;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a new instance handler.
    *
    * @param handler the component handler
    * @param logger the logging channel
    */
    DefaultProvider( EventQueue queue, DefaultComponentHandler handler, Logger logger ) 
      throws InvocationTargetException, IOException
    {
        super( queue, logger );
        
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        
        m_handler = handler;
        
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "provider instantiation" );
        }
        
        State graph = m_handler.getStateGraph();
        m_machine = new DefaultStateMachine( queue, logger, graph );
        //m_propergator = new StateEventPropergator();
        //m_machine.addPropertyChangeListener( m_propergator );
        m_parts = new DefaultPartsManager( this );
        
        ClassLoader classloader = m_handler.getClassLoader();
        Class[] services = m_handler.getServiceClassArray();
        if( allClassesAreInterfaces( services ) )
        {
            m_proxy = Proxy.newProxyInstance( classloader, services, this );
        }
        else
        {
            m_proxy = null;
        }
    }
    
    void initialize()
      throws RemoteException, ControlException, InvocationTargetException
    {
        synchronized( m_status )
        {
            m_status = Status.COMMISSIONING;
            try
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().trace( 
                      "instantiating [" 
                      + m_handler.getImplementationClass().getName() 
                      + "]" );
                }
                
                m_parts.commission();
                m_value = m_handler.getComponentController().createInstance( this );
                m_tag = createTag( m_value );
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "instantiated " + m_tag );
                }
                
                m_machine.initialize( m_value );
                m_status = Status.AVAILABLE;
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "activated " + m_tag );
                }
            }
            catch( Exception e )
            {
                final String error = 
                  "An error occured while attempting to initialize provider [" + this + "]";
                throw new ControllerException( error, e );
            }
        }
    }
    
    //-------------------------------------------------------------------
    // Provider
    //-------------------------------------------------------------------
    
   /**
    * Return the current status of the provider.
    * @return the provider status
    */
    public Status getStatus()
    {
        return m_status;
    }
    
   /**
    * Returns the current state of the instance.
    * @return the current runtime state
    */
    public State getState()
    {
        return m_machine.getState();
    }

   /**
    * Add a state listener to the handler.
    * @param listener the state listener
    */
    public synchronized void addStateListener( StateListener listener )
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "adding state listener [" + listener + "] to " + m_tag );
        }
        m_machine.addStateListener( listener );
    }

   /**
    * Remove a state listener from the handler.
    * @param listener the state listener to be removed
    */
    public synchronized void removeStateListener( StateListener listener )
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "removing state listener [" + listener + "] to " + m_tag );
        }
        m_machine.removeStateListener( listener );
    }
    
   /**
    * Return the runtime value associated with this instance.
    * @param isolate if TRUE the value returned is a proxy exposing the 
    *    service interfaces declared by the component type otherwise 
    *    the instance value is returned.
    */
    public Object getValue( boolean isolate )
    {
        synchronized( m_status )
        {
            if( Status.INSTANTIATED.equals( m_status ) )
            {
                try
                {
                    initialize();
                }
                catch( Exception e )
                {
                    final String error = 
                      "Initialization failure.";
                    throw new ControllerRuntimeException( error, e );
                }
            }
            checkAvailable();
            if( isolate  && ( null != m_proxy ) )
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().trace( "returning proxied service " + m_tag );
                }
                return m_proxy;
            }
            else
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().trace( "returning service instance " + m_tag );
                }
                return m_value;
            }
        }
    }
    
    private void checkAvailable()
    {
        if( !Status.AVAILABLE.equals( m_status ) )
        {
            final String error =
              "Provider is not available (current status " + m_status.getName() + ")";
            throw new IllegalStateException( error );
        }
    }
    
   /**
    * Apply a transition to the instance.
    * @param key the transition name
    * @exception UnknownTransitionException if the supplied key does not map to an available transition
    * @exception InvocationTargetException if an invocation error occurs
    * @exception RemoteException if a remote I/O error occurs
    */
    public synchronized State apply( String key ) 
      throws UnknownTransitionException, InvocationTargetException
    {
        Object value = getValue( false );
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "applying state transition [" + key + "] to " + m_tag );
        }
        return m_machine.apply( key, value );
    }
    
   /**
    * Invoke an operation on the instance.
    * @param name the operation name
    * @param args operation arguments
    * @return the result of the operation invocation
    * @exception UnknownOperationException if the supplied key does not map to an available operation
    * @exception InvocationTargetException if an invocation error occurs
    * @exception RemoteException if a remote I/O error occurs
    */
    public synchronized Object exec( String name, Object[] args )
      throws UnknownOperationException, InvocationTargetException
    {
        Object value = getValue( false );
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "applying operation [" + name + "] to " + m_tag );
        }
        return m_machine.execute( name, value, args );
    }
    
   /**
    * Invoke an operation on the instance.
    * @param method the operation name
    * @param args operation arguments
    * @return the result of the operation invocation
    * @exception UnknownOperationException if the supplied key does not map to an available operation
    * @exception InvocationTargetException if an invocation error occurs
    * @exception RemoteException if a remote I/O error occurs
    */
    public Object invoke( String method, Object[] args ) 
      throws UnknownOperationException, InvocationTargetException, IllegalStateException
    {
        Object value = getValue( false );
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "invoking method [" + method + "] to " + m_tag );
        }
        return m_machine.invoke( value, method, args );
    }
    

   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service definition
    * @return a component matching the serivce definiton
    * @exception ServiceNotFoundException if no component found
    * @exception RemoteException if a remote exception occurs
    */
    public Provider lookup( Service service ) throws ServiceNotFoundException, RemoteException
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( 
              "mediating lookup for [" 
              + service.getServiceClass().getName() 
              + "]" );
        }
        
        // check if a child component can handle the request
        
        ComponentHandler[] components = m_parts.getComponentHandlers();
        for( int i=0; i<components.length; i++ )
        {
            Component component = components[i];
            if( component.isaCandidate( service ) )
            {
                try
                {
                    return component.getProvider();
                }
                catch( Exception e )
                {
                    final String error = 
                      "Internal error while attempting to resolve provider."
                      + "\nEnclosing Provider: " + this
                      + "\nComponent: " + component;
                    throw new ControllerRuntimeException( error, e );
                }
            }
        }
        
        // delegate to the parent
        
        Provider parent = getParent();
        if( null != parent )
        {
            return parent.lookup( service );
        }
        else
        {
            String classname = service.getServiceClass().getName();
            throw new ServiceNotFoundException( CompositionController.CONTROLLER_URI, classname );
        }
    }
    
    public Provider getParent()
    {
        return m_handler.getParentProvider();
    }
    
    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

    public void processEvent( EventObject event )
    {
        if( event instanceof StateEvent )
        {
            StateEvent e = (StateEvent) event;
            EventListener[] listeners = getEventListeners();
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
                    catch( Throwable throwable )
                    {
                        final String error =
                          "Ignoring exception raised by a listener in response to a state change notification.";
                        getLogger().warn( error, throwable );
                    }
                }
            }
        }
        else
        {
            final String error =
              "Event type not supported."
              + "\nEvent Class: " + event.getClass().getName();
            getLogger().warn( error );
        }
    }
    
    //-------------------------------------------------------------------
    // Object
    //-------------------------------------------------------------------

    protected void finalize() throws Throwable
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "initiating finalization in " + m_tag );
        }
        String tag = m_tag;
        dispose();
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "finalization completed in " + tag );
        }
    }
    
    public String toString()
    {
        if( null != m_tag )
        {
            return m_handler.getPath() + "#" + m_tag;
        }
        else
        {
            return m_handler.getPath() + "#0";
        }
    }
    
    String getTag()
    {
        return m_tag;
    }
    
    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------
    
    PartsManager getPartsManager()
    {
        return m_parts;
    }
    
    private boolean allClassesAreInterfaces( Class[] classes )
    {
        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( !c.isInterface() )
            {
                return false;
            }
        }
        return true;
    } 

    public void dispose()
    {
        synchronized( getLock() )
        {
            if( Status.DECOMMISSIONING.equals( m_status ) || Status.DISPOSED.equals( m_status ) )
            {
                return;
            }
            m_status = Status.DECOMMISSIONING;
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "initiating disposal of " + m_tag );
            }
            
            m_machine.terminate( m_value );
            m_parts.decommission();
            //m_machine.removePropertyChangeListener( m_propergator );
            m_machine.dispose();
            if( m_parts instanceof Disposable )
            {
                Disposable disposable = (Disposable) m_parts;
                disposable.dispose();
            }
            super.dispose();
            m_status = Status.DISPOSED;
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "instance disposal " + m_tag );
            }
            m_tag = null;
            m_value = null;
        }
    }
    
    DefaultComponentHandler getDefaultComponentHandler()
    {
        return m_handler;
    }
    
    static String createTag( Object instance )
    {
        return "[" + System.identityHashCode( instance ) + "]";
    }
        
   /**
    * Invoke the specified method on underlying object.
    * This is called by the proxy object.
    *
    * @param proxy the proxy object
    * @param method the method invoked on proxy object
    * @param args the arguments supplied to method
    * @return the return value of method
    * @throws Throwable if an error occurs
    */
    public Object invoke( final Object proxy, final Method method, final Object[] args ) 
      throws InvocationTargetException, IllegalAccessException
    {
        checkAvailable();
        return method.invoke( m_value, args );
    }
}
