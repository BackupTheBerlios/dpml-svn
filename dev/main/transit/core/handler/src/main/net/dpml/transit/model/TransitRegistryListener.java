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

import java.util.EventListener;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A RegistryListener maintains information about the configuration of a Transit
 * content management system. An implementation of this interface would be supplied 
 * a ContentRegistry implementation.
 */
public interface TransitRegistryListener extends EventListener, Remote
{
   /**
    * Notify all listeners of the addition of a model to the registry.
    * @param event the registry event
    */
    void modelAdded( TransitRegistryEvent event ) throws RemoteException;

   /**
    * Notify all listeners of the removal of a model from the registry.
    * @param event the registry event
    */
    void modelRemoved( TransitRegistryEvent event ) throws RemoteException;

}
