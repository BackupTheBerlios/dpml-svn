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

package net.dpml.depot.profile;

import java.rmi.RemoteException;

import net.dpml.transit.model.Model;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * 
 */
public interface DepotProfile extends Model
{
    int getApplicationProfileCount() throws RemoteException;

    int getActivationGroupProfileCount() throws RemoteException;

    ApplicationProfile[] getApplicationProfiles() throws RemoteException;

    ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException;

    void addActivationGroupProfile( ActivationGroupProfile profile ) 
      throws DuplicateKeyException, RemoteException;

    void removeActivationGroupProfile( ActivationGroupProfile profile ) throws RemoteException;

    ActivationGroupProfile[] getActivationGroupProfiles() throws RemoteException;

    ActivationGroupProfile getActivationGroupProfile( String key ) throws UnknownKeyException, RemoteException;

   /**
    * Add a depot content change listener.
    * @param listener the registry change listener to add
    */
    void addDepotListener( DepotListener listener ) throws RemoteException;

   /**
    * Remove a depot content change listener.
    * @param listener the registry change listener to remove
    */
    void removeDepotListener( DepotListener listener ) throws RemoteException;

    void addApplicationProfile( ApplicationProfile profile ) 
      throws DuplicateKeyException, RemoteException;

    void removeApplicationProfile( ApplicationProfile profile ) throws RemoteException;

}

