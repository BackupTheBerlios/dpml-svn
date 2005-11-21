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
 * A LayoutRegistryModel maintains a collection of layout models.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface LayoutRegistryModel extends CodeBaseModel
{
   /**
    * Return the set of location resolver models.
    * @return the model array.
    * @exception RemoteException if a remote exception occurs
    */
    LayoutModel[] getLayoutModels() throws RemoteException;

   /**
    * Return a layout resolver model matching the supplied id. If the id is unknown
    * an implementation shall return a null value.
    * @param id the layout id
    * @return the layout model
    * @exception UnknownKeyException if the key is unknown
    * @exception RemoteException if a remote exception occurs
    */
    LayoutModel getLayoutModel( String id ) throws UnknownKeyException, RemoteException;

   /**
    * Add a change listener.
    * @param listener the registry change listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addLayoutRegistryListener( LayoutRegistryListener listener ) throws RemoteException;

   /**
    * Remove a change listener.
    * @param listener the registry change listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeLayoutRegistryListener( LayoutRegistryListener listener ) throws RemoteException;

   /**
    * Add a new layout model to the registry.
    * @param id the layout model identity
    * @exception DuplicateKeyException if a layout model of the same id already exists
    * @exception RemoteException if a remote exception occurs
    */
    void addLayoutModel( String id ) throws DuplicateKeyException, RemoteException;

   /**
    * Add a new layout model to the registry.
    * @param model the layout model
    * @exception DuplicateKeyException if a layout model of the same id already exists
    * @exception RemoteException if a remote exception occurs
    */
    void addLayoutModel( LayoutModel model ) throws DuplicateKeyException, RemoteException;

   /**
    * Remove a layout model from the registry.
    * @param model the layout model to be removed
    * @exception ModelReferenceException if the layout is in use
    * @exception RemoteException if a remote exception occurs
    */
    void removeLayoutModel( LayoutModel model ) throws ModelReferenceException, RemoteException;

}

