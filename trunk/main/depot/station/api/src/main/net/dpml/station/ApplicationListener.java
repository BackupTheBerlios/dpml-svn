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

package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A ApplicationListener enabled client application to receive notification
 * of high-level application state changes.
 */
public interface ApplicationListener extends EventListener, Remote
{
   /**
    * Notify the listener of an application state change.
    * @param event the application event
    * @exception RemoteException if a remote exception occurs
    */
    void applicationStateChanged( ApplicationEvent event ) throws RemoteException;

}
