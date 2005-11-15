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

import net.dpml.logging.Logger;

import net.dpml.part.Instance;
import net.dpml.part.ControlException;
import net.dpml.part.ActivationPolicy;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.state.impl.DefaultStateMachine;

/**
 * Instance holder.
 *
 * TODO: add disposal semantics (including disposal listeners)
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
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

   /**
    * The proxied instance.
    */
    private final Object m_proxy;

    //-------------------------------------------------------------------
    // mutable state
    //-------------------------------------------------------------------
    
   /**
    * The raw instance.
    */
    private Object m_value;

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
    * Create a context invocation handler.
    *
    * @param handler the component handler
    */
    DefaultInstance( ComponentHandler handler, Logger logger ) 
      throws RemoteException
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
        
        ClassLoader classloader = m_handler.getClassLoader();
        Class[] services = m_handler.getServiceClassArray();
        InvocationHandler invocationHandler = new InstanceInvocationHandler( this );
        m_proxy = Proxy.newProxyInstance( classloader, services, invocationHandler );

        try
        {
            Object value = m_handler.createNewObject();
            activate( value );
        }
        catch( ControlException e )
        {
            getLogger().warn( "Instance value establishment control error.", e );
        }
        catch( InvocationTargetException e )
        {
            getLogger().warn( "Target component instantiation error.", e );
        }
        catch( Throwable e )
        {
            getLogger().warn( "Unexpected activation failure.", e );
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
    
    boolean isAvailable()
    {
        return m_activated;
    }

    void activate( Object value ) throws InvocationTargetException, ControlException
    {
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }
        
        if( m_activated )
        {
            deactivate();
        }
        synchronized( getLock() )
        {
            setValue( value );
            getLogger().debug( m_tag + "activating" );
            m_machine.initialize( m_value );
            m_activated = true;
            getLogger().debug( m_tag + "activated" );
        }
    }

    void deactivate()
    {
        if( !m_activated )
        {
            return;
        }
        
        synchronized( getLock() )
        {
            getLogger().debug( m_tag + "deactivating" );
            m_machine.terminate( m_value );
            getLogger().debug( m_tag + "deactivated" );
            m_tag = null;
            m_value = null;
            m_activated = false;
        }
    }
    
    protected void dispose()
    {
        synchronized( getLock() )
        {
            deactivate();
            m_machine.dispose();
            super.dispose();
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
    
    private void setValue( Object value )
    {
        m_value = value;
        String tag = "[" + System.identityHashCode( value ) + "               ";
        m_tag = tag.substring( 0, 10 ) + "] ";
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
