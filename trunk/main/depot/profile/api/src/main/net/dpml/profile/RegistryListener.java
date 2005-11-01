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

package net.dpml.profile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A RegistryListener enabled client application to receive notification
 * of the addition and removale of application profiles from the application
 * registry.
 */
public interface RegistryListener extends EventListener, Remote
{
   /**
    * Notify the listener of the addition of a new application profile.
    * @param event the registry event
    * @exception RemoteException if a transport error occurs
    */
    void profileAdded( RegistryEvent event ) throws RemoteException;

   /**
    * Notify a listener of the removal of an application profile.
    * @param event the registry event
    * @exception RemoteException if a transport error occurs
    */
    void profileRemoved( RegistryEvent event ) throws RemoteException;

}
