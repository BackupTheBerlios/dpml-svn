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

package net.dpml.metro.runtime;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.logging.Logger;

import net.dpml.metro.part.Instance;
import net.dpml.metro.part.ControlException;

import net.dpml.metro.state.State;
import net.dpml.metro.state.StateEvent;
import net.dpml.metro.state.StateListener;
import net.dpml.metro.state.UnknownTransitionException;
import net.dpml.metro.state.UnknownOperationException;
import net.dpml.metro.state.impl.DefaultStateMachine;

/**
 * The DefaultInstance class maintains the state of a client instance.  On creation
 * of a new DefaultInstance the implementation constructs a proxy based on the 
 * service clesses declared within the component type, establishes the instance, and
 * executes initialization based on the state graph associated with the 
 * object.  If a request is made by the container for the disposal of an instance of 
 * this class, the implementation will execute a formal termination sequence on the 
 * client instance using the state graph declarations.  Finally, if the client instance 
 * implements the Disposable interface - the implementation will invoke the dispose 
 * operation on the client instance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultInstance extends UnicastEventSource implements Instance
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component handler.
    */
    private final ComponentHandler m_handler;
    
   /**
    * The state machine that tracks the instance state.
    */
    private final DefaultStateMachine m_machine;
    
   /**
    * The logging channel.
    */
    private final Logger m_logger;

    //-------------------------------------------------------------------
    // mutable state
    //-------------------------------------------------------------------
    
   /**
    * The raw instance.
    */
    private Object m_value;

   /**
    * The proxied instance.
    */
    private Object m_proxy;

   /**
    * Tag used within logging messages.
    */
    private String m_tag;

   /**
    * The available state.
    */
    private boolean m_activated = false;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a new instance handler.
    *
    * @param handler the component handler
    * @param logger the logging channel
    */
    DefaultInstance( ComponentHandler handler, Logger logger ) 
      throws RemoteException, ControlException, InvocationTargetException
    {
        super();
        
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        
        m_logger = logger;
        m_handler = handler;

        State graph = handler.getStateGraph();
        m_machine = new DefaultStateMachine( graph );
        m_machine.addPropertyChangeListener( new StateEventPropergator( this ) );
        initialize();
    }
    
    //-------------------------------------------------------------------
    // Instance
    //-------------------------------------------------------------------

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
        super.addListener( listener );
    }

   /**
    * Remove a state listener from the handler.
    * @param listener the state listener to be removed
    */
    public synchronized void removeStateListener( StateListener listener )
    {
        super.removeListener( listener );
    }
   
   /**
    * Return the runtime value associated with this instance.
    * @param isolate if TRUE the value returned is a proxy exposing the 
    *    service interfaces declared by the component type otherwise 
    *    the instance value is returned.
    */
    public Object getValue( boolean isolate )
    {
        if( isDisposed() )
        {
            throw new IllegalStateException( "disposed" );
        }
        if( !m_activated )
        {
            throw new IllegalStateException( "deactivated" );
        }
        if( isolate )
        {
            return m_proxy;
        }
        else
        {
            return m_value;
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
        return m_machine.apply( key, m_value );
    }
    
   /**
    * Invoke an operation on the instance.
    * @param key the operation name
    * @exception UnknownOperationException if the supplied key does not map to an available operation
    * @exception InvocationTargetException if an invocation error occurs
    * @exception RemoteException if a remote I/O error occurs
    */
    public synchronized void exec( String key )
      throws UnknownOperationException, InvocationTargetException
    {
        m_machine.execute( key, m_value );
    }
    
    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

    protected void processEvent( EventObject event )
    {
        if( event instanceof StateEvent )
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
            m_logger.warn( error );
        }
    }
    
    //-------------------------------------------------------------------
    // Object
    //-------------------------------------------------------------------

    public void finalize() throws Throwable
    {
        getLogger().debug( "finalizing " + this );
        dispose();
    }
    
    public String toString()
    {
        return m_handler.getPath() + "#" + System.identityHashCode( m_value );
    }
    
    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------
    
    private void initialize() throws InvocationTargetException, ControlException
    {
        synchronized( getLock() )
        {
            ClassLoader classloader = m_handler.getClassLoader();
            Class[] services = m_handler.getServiceClassArray();
            InvocationHandler invocationHandler = new InstanceInvocationHandler( this );
            m_proxy = Proxy.newProxyInstance( classloader, services, invocationHandler );
            m_value = m_handler.createNewObject();
            m_tag = createTag( m_value );
            getLogger().debug( m_tag + "activating instance" );
            m_machine.initialize( m_value );
            m_activated = true;
        }
    }

    boolean isAvailable()
    {
        return m_activated;
    }

    void dispose()
    {
        synchronized( getLock() )
        {
            getLogger().debug( m_tag + "instance disposal" );
            deactivate();
            m_machine.dispose();
            super.dispose();
        }
    }
    
    private void deactivate()
    {
        if( !m_activated )
        {
            return;
        }
        
        synchronized( getLock() )
        {
            getLogger().debug( m_tag + "deactivating instance" );
            m_machine.terminate( m_value );
            m_tag = null;
            m_value = null;
            m_activated = false;
        }
    }
    
    ComponentHandler getComponentHandler()
    {
        return m_handler;
    }
    
    void addPropertyChangeListener( PropertyChangeListener listener )
    {
        m_machine.addPropertyChangeListener( listener );
    }

    void removePropertyChangeListener( PropertyChangeListener listener )
    {
        m_machine.removePropertyChangeListener( listener );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private String createTag( Object instance )
    {
        String tag = 
          "[" 
          + System.identityHashCode( instance ) 
          + "               ";
        return tag.substring( 0, 10 ) + "] ";
    }

   /**
    * State event propergator implementation.
    */
    private final class StateEventPropergator implements PropertyChangeListener
    {
        private final DefaultInstance m_holder;
        
        private StateEventPropergator( DefaultInstance holder )
        {
            m_holder = holder;
        }
        
       /**
        * Handle a property change notification.
        * @param event the property change event
        */
        public void propertyChange( PropertyChangeEvent event )
        {
            //
            // fire a state change event to remote listeners
            //
            
            State oldState = (State) event.getOldValue();
            State newState = (State) event.getNewValue();
            getLogger().debug( m_tag + " state changing from: " + oldState + " to: " + newState );
            StateEvent stateEvent = new StateEvent( m_holder, oldState, newState );
            m_holder.enqueueEvent( stateEvent );
        }
    }
}
