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

import java.io.File;
import java.net.URI;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * A CacheModel maintains information about the configuration of the Transit
 * cache subsystem.  Instances of CacheModel shall be supplied to cache handler
 * implementations as constructor arguments.
 */
public interface CacheModel extends CodeBaseModel, Disposable
{
   /**
    * The property key used to identify the cache location when configuring 
    * a transit profile via an authorative url.
    */
    static final String CACHE_LOCATION_KEY = "dpml.transit.cache.location";

   /**
    * The property key used to identify the cache layout model id when configuring 
    * a transit profile via an authorative url.
    */
    static final String CACHE_LAYOUT_KEY = "dpml.transit.cache.layout";

   /**
    * Return the directory path to be used by the cache handler.
    * @return the cache directory path.
    */
    String getCacheDirectoryPath() throws RemoteException;

   /**
    * Return the directory to be used by the cache handler as the cache directory.
    * @return the cache directory.
    */
    File getCacheDirectory() throws RemoteException;

   /**
    * Update the value the local cache directory path.
    *
    * @param path the cache directory path
    */
    void setCacheDirectoryPath( final String path ) throws RemoteException;

   /**
    * Return the array of hosts configured for the cache.
    * @return the host director array
    */
    HostModel[] getHostModels() throws RemoteException;

   /**
    * Return an identified host model.
    * @param id the host identifier
    * @return the host model
    * @exception UnknownKeyException if the requested host id is unknown
    */
    HostModel getHostModel( String id ) throws UnknownKeyException, RemoteException;

   /**
    * Add a cache listener to the model.
    * @param listener the listener to add
    */
    void addCacheListener( CacheListener listener ) throws RemoteException;

   /**
    * Remove a cache listener from the model.
    * @param listener the listener to remove
    */
    void removeCacheListener( CacheListener listener ) throws RemoteException;

   /**
    * Return the cache layout model.
    * @return the layout model
    */
    LayoutModel getLayoutModel() throws RemoteException;

   /**
    * Return the layout registry model.
    * @return the layout registry model
    */
    LayoutRegistryModel getLayoutRegistryModel() throws RemoteException;

   /**
    * Add a new host model to the cache model.
    * @param id the host identifier
    * @exception DuplicateKeyException if a host of the same identifier already exists
    * @exception UnknownKeyException if a host references an unknown layout key
    */
    void addHostModel( String id ) 
     throws DuplicateKeyException, UnknownKeyException, RemoteException, MalformedURLException;

   /**
    * Add a new host model to the cache model.
    * @param model the host model to add to the cache model
    * @exception DuplicateKeyException if a host of the same identifier already exists
    */
    void addHostModel( HostModel model ) throws DuplicateKeyException, RemoteException;

   /**
    * Remove a host from the cache model.
    * @param model the host model to remove
    */
    void removeHostModel( HostModel model ) throws RemoteException;

}
