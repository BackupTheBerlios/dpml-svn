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
 * Interface implemented by objects that are interested in changes
 * to a layout registry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface LayoutRegistryListener extends EventListener, Remote
{
   /**
    * Notify all listeners of the addition of a layout model.
    * @param event the layout event
    * @exception RemoteException if a remote exception occurs
    */
    void layoutAdded( LayoutRegistryEvent event ) throws RemoteException;

   /**
    * Notify all listeners of the removal of a layout model.
    * @param event the layout event
    * @exception RemoteException if a remote exception occurs
    */
    void layoutRemoved( LayoutRegistryEvent event ) throws RemoteException;

}
