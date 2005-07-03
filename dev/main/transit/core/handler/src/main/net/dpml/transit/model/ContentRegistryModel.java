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

import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URI;

/**
 * A RegistryModel maintains information about the configuration of the Transit
 * content registry subsystem.  Instances of RegistryModel shall be supplied to 
 * registry implementations as constructor arguments.
 */
public interface ContentRegistryModel extends CodeBaseModel
{
   /**
    * Return an array of content managers currently assigned to the registry.
    * @return the content manager array
    */
    ContentModel[] getContentModels() throws RemoteException;

   /**
    * Return a content model matching the supplied type. If the type is unknown
    * an implementation shall return a null value.
    *
    * @return the content model
    */
    ContentModel getContentModel( String type ) throws UnknownKeyException, RemoteException;

    void addContentModel( String type ) throws DuplicateKeyException, RemoteException;

    void addContentModel( String type, String title, URI uri ) 
      throws DuplicateKeyException, RemoteException;

    void addContentModel( ContentModel model ) throws DuplicateKeyException, RemoteException;

    void removeContentModel( ContentModel model ) throws RemoteException;

   /**
    * Add a regstry change listener.
    * @param listener the registry change listener to add
    */
    void addRegistryListener( ContentRegistryListener listener ) throws RemoteException;

   /**
    * Remove a regstry change listener.
    * @param listener the registry change listener to remove
    */
    void removeRegistryListener( ContentRegistryListener listener ) throws RemoteException;


}
