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

import java.rmi.RemoteException;


/**
 * The TransitModel is an interface implemented by objects that
 * manage the configuration of a running transit system.
 */
public interface TransitModel extends Model, Disposable
{
   /**
    * Return the model identifier.
    * @return the model id
    */
    String getID() throws RemoteException;

   /**
    * Return the proxy model.
    * @return the proxy model
    */
    ProxyModel getProxyModel() throws RemoteException;

   /**
    * Return the cache model.
    * @return the cache director
    */
    CacheModel getCacheModel() throws RemoteException;

   /**
    * Return the model maintaining configuration information about
    * the content registry.
    *
    * @return the content model
    */
    ContentRegistryModel getContentRegistryModel() throws RemoteException;

   /**
    * Return the model maintaining configuration information about
    * the repository service.
    *
    * @return the repository model
    */
    RepositoryModel getRepositoryModel() throws RemoteException;

}
