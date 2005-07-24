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

   /**
    * Add a new Transit profile to the registry.
    * @param id the identifier of the new profile
    * @exception DuplicateKeyException if a profile with the same id is already registered
    */
    void addTransitModel( String id ) throws DuplicateKeyException, RemoteException;

   /**
    * Add a new Transit profile to the registry.
    * @param model the profile to add
    * @exception DuplicateKeyException if a profile with the same id as the 
    *    id declared by the supplied model is already registered
    */
    void addTransitModel( TransitModel model ) throws DuplicateKeyException, RemoteException;

   /**
    * Return the set of transit models in the registry.
    * @return the model array
    */
    TransitModel[] getTransitModels() throws RemoteException;

   /**
    * Return a transit profile matching the supplied model identifier.
    * @param id the model identifier
    * @return the transit model
    */
    TransitModel getTransitModel( String id ) throws UnknownKeyException, RemoteException;

   /**
    * Remove a transit model from the registry.
    * @param model the model to remove
    * @exception ModelReferenceException if the model is in use
    */
    void removeTransitModel( TransitModel model ) throws ModelReferenceException, RemoteException;
}
