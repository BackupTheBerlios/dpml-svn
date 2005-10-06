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

import java.util.EventObject;
import java.rmi.RemoteException;

import net.dpml.component.model.ComponentModel;

import net.dpml.composition.event.EventProducer;

import net.dpml.logging.Logger;

import net.dpml.part.Handler;

import net.dpml.state.State;
import net.dpml.state.StateMachine;
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
    }

    //--------------------------------------------------------------------------
    // Handler
    //--------------------------------------------------------------------------
    
   /**
    * Returns the current state of the control.
    * @return the current runtime state
    */
    public State getState()
    {
        return m_machine.getState();
    }
    
   /**
    * Add a state listener to the control.
    * @param listener the state listener
    */
    public void addStateListener( StateListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a state listener from the control.
    * @param listener the state listener
    */
    public void removeStateListener( StateListener listener )
    {
        super.removeListener( listener );
    }

    //--------------------------------------------------------------------------
    // EventProducer
    //--------------------------------------------------------------------------

    protected void processEvent( EventObject event )
    {
    }
    
    //--------------------------------------------------------------------------
    // internals
    //--------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
