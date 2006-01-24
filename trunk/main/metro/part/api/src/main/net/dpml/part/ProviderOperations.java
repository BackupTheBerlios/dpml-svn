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

package net.dpml.part;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.UnknownOperationException;
import net.dpml.state.UnknownTransitionException;

/**
 * ProviderOperations interface defines the set of operations 
 * common between local and remote provider consumers.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ProviderOperations
{
    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

   /**
    * Returns the current state of the control.
    * @return the current runtime state
    * @exception RemoteException if a remote I/O error occurs
    */
    State getState() throws RemoteException;
    
   /**
    * Add a state listener to the control.
    * @param listener the state listener
    * @exception RemoteException if a remote I/O error occurs
    */
    void addStateListener( StateListener listener ) throws RemoteException;

   /**
    * Remove a state listener from the control.
    * @param listener the state listener
    * @exception RemoteException if a remote I/O error occurs
    */
    void removeStateListener( StateListener listener ) throws RemoteException;
    
   /**
    * Return the runtime value associated with this instance.
    * @param isolate if TRUE the value returned is a proxy exposing the 
    *    service interfaces declared by the component type otherwise 
    *    the instance value is returned.
    * @return the value
    * @exception RemoteException if a remote I/O error occurs
    */
    Object getValue( boolean isolate ) throws RemoteException;
    
   /**
    * Apply a transition to the instance.
    * @param key the transition name
    * @return the state established as a result of applying the transition
    * @exception UnknownTransitionException if the supplied key does not map to an available transition
    * @exception InvocationTargetException if an invocation error occurs
    * @exception RemoteException if a remote I/O error occurs
    */
    State apply( String key ) 
      throws UnknownTransitionException, InvocationTargetException, RemoteException;
    
   /**
    * Invoke an operation on the instance.
    * @param name the operation name
    * @param args operation arguments
    * @return the result of the operation invocation
    * @exception UnknownOperationException if the supplied key does not map to an available operation
    * @exception InvocationTargetException if an invocation error occurs
    * @exception RemoteException if a remote I/O error occurs
    */
    Object exec( String name, Object[] args ) 
      throws UnknownOperationException, InvocationTargetException, RemoteException;
    
}
