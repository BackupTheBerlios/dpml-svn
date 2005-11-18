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
import java.net.URI;

/**
 * A RegistryModel maintains information about the configuration of the Transit
 * content registry subsystem.  Instances of RegistryModel shall be supplied to 
 * registry implementations as constructor arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ContentRegistryModel extends CodeBaseModel
{
   /**
    * Return an array of content models currently assigned to the registry.
    * @return the content model array
    * @exception RemoteException if a remote exception occurs
    */
    ContentModel[] getContentModels() throws RemoteException;

   /**
    * Return a content model matching the supplied type.
    *
    * @param type the content model type
    * @return the content model
    * @exception UnknownKeyException if the content model type is unknown
    * @exception RemoteException if a remote exception occurs
    */
    ContentModel getContentModel( String type ) throws UnknownKeyException, RemoteException;

   /**
    * Create a new content model for the supplied type.
    * @param type the content model type
    * @exception DuplicateKeyException if a content model already exists for the supplied type
    * @exception RemoteException if a remote exception occurs
    */
    void addContentModel( String type ) throws DuplicateKeyException, RemoteException;

   /**
    * Create a new content model for the supplied type using a supplied title and codebase uri.
    * @param type the content model type
    * @param title the content model title
    * @param uri the content model codebase uri
    * @exception DuplicateKeyException if a content model already exists for the supplied type
    * @exception RemoteException if a remote exception occurs
    */
    void addContentModel( String type, String title, URI uri ) 
      throws DuplicateKeyException, RemoteException;

   /**
    * Add a new content model to the content registry.
    * @param model the content model to add
    * @exception DuplicateKeyException if a content model already exists for the type
    *   declared by the supplied model
    * @exception RemoteException if a remote exception occurs
    */
    void addContentModel( ContentModel model ) throws DuplicateKeyException, RemoteException;

   /**
    * Remove a content model from the registry.
    * @param model the model to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeContentModel( ContentModel model ) throws RemoteException;

   /**
    * Add a content registry change listener.
    * @param listener the registry change listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addRegistryListener( ContentRegistryListener listener ) throws RemoteException;

   /**
    * Remove a registry change listener.
    * @param listener the registry change listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeRegistryListener( ContentRegistryListener listener ) throws RemoteException;

}
