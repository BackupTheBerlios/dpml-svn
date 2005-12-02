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

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.RegistryDescriptor;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Registry of application profiles.
 */
public interface ApplicationRegistry extends Remote
{
   /**
    * The default storage uri.
    */
    static final URI DEFAULT_STORAGE_URI = RegistryDescriptor.DEFAULT_STORAGE_URI;
    
   /**
    * Return the array of application keys.
    * @return the application key array
    * @exception RemoteException if a transport error occurs
    */
    String[] getKeys() throws RemoteException;

   /**
    * Return the number of application descriptors in the registry.
    * @return the application descriptor count
    * @exception RemoteException if a transport error occurs
    */
    int getApplicationDescriptorCount() throws RemoteException;
    
   /**
    * Return an array of all profiles in the registry.
    * @return the application profiles
    * @exception RemoteException if a transport error occurs
    */
    ApplicationDescriptor[] getApplicationDescriptors() throws RemoteException;

   /**
    * Add an application descriptor to the registry.
    * @param key the application key
    * @param descriptor the application descriptor
    * @exception DuplicateKeyException if the key is already assigned
    * @exception RemoteException if a transport error occurs
    */
    void addApplicationDescriptor( String key, ApplicationDescriptor descriptor ) 
      throws DuplicateKeyException, RemoteException;
    
   /**
    * Get an application descriptor matching the supplied key.
    * @param key the application key
    * @return the application descriptor
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a transport error occurs
    */
    ApplicationDescriptor getApplicationDescriptor( String key ) 
      throws UnknownKeyException, RemoteException;
    
   /**
    * Replace an application descriptor within the registry with a supplied descriptor.
    * @param key the application key
    * @param descriptor the updated application descriptor
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a transport error occurs
    */
    void updateApplicationDescriptor( String key, ApplicationDescriptor descriptor ) 
      throws UnknownKeyException, RemoteException;
    
   /**
    * Remove an application descriptor from the registry.
    * @param key the application key
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a transport error occurs
    */
    void removeApplicationDescriptor( String key ) 
      throws UnknownKeyException, RemoteException;

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
    
   /**
    * Request externalization of the registry state. 
    * @exception IOException if an I/O error occurs
    * @exception RemoteException if a transport error occurs
    */
    void flush() throws IOException, RemoteException;
}

