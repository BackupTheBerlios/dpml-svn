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

import java.net.URI;
import java.rmi.RemoteException;

import net.dpml.transit.model.Model;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Registry of application profiles.
 */
public interface ApplicationRegistry extends Model
{
   /**
    * Return the number of application profiles in the registry.
    * @return the application profile count
    * @exception RemoteException if a transport error occurs
    */
    int getApplicationProfileCount() throws RemoteException;

   /**
    * Return an array of all profiles in the registry.
    * @return the application profiles
    * @exception RemoteException if a transport error occurs
    */
    ApplicationProfile[] getApplicationProfiles() throws RemoteException;

   /**
    * Create an return a new unnamed application profile.
    * @param codebase the application codebase uri
    * @return the application profile
    * @exception RemoteException if a transport error occurs
    */
    ApplicationProfile createAnonymousApplicationProfile( URI codebase ) throws RemoteException;

   /**
    * Retrieve an application profile.
    * @param key the application profile key
    * @return the application profile
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a transport error occurs
    */
    ApplicationProfile getApplicationProfile( String key ) throws UnknownKeyException, RemoteException;

   /**
    * Add an application profile to the registry.
    * @param profile the application profile to add to the registry
    * @exception DuplicateKeyException if the profile key is already assigned
    * @exception RemoteException if a transport error occurs
    */
    void addApplicationProfile( ApplicationProfile profile ) 
      throws DuplicateKeyException, RemoteException;

   /**
    * Remove an application profile from the registry.
    * @param profile the application profile to remove 
    * @exception RemoteException if a transport error occurs
    */
    void removeApplicationProfile( ApplicationProfile profile ) throws RemoteException;

   /**
    * Add a registry change listener.
    * @param listener the registry change listener to add
    * @exception RemoteException if a transport error occurs
    */
    void addRegistryListener( RegistryListener listener ) throws RemoteException;

   /**
    * Remove a registry change listener.
    * @param listener the registry change listener to remove
    * @exception RemoteException if a transport error occurs
    */
    void removeRegistryListener( RegistryListener listener ) throws RemoteException;

}

