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

package net.dpml.metro.state;

/**
 * Interface describing an application state.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface State
{   
   /**
    * Return the name of the state.
    * @return the state name
    */
    String getName();
    
   /**
    * Set the parent state. 
    * @param state the parent state
    */
    void setParent( State state );

   /**
    * Return the parent state to this state or null if this is 
    * the root of a state graph.
    * @return the parent state
    */
    State getParent();
    
   /**
    * Return the state path.  The path is composed of a sequence of 
    * states from the root to this state.
    * @return the state path
    */
    State[] getStatePath();
    
   /**
    * Return the substates within this state.
    * @return the substate array
    */
    State[] getStates();
    
   /**
    * Return the array of triggers associated with the state.
    * @return the trigger array
    */
    Trigger[] getTriggers();
    
   /**
    * Return the array of transtions associated with the state.
    * @return the transition array
    */
    Transition[] getTransitions();
    
   /**
    * Return the array of operations associated with the state.
    * @return the operation array
    */
    Operation[] getOperations();
    
   /**
    * Test is the state is a terminal state.
    * @return true if terminal
    */
    boolean isTerminal();
}
