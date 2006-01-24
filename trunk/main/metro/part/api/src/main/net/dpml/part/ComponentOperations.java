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

package net.dpml.part;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;

import net.dpml.part.remote.Component;
import net.dpml.part.remote.Service;
import net.dpml.part.remote.Provider;

/**
 * The ComponentOperations represents the set of operations shared between 
 * local and remote component users.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentOperations
{
   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service descriptor
    * @return a component matching the requested service
    * @exception ServiceNotFoundException if no component could found
    * @exception RemoteException if a remote I/O occurs
    */
    Component lookup( Service service ) throws ServiceNotFoundException, RemoteException;
    
   /**
    * Return true if this handler is a candidate for the supplied service definition.
    * @param service the service descriptor
    * @return true if this is a candidate
    * @exception RemoteException if a remote exception occurs
    */
    boolean isaCandidate( Service service ) throws RemoteException;
    
   /**
    * Initiate activation of a runtime handler.
    * @exception ControlException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation source exception occured
    * @exception RemoteException if a remote exception occurs
    */
    void activate() throws ControlException, InvocationTargetException, RemoteException;
    
   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    * @exception RemoteException if a remote exception occurs
    */
    boolean isActive() throws RemoteException;
    
   /**
    * Return the number of instances currently under management.
    * @return the instance count.
    * @exception RemoteException if a remote exception occurs
    */
    int size() throws RemoteException;
    
   /**
    * Return a reference to a instance of the component handled by the handler.
    * @return the instance holder
    * @exception InvocationTargetException if the component instantiation process 
    *  is on demand and an target invocation error occurs
    * @exception ControlException if the component could not be established due to a controller 
    *  related error
    * @exception RemoteException if a remote exception occurs
    */
    Provider getProvider() throws ControlException, InvocationTargetException, RemoteException;
    
   /**
    * Deactivate the handler.
    * @exception RemoteException if a remote exception occurs
    */
    void deactivate() throws RemoteException;
}

