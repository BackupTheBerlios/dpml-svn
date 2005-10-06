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
import java.util.EventObject;
import java.util.EventListener;
import java.rmi.RemoteException;

import net.dpml.component.model.ComponentModel;

import net.dpml.composition.event.EventProducer;

import net.dpml.logging.Logger;

import net.dpml.part.Handler;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;
import net.dpml.state.impl.DefaultStateMachine;

/**
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentHandler extends EventProducer implements Handler
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final ComponentController m_control;
    private final ComponentModel m_model;
    private final StateMachine m_machine;
    
    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------
    
    public ComponentHandler( Logger logger, ComponentController control, ComponentModel model )
      throws RemoteException
    {
        super();
        
        m_logger = logger;
        m_control = control;
        m_model = model;
        
        State graph = model.getStateGraph();
        m_machine = new DefaultStateMachine( graph );
        m_machine.addPropertyChangeListener( new StateEventPropergator( this ) );
    }
    
    //--------------------------------------------------------------------------
    // Handler
    //--------------------------------------------------------------------------
    
   /**
    * Returns the current state of the handler.
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
    public void addStateListener( StateListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a state listener from the handler.
    * @param listener the state listener to be removed
    */
    public void removeStateListener( StateListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    */
    public boolean isActive()
    {
        return m_machine.isActive();
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
    
    //--------------------------------------------------------------------------
    // ComponentHandler
    //--------------------------------------------------------------------------
    
    ComponentModel getComponentModel()
    {
        return m_model;
    }
    
    StateMachine getStateMachine()
    {
        return m_machine;
    }
    
    Object getInstance()
    {
        return null;
    }
    
    Handler getParentHandler()
    {
        return null;
    }
    
    //--------------------------------------------------------------------------
    // internals
    //--------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private class StateEventPropergator implements PropertyChangeListener
    {
        private final ComponentHandler m_handler;
        
        private StateEventPropergator( ComponentHandler handler )
        {
            m_handler = handler;
        }
        
        public void propertyChange( PropertyChangeEvent event )
        {
            State oldState = (State) event.getOldValue();
            State newState = (State) event.getNewValue();
            StateEvent stateEvent = new StateEvent( m_handler, oldState, newState );
            m_handler.enqueueEvent( stateEvent );
        }
    }
    
    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------

    public String toString()
    {
        try
        {
            return "component:" + m_model.getImplementationClassName();
        }
        catch( RemoteException e )
        {
            return "handler:" + getClass().getName();
        }
    }
}
