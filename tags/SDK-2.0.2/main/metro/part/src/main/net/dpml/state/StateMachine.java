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

package net.dpml.state;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>Defintion of the state machine.  The state machine handles the maintenance 
 * of a single current state and supports the invocation of transition and operations
 * relative to the current state.  Invocation of transiton and operations are handled
 * via two mechansims:</p>
 *
 * <ol>
 *  <li>triggers</li>
 *  <li>explicit invocation of the 'appply' and 'execute' operations</li>
 * </ol>
 *
 * <p>Triggers, Transitions, and Operations may be declared within any state and enter
 * active scope when when the enclosing state is within the current state path
 * (where the current state path is the sequence of state's from the current state to  
 * the root state).</p>
 *
 * <p>Transitions and operations available at any given time are a function of 
 * all uniquely named transitions and operations exposed within the current state path.
 * If multiple instances of a transition or operation share the same name, the instance
 * closest to the current state takes precedence and the duplicate instance will not be 
 * exposed (equivalent to overriding the characteristics of a super-state).
 *
 * <p>Triggers are structures that hold a single action (transition or operation) and 
 * are invoked automatically by a state machine implementation as a part of initialization
 * and termination requests. If a trigger contains a transition resulting that results in 
 * a change to the current state a state-machine will recursively evaluate triggers in the 
 * new current state path.  The process of recursive evaluation of triggers will continue
 * until a state is reached where no further trigger invocation is possible.</p>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface StateMachine
{
   /**
    * Add a property change listener to the state machine.
    * @param listener the property change listener
    */
    //void addPropertyChangeListener( final PropertyChangeListener listener );
    
   /**
    * Remove a property change listener from the state machine.
    * @param listener the property change listener
    */
    //void removePropertyChangeListener( final PropertyChangeListener listener );
    
   /**
    * Add a state change listener to the state machine.
    * @param listener the state listener
    */
    void addStateListener( final StateListener listener );
    
   /**
    * Remove a state listener from the state machine.
    * @param listener the state listener
    */
    void removeStateListener( final StateListener listener );

   /**
    * Return the current state.
    * @return the current state
    */
    State getState();
    
   /**
    * Invoke initialization of the supplied object using the initialization action
    * declared under the current state path.
    * 
    * @param object the object to initialize
    * @return the state established as a side-effect of initialization
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of initialization
    */
    State initialize( Object object ) throws InvocationTargetException;
    
   /**
    * Execute a named operation on the supplied object.
    * @param name an operation name
    * @param object the target object
    * @param args operation argument array
    * @return the return value
    * @exception UnknownOperationException if the operation is unknown
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of operation execution
    */
    Object execute( String name, Object object, Object[] args ) 
      throws UnknownOperationException, InvocationTargetException;
    
   /**
    * Invoke a management method on the supplied object.
    * @param object the target object
    * @param method the method name
    * @param args method parameter arguments
    * @return the return value
    * @exception IllegalStateException if the method is recognized but not available
    * @exception UnknownOperationException if the operation is unknown
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of operation execution
    */
    Object invoke( Object object, String method, Object[] args ) 
      throws UnknownOperationException, InvocationTargetException, IllegalStateException;
    
   /**
    * Apply a named state transition.
    * @param name the transition name
    * @param object the object against which any transition handler action are to be applied
    * @return the state established by the application of the transition
    * @exception UnknownTransitionException if the transition is unknown
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of transition invocation
    */
    State apply( String name, Object object ) 
      throws UnknownTransitionException, InvocationTargetException;
    
   /**
    * Return all of the available transitions relative to the current state.
    * @return the available transitions
    */
    Transition[] getTransitions();
    
   /**
    * Return all of the available operations relative to the current state.
    * @return the available operations
    */
    Operation[] getOperations();
    
   /**
    * Return all of the available interfaces relative to the current state.
    * @return the available interface declarations
    */
    Interface[] getInterfaces();
    
   /**
    * Invoke termination of the supplied object using the termination action
    * declared under the current state path.
    * 
    * @param object the object to terminate
    * @return the state established as a side-effect of the termination
    */
    State terminate( Object object );

   /**
    * Returns the active status of the state machine.
    * @return TRUE if the state machine has invoked initialization and 
    * termination has not been performed otherwise FALSE
    */
    boolean isActive();
    
}
