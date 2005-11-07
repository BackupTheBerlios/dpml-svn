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

package net.dpml.profile.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A RegistryListener enabled client application to receive notification
 * of the addition and removale of application profiles from the application
 * registry.
 */
public interface ApplicationProfileListener extends EventListener, Remote
{
   /**
    * Notify the listener of a change to the application title.
    * @param event the application profile event
    * @exception RemoteException if a transport error occurs
    */
    void titleChange( ApplicationProfileEvent event ) throws RemoteException;

   /**
    * Notify a listener of changes to the working directory.
    * @param event the application profile event
    * @exception RemoteException if a transport error occurs
    */
    void workingDirectoryPathChanged( ApplicationProfileEvent event ) throws RemoteException;

   /**
    * Notify a listener of changes to the system property settings.
    * @param event the application profile event
    * @exception RemoteException if a transport error occurs
    */
    void systemPropertiesChanged( ApplicationProfileEvent event ) throws RemoteException;

   /**
    * Notify a listener of changes to the startup timeout settings.
    * @param event the application profile event
    * @exception RemoteException if a transport error occurs
    */
    void startupTimeoutChanged( ApplicationProfileEvent event ) throws RemoteException;

   /**
    * Notify a listener of changes to the shutdown timeout settings.
    * @param event the application profile event
    * @exception RemoteException if a transport error occurs
    */
    void shutdownTimeoutChanged( ApplicationProfileEvent event ) throws RemoteException;

   /**
    * Notify a listener of changes to the startup policy settings.
    * @param event the application profile event
    * @exception RemoteException if a transport error occurs
    */
    void startupPolicyChanged( ApplicationProfileEvent event ) throws RemoteException;
}
