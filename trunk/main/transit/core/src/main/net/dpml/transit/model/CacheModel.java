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
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A CacheModel maintains information about the configuration of the Transit
 * cache subsystem.  Instances of CacheModel shall be supplied to cache handler
 * implementations as constructor arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface CacheModel extends Remote
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
    * @exception RemoteException if a remote exception occurs
    */
    String getCacheDirectoryPath() throws RemoteException;

   /**
    * Return the directory to be used by the cache handler as the cache directory.
    * @return the cache directory.
    * @exception RemoteException if a remote exception occurs
    */
    File getCacheDirectory() throws RemoteException;

   /**
    * Return the array of hosts configured for the cache.
    * @return the host model array
    * @exception RemoteException if a remote exception occurs
    */
    HostModel[] getHostModels() throws RemoteException;

   /**
    * Return an identified host model.
    * @param id the host identifier
    * @return the host model
    * @exception UnknownKeyException if the requested host id is unknown
    * @exception RemoteException if a remote exception occurs
    */
    HostModel getHostModel( String id ) throws UnknownKeyException, RemoteException;

   /**
    * Add a cache listener to the model.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addCacheListener( CacheListener listener ) throws RemoteException;

   /**
    * Remove a cache listener from the model.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeCacheListener( CacheListener listener ) throws RemoteException;

   /**
    * Return the cache layout model.
    * @return the layout model
    * @exception RemoteException if a remote exception occurs
    */
    LayoutModel getLayoutModel() throws RemoteException;

   /**
    * Return the layout registry model.
    * @return the layout registry model
    * @exception RemoteException if a remote exception occurs
    */
    LayoutRegistryModel getLayoutRegistryModel() throws RemoteException;
    
   /**
    * Return the model maintaining configuration information about
    * the content registry.
    *
    * @return the content model
    * @exception RemoteException if a remote exception occurs
    */
    ContentRegistryModel getContentRegistryModel() throws RemoteException;

}
