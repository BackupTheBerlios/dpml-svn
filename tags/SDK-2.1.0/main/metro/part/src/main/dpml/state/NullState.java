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

package dpml.state;

import java.io.Serializable;

import net.dpml.state.State;
import net.dpml.state.Transition;
import net.dpml.state.Operation;
import net.dpml.state.Interface;
import net.dpml.state.Trigger;

/**
 * Null state implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class NullState implements State, Serializable
{   
   /**
    * Return the name of the state.
    * @return the state name
    */
    public String getName()
    {
        return "";
    }
    
   /**
    * Set the parent state. 
    * @param state the parent state
    */
    public void setParent( State state )
    {
    }

   /**
    * Return the parent state to this state or null if this is 
    * the root of a state graph.
    * @return the parent state
    */
    public State getParent()
    {
        return null;
    }
    
   /**
    * Return the state path.  The path is composed of a sequence of 
    * states from the root to this state.
    * @return the state path
    */
    public State[] getStatePath()
    {
        return new State[0];
    }
    
   /**
    * Return the substates within this state.
    * @return the substate array
    */
    public State[] getStates()
    {
        return new State[0];
    }
    
   /**
    * Return the array of triggers associated with the state.
    * @return the trigger array
    */
    public Trigger[] getTriggers()
    {
        return new Trigger[0];
    }
    
   /**
    * Return the array of transtions associated with the state.
    * @return the transition array
    */
    public Transition[] getTransitions()
    {
        return new Transition[0];
    }
    
   /**
    * Return the array of operations associated with the state.
    * @return the operation array
    */
    public Operation[] getOperations()
    {
        return new Operation[0];
    }
    
   /**
    * Return the array of operations associated with the state.
    * @return the operation array
    */
    public Interface[] getInterfaces()
    {
        return new Interface[0];
    }
    
   /**
    * Test is the state is a terminal state.
    * @return true if terminal
    */
    public boolean isTerminal()
    {
        return false;
    }
    
   /**
    * Test is this state is equal to the supplied object.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            return ( other instanceof NullState );
        }
    }
    
   /**
    * Calcualte the hashcode for this instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
