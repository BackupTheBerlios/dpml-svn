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

package net.dpml.transit.model;

import java.util.EventListener;
import java.rmi.Remote;
import java.rmi.RemoteException;

/** 
 * The HostListener is an interface implmented by resource host
 * implementation.  The interface declares host change notification
 * methods that is invoked by a host model following one or more changes 
 * to the host configuration.
 */
public interface HostListener extends EventListener, Remote
{
   /**
    * Notify a consumer of the change to the host name.
    * @param event the host name change event
    */
    void nameChanged( HostNameEvent event ) throws RemoteException;

   /**
    * Notify a consumer of an aggregated set of changes concerning the 
    * base url, index, request identifier and/or authentication.
    * @param event the host change event
    */
    void hostChanged( HostChangeEvent event ) throws RemoteException;

   /**
    * Notify a consumer of a change to the host priority.
    * @param event the host priority event
    */
    void priorityChanged( HostPriorityEvent event ) throws RemoteException;

   /**
    * Notify a consumer of a change to the host layout.
    * @param event the host layout change event
    */
    void layoutChanged( HostLayoutEvent event ) throws RemoteException;

}

