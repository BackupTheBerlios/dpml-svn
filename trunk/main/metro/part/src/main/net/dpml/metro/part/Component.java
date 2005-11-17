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

package net.dpml.metro.part;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import net.dpml.metro.state.State;
import net.dpml.metro.state.StateEvent;
import net.dpml.metro.state.StateListener;

/**
 * The Component represents a remote interface to a runtime component type.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Component extends Remote
{
   /**
    * Return a handler capable of supporting the requested service.
    * @param service the service descriptor
    */
    Component lookup( Service service ) throws ServiceNotFoundException, RemoteException;
    
   /**
    * Return true if this handler is a candidate for the supplied service definition.
    * @return true if this is a candidate
    * @exception RemoteException if a remote exception occurs
    */
    boolean isaCandidate( Service service ) throws RemoteException;
    
   /**
    * Initiate activation of a runtime handler.
    * @exception HandlerException if an activation error occurs
    * @exception InvocationTargetException if the component declares activation on startup
    *    and a implementation source exception occured
    * @exception RemoteException if a remote exception occurs
    */
    void activate() throws HandlerException, InvocationTargetException, RemoteException;
    
   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    * @exception RemoteException if a remote exception occurs
    */
    boolean isActive() throws RemoteException;
    
   /**
    * Return the number of instances currently under management.
    * @return the instance count.
    */
    int size() throws RemoteException;
    
   /**
    * Return a reference to a instance of the component handled by the handler.
    * @return the instance holder
    * @exception InvocationTargetException if the component instantiation process 
    *  is on demand and an target invocation error occurs
    * @exception HandlerException if the component could not be established due to a handler 
    *  related error
    * @exception RemoteException if a remote exception occurs
    */
    Instance getInstance() throws RemoteException, HandlerException, InvocationTargetException;
    
   /**
    * Deactivate the handler.
    * @exception RemoteException if a remote exception occurs
    */
    void deactivate() throws RemoteException;
}

