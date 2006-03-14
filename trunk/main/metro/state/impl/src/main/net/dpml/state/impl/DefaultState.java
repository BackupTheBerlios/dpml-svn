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
import net.dpml.state.Interface;
import net.dpml.state.Operation;
import net.dpml.state.Trigger;
import net.dpml.state.Action;

/**
 * Default implementation of an application state descriptor.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultState implements State, Serializable
{
    private final String m_name;
    private final Transition[] m_transitions;
    private final Operation[] m_operations;
    private final Interface[] m_interfaces;
    private final State[] m_states;
    private final Trigger[] m_triggers;
    private final boolean m_terminal;
    
    private transient State m_parent;

   /**
    * Creation of a new state.
    * @param name the state name
    */
    public DefaultState()
    {
        this( "root" );
    }
    
   /**
    * Creation of a new state.
    * @param name the state name
    */
    public DefaultState( final String name )
    {
        this( name, new Trigger[0], new Transition[0], new Interface[0], new Operation[0], new State[0], true );
    }
    
   /**
    * Creation of a new non-terminal state.
    * @param name the state name
    * @param triggers an array of triggers
    * @param transitions an array of state transitions
    * @param interfaces an array of management interface declarations
    * @param operations an array of operations
    * @param states an array of substates
    */
    public DefaultState( 
      final String name, final Trigger[] triggers, final Transition[] transitions, 
      final Interface[] interfaces, final Operation[] operations, final State[] states )
    {
        this( name, triggers, transitions, interfaces, operations, states, false );
    }
    
   /**
    * Creation of a new state.
    * @param name the state name
    * @param triggers an array of triggers
    * @param transitions an array of state transitions
    * @param interfaces an array of management interface declarations
    * @param operations an array of operations
    * @param states an array of substates
    * @param terminal the terminal flag
    */
    public DefaultState( 
      final String name, final Trigger[] triggers, final Transition[] transitions, 
      final Interface[] interfaces, final Operation[] operations, final State[] states, boolean terminal )
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
        m_interfaces = interfaces;
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
        
        for( int i=0; i<interfaces.length; i++ )
        {
            Interface description = interfaces[i];
            if( null == description )
            {
                throw new NullPointerException( "interface/" + i );
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
    
   /**
    * Return the parent state to this state or null if this is the root of a 
    * state graph.
    * @return the parent state
    */
    public State getParent()
    {
        return m_parent;
    }

   /**
    * Set the parent state. 
    * @param state the parent state
    */
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
    
   /**
    * Return the name of the state.
    * @return the state name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the array of triggers associated with the state.
    * @return the trigger array
    */
    public Trigger[] getTriggers()
    {
        return m_triggers;
    }
    
   /**
    * Return the state path.  The path is composed of a sequence of states
    * from the root to this state.
    * @return the state path
    */
    public State[] getStatePath()
    {
        if( null == m_parent )
        {
            return new State[]{this};
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
    
   /**
    * Return the substates within this state.
    * @return the substate array
    */
    public State[] getStates()
    {
        return m_states;
    }
    
   /**
    * Return the array of transtions associated with the state.
    * @return the transition array
    */
    public Transition[] getTransitions()
    {
        return m_transitions;
    }
    
   /**
    * Return the array of operations associated with the state.
    * @return the operation array
    */
    public Operation[] getOperations()
    {
        return m_operations;
    }
    
   /**
    * Return the array of management interfaces associated with the state.
    * @return the interfaces array
    */
    public Interface[] getInterfaces()
    {
        return m_interfaces;
    }
    
   /**
    * Return the terminal flag.
    * @return true if terminal
    */
    public boolean getTerminal()
    {
        return isTerminal();
    }
    
   /**
    * Test is the state is a terminal state.
    * @return true if terminal
    */
    public boolean isTerminal()
    {
        return m_terminal;
    }
    
   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        State[] path = getStatePath();
        for( int i=0; i<path.length; i++ )
        {
            State state = path[i];
            if( i>0 )
            {
                buffer.append( "/" );
            }
            buffer.append( state.getName() );
        }
        return buffer.toString();
        //return "[" + m_name + "]";
    }
    
   /**
    * Compare this object to another for equality.
    * @param other the other object
    * @return true if the object is equal to this object
    */
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
            else
            {
                return Arrays.equals( m_states, state.getStates() );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hashcode for this instance.
    * @return the hashcode value
    */
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
