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
 * A listener for events related to the Transit cache subsystem.
 */
public interface CacheListener extends EventListener, Remote
{
   /**
    * Notify the listener of a change to the cache directory.
    * @param event the cache directory change event
    */
    void cacheDirectoryChanged( CacheDirectoryChangeEvent event ) throws RemoteException;

   /**
    * Notify the listener of the addition of a new host.
    * @param event the host added event
    */
    void hostAdded( CacheEvent event ) throws RemoteException;

   /**
    * Notify the listener of the removal of a host.
    * @param event the host removed event
    */
    void hostRemoved( CacheEvent event ) throws RemoteException;

}
