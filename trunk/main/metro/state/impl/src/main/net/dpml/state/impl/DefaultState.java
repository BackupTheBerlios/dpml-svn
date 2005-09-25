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
import net.dpml.state.Operation;
import net.dpml.state.Trigger;
import net.dpml.state.Action;

/**
 * Default implementation of an application state descriptor.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultState implements State, Serializable
{
    private final String m_name;
    private final Transition[] m_transitions;
    private final Operation[] m_operations;
    private final State[] m_states;
    private final Trigger[] m_triggers;
    private final boolean m_terminal;
    
    private transient State m_parent;

    public DefaultState( final String name )
    {
        this( name, new Trigger[0], new Transition[0], new Operation[0], new State[0], true );
    }
    
    public DefaultState( 
      final String name, final Trigger[] triggers, final Transition[] transitions, 
      final Operation[] operations, final State[] states )
    {
        this( name, triggers, transitions, operations, states, false );
    }
    
    public DefaultState( 
      final String name, final Trigger[] triggers, final Transition[] transitions, 
      final Operation[] operations, final State[] states, boolean terminal )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            if( null == operation )
            {
                throw new NullPointerException( "operation/" + i );
            }
        }
        
        m_name = name;
        m_triggers = triggers;
        m_transitions = transitions;
        m_operations = operations;
        m_states = states;
        m_terminal = terminal;
        
        for( int i=0; i<transitions.length; i++ )
        {
            Transition transition = transitions[i];
            if( null == transition )
            {
                throw new NullPointerException( "transition/" + i );
            }
            transition.setState( this );
        }
        
        for( int i=0; i<triggers.length; i++ )
        {
            Trigger trigger = triggers[i];
            if( null == trigger )
            {
                throw new NullPointerException( "trigger/" + i );
            }
            Action action = trigger.getAction();
            if( action instanceof Transition )
            {
                Transition transition = (Transition) action;
                transition.setState( this );
            }
        }
        
        for( int i=0; i<states.length; i++ )
        {
            State state = states[i];
            if( null == state )
            {
                throw new NullPointerException( "state/ " + i );
            }
            state.setParent( this );
        }
    }
    
    public State getParent()
    {
        return m_parent;
    }

    public void setParent( State state )
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
    
    public Trigger[] getTriggers()
    {
        return m_triggers;
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
        return m_states;
    }
    
    public Transition[] getTransitions()
    {
        return m_transitions;
    }
    
    public Operation[] getOperations()
    {
        return m_operations;
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
        return "[" + m_name + "]";
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
            else if( m_terminal != state.isTerminal() )
            {
                return false;
            }
            else if( !Arrays.equals( m_triggers, state.getTriggers() ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_transitions, state.getTransitions() ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_operations, state.getOperations() ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_states, state.getStates() ) )
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
    
    public int hashCode()
    {
        if( null == m_parent )
        {
            return m_name.hashCode();
        }
        else
        {
            int hash = m_parent.hashCode();
            hash ^= m_name.hashCode();
            return hash;
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
