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

package net.dpml.state.impl;

import java.io.Serializable;
import java.util.Arrays;

import net.dpml.state.State;
import net.dpml.state.Transition;
//import net.dpml.state.Initialization;
//import net.dpml.state.Termination;
import net.dpml.state.Operation;
import net.dpml.state.Trigger;

/**
 * Default implementation of an application state descriptor.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultState implements State, Serializable
{
    private final String m_name;
    //private final Initialization m_initialization;
    private final Transition[] m_transitions;
    private final Operation[] m_operations;
    private final State[] m_states;
    //private final Termination m_termination;
    private final Trigger[] m_triggers;
    private final boolean m_terminal;
    
    private State m_parent;
    
    public DefaultState( 
      final String name, final Trigger[] triggers, final Transition[] transitions, 
      final Operation[] operations, final DefaultState[] states, boolean terminal )
    {
        m_name = name;
        //m_initialization = initialization;
        m_triggers = triggers;
        m_transitions = transitions;
        m_operations = operations;
        m_states = states;
        //m_termination = termination;
        m_terminal = terminal;
        
        for( int i=0; i<states.length; i++ )
        {
            DefaultState state = states[i];
            state.setParent( this );
        }
    }
    
    public State getParent()
    {
        return m_parent;
    }

    void setParent( State state )
    {
        if( null == m_parent )
        {
            m_parent = state;
        }
        else
        {
            final String error = 
              "Illegal attempt to reassign parent.";
            throw new IllegalStateException( error );
        }
    }
    
    public String getName()
    {
        return m_name;
    }
    
    //public Initialization getInitialization()
    //{
    //    return m_initialization;
    //}
    
    //public Termination getTermination()
    //{
    //    return m_termination;
    //}
    
    public Trigger[] getTriggers()
    {
        return m_triggers;
    }
    
    public State[] getLocalStates()
    {
        return m_states;
    }
    
    public State[] getStatePath()
    {
        if( null == m_parent )
        {
            return new State[]{ this };
        }
        else
        {
            State[] path = m_parent.getStatePath();
            State[] result = new State[ path.length + 1 ];
            System.arraycopy( path, 0, result, 0, path.length );
            result[ path.length ] = this;
            return result;
        }
    }
    
    public State[] getStates()
    {
        State[] parents = getStatePath();
        State[] local = getLocalStates();
        State[] result = new State[ parents.length + local.length ];
        System.arraycopy( parents, 0, result, 0, parents.length );
        System.arraycopy( local, 0, result, parents.length, local.length  );
        return result;
    }
    
    public Transition[] getLocalTransitions()
    {
        return m_transitions;
    }
    
    public Transition[] getTransitions()
    {
        if( null != m_parent )
        {
            Transition[] parent = m_parent.getTransitions();
            Transition[] local = m_transitions;
            Transition[] result = new Transition[ parent.length + local.length ];
            System.arraycopy( parent, 0, result, 0, parent.length );
            System.arraycopy( local, 0, result, parent.length, local.length  );
            return result;
        }
        else
        {
            return m_transitions;
        }
    }
    
    public Operation[] getLocalOperations()
    {
        return m_operations;
    }
    
    public Operation[] getOperations()
    {
        if( null != m_parent )
        {
            Operation[] parent = m_parent.getOperations();
            Operation[] local = m_operations;
            Operation[] result = new Operation[ parent.length + local.length ];
            System.arraycopy( parent, 0, result, 0, parent.length );
            System.arraycopy( local, 0, result, parent.length, local.length  );
            return result;
        }
        else
        {
            return m_operations;
        }
    }
    
    public boolean getTerminal()
    {
        return isTerminal();
    }
    
    public boolean isTerminal()
    {
        return m_terminal;
    }
    
    public String toString()
    {
        return m_name;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof DefaultState )
        {
            DefaultState state = (DefaultState) other;
            if( !m_name.equals( state.getName() ) )
            {
                return false;
            }
            //else if( !equals( m_initialization, state.getInitialization() ) )
            //{
            //    return false;
            //}
            //else if( !equals( m_termination, state.getTermination() ) )
            //{
            //    return false;
            //}
            else if( m_terminal != state.isTerminal() )
            {
                return false;
            }
            else if( !Arrays.equals( m_triggers, state.getTriggers() ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_transitions, state.getLocalTransitions() ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_operations, state.getLocalOperations() ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_states, state.getLocalStates() ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    private boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return null == b;
        }
        else
        {
            return a.equals( b );
        }
    }
}
