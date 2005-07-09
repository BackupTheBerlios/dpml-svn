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
 * The TransitRegistryModel is an interface implemented by objects that
 * manage a collection of TransitModel instances.
 */
public interface TransitRegistryModel extends Model
{
   /**
    * Return the number of registered transit profiles.
    * @return the number of registered models
    */
    int getTransitModelCount() throws RemoteException;

   /**
    * Add a registry change listener.
    * @param listener the registry change listener to add
    */
    void addTransitRegistryListener( TransitRegistryListener listener ) throws RemoteException;

   /**
    * Remove a registry change listener.
    * @param listener the registry change listener to remove
    */
    void removeTransitRegistryListener( TransitRegistryListener listener ) throws RemoteException;

    void addTransitModel( String id ) throws DuplicateKeyException, RemoteException;

    void addTransitModel( TransitModel model ) throws DuplicateKeyException, RemoteException;

    TransitModel[] getTransitModels() throws RemoteException;

    TransitModel getTransitModel( String id ) throws UnknownKeyException, RemoteException;

    void removeTransitModel( TransitModel model ) throws ModelReferenceException, RemoteException;
}
