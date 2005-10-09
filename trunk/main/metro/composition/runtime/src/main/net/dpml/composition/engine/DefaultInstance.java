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

package net.dpml.composition.engine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.composition.event.EventProducer;

import net.dpml.logging.Logger;

import net.dpml.part.Instance;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.state.impl.DefaultStateMachine;

import net.dpml.transit.model.Value;

/**
 * Instance holder.
 *
 * TODO: add disposal semantics (including disposal listeners)
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class DefaultInstance extends EventProducer implements Value, Instance // Disposable
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
    private final StateMachine m_machine;
    
   /**
    * The raw instance.
    */
    private final Object m_instance;

   /**
    * The proxied instance.
    */
    private final Object m_proxy;

   /**
    * The logging channel.
    */
    private final Logger m_logger;

   /**
    * The available state.
    */
    private boolean m_available = false;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param handler the component handler
    */
    DefaultInstance( ComponentHandler handler, Logger logger, Object instance ) 
      throws InvocationTargetException, RemoteException
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
        if( null == instance )
        {
            throw new NullPointerException( "instance" );
        }
        
        m_logger = logger;
        m_handler = handler;
        m_instance = instance;
        
        State graph = handler.getStateGraph();
        m_machine = new DefaultStateMachine( graph );
        m_machine.addPropertyChangeListener( new StateEventPropergator( this ) );
        ClassLoader classloader = handler.getClassLoader();
        Class[] services = handler.getServiceClassArray();
        InvocationHandler invocationHandler = new InstanceInvocationHandler( instance );
        m_proxy = Proxy.newProxyInstance( classloader, services, invocationHandler );
        
        //
        // startup the instance
        //
        
        m_machine.initialize( instance );
        m_available = true;
    }

    //-------------------------------------------------------------------
    // Value
    //-------------------------------------------------------------------

    public Object resolve() throws Exception
    {
        return resolveInstance( null, true );
    }
    
    public Object resolve( boolean isolate ) throws Exception
    {
        return resolveInstance( null, isolate );
    }
    
    public Object resolve( Map map ) throws Exception
    {
        return resolveInstance( map, true );
    }
    
    public Object resolve( Map map, boolean isolate ) throws Exception
    {
        return resolveInstance( map, isolate );
    }
    
    private Object resolveInstance( Map map, boolean isolate ) throws Exception
    {
        if( m_available )
        {
            if( isolate )
            {
                return m_proxy;
            }
            else
            {
                return getInstance();
            }
        }
        else
        {
            final String error = 
              "The instance of the component ["
              + m_handler.getPath()
              + "] is unavailable.";
            throw new IllegalStateException( error );
        }
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
    // implementation
    //-------------------------------------------------------------------

    void dispose()
    {
        m_available = false;
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

    private Object getInstance()
    {
        return m_instance;
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

    private class StateEventPropergator implements PropertyChangeListener
    {
        private final DefaultInstance m_holder;
        
        private StateEventPropergator( DefaultInstance holder )
        {
            m_holder = holder;
        }
        
        public void propertyChange( PropertyChangeEvent event )
        {
            //
            // fire a remote state change event to state event listeners
            //
            
            State oldState = (State) event.getOldValue();
            State newState = (State) event.getNewValue();
            getLogger().debug( "state changing from: " + oldState + " to: " + newState );
            StateEvent stateEvent = new StateEvent( m_holder, oldState, newState );
            m_holder.enqueueEvent( stateEvent );
        }
    }
}
