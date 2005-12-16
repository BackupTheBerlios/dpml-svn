/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import java.util.ArrayList;
import java.util.List;

import net.dpml.state.Trigger;
import net.dpml.state.State;
import net.dpml.state.Operation;
import net.dpml.state.Transition;
import net.dpml.state.impl.DefaultState;


/**
 * Utility datatype supporting State instance construction.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StateDataType
{
    private final boolean m_root;
    
    private String m_name;
    private List m_states = new ArrayList();
    private List m_operations = new ArrayList();
    private List m_transitions = new ArrayList();
    private List m_triggers = new ArrayList();
    private boolean m_terminal = false;
    
    StateDataType()
    {
        this( false );
    }
    
    StateDataType( boolean root )
    {
        m_root = root;
    }
    
   /**
    * Set the state name.
    * @param name the cname of the state
    */
    public void setName( final String name )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
    }
    
   /**
    * Mark the state as a terminal state.
    * @param flag true if this is a terminal state
    */
    public void setTerminal( final boolean flag )
    {
        m_terminal = flag;
    }
    
   /**
    * Add a substate within the state.
    * @return the sub-state datatype
    */
    public StateDataType createState()
    {
        final StateDataType state = new StateDataType();
        m_states.add( state );
        return state;
    }
    
   /**
    * Add an operation within this state.
    * @return the operation datatype
    */
    public OperationDataType createOperation()
    {
        final OperationDataType operation = new OperationDataType();
        m_operations.add( operation );
        return operation;
    }
    
   /**
    * Add an transition within this state.
    * @return the operation datatype
    */
    public TransitionDataType createTransition()
    {
        final TransitionDataType transition = new TransitionDataType();
        m_transitions.add( transition );
        return transition;
    }

   /**
    * Add an trigger to the state.
    * @return the trigger datatype
    */
    public TriggerDataType createTrigger()
    {
        final TriggerDataType trigger = new TriggerDataType();
        m_triggers.add( trigger );
        return trigger;
    }
    
    DefaultState getState()
    {
        String name = getStateName();
        Trigger[] triggers = getTriggers();
        Operation[] operations = getOperations();
        Transition[] transitions = getTransitions();
        State[] states = getStates();
        return new DefaultState( name, triggers, transitions, operations, states, m_terminal ); 
    }
    
    String getStateName()
    {
        if( m_root )
        {
            return "";
        }
        else
        {
            return m_name;
        }
    }
    
    Trigger[] getTriggers()
    {
        TriggerDataType[] types = (TriggerDataType[]) m_triggers.toArray( new TriggerDataType[0] );
        Trigger[] values = new Trigger[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            TriggerDataType data = types[i];
            values[i] = data.getTrigger();
        }
        return values;
    }
    
    Operation[] getOperations()
    {
        OperationDataType[] types = (OperationDataType[]) m_operations.toArray( new OperationDataType[0] );
        Operation[] values = new Operation[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            OperationDataType data = types[i];
            values[i] = data.getOperation();
        }
        return values;
    }
    
    Transition[] getTransitions()
    {
        TransitionDataType[] types = (TransitionDataType[]) m_transitions.toArray( new TransitionDataType[0] );
        Transition[] values = new Transition[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            TransitionDataType data = types[i];
            values[i] = data.getTransition();
        }
        return values;
    }
    
    State[] getStates()
    {
        StateDataType[] types = (StateDataType[]) m_states.toArray( new StateDataType[0] );
        State[] values = new State[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            StateDataType data = types[i];
            values[i] = data.getState();
        }
        return values;
    }
}
