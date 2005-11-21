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

import java.rmi.RemoteException;

/**
 * Interface implementated by disposable models.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Disposable
{
   /**
    * Add a disposal listener to the model.
    * @param listener the disposal listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addDisposalListener( DisposalListener listener ) throws RemoteException;

   /**
    * Remove a disposal listener from the model.
    * @param listener the disposal listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeDisposalListener( DisposalListener listener ) throws RemoteException;

   /**
    * Dispose of the model.
    * @exception RemoteException if a remote exception occurs
    */
    void dispose() throws RemoteException;

}
