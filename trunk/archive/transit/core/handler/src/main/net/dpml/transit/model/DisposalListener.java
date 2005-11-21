/*
 * Copyright 2005 Stephen McConnell
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
 * A DisposalListener interface provides support for notification of 
 * an imminent disposal and post disposal notification.  Implementations
 * may veto disposal by raising the VetoDisposalException.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface DisposalListener extends EventListener, Remote
{
   /**
    * Notify the listener of the imminent disposal of a model.
    * @param event the dsposal event
    * @exception VetoDisposalException thrown by an implementation whishing to veto the disposal
    * @exception RemoteException if a remote exception occurs
    */
    void disposing( DisposalEvent event ) throws VetoDisposalException, RemoteException;

   /**
    * Notify a listener of the disposal of a model.
    * @param event the disposal event
    * @exception RemoteException if a remote exception occurs
    */
    void disposed( DisposalEvent event ) throws RemoteException;
}
