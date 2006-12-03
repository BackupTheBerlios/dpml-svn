/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.component;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.UnknownOperationException;
import net.dpml.state.UnknownTransitionException;

/**
 * Provider holder.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Provider extends Remote
{
   /**
    * Return a parent provider.
    * @return the parent provider or null if this is a root provider
    * @exception RemoteException if a remote I/O occurs
    */
    Provider getParent() throws RemoteException;
    
   /**
    * Return the current status of the provider.
    * @return the provider status
    * @exception RemoteException if a remote I/O occurs
    */
    Status getStatus() throws RemoteException;
    
   /**
    * Return a provider capable of supporting the requested service.
    * @param service the service descriptor
    * @return a component matching the requested service
    * @exception ServiceNotFoundException if no component could found
    * @exception RemoteException if a remote I/O occurs
    */
    Provider lookup( Service service ) throws ServiceNotFoundException, RemoteException;

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
    
   /**
    * Invoke an operation on the instance.
    * @param method the operation name
    * @param args operation arguments
    * @return the result of the operation invocation
    * @exception UnknownOperationException if the supplied key does not map to an available operation
    * @exception InvocationTargetException if an invocation error occurs
    * @exception IllegalStateException if the component state does not expose the operation
    * @exception RemoteException if a remote I/O error occurs
    */
    Object invoke( String method, Object[] args ) 
      throws UnknownOperationException, InvocationTargetException, 
      IllegalStateException, RemoteException;

}
