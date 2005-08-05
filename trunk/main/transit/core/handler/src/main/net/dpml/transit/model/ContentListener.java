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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A ContentListener maintains information about the configuration of 
 * content type.
 */
public interface ContentListener extends EventListener, Remote
{
   /**
    * Notify a content model listener of a change to content model title.
    * @param event the content change event
    * @exception RemoteException if a remote exception occurs
    */
    void titleChanged( ContentEvent event ) throws RemoteException;

   /**
    * Notify a regstry listener of a change to a property of the content model.
    * @param event the content property change event
    * @exception RemoteException if a remote exception occurs
    */
    void propertyChanged( PropertyChangeEvent event ) throws RemoteException;

}
