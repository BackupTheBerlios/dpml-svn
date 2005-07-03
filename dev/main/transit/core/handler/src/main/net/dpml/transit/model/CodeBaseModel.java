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

import java.net.URI;
import java.rmi.RemoteException;

/**
 * A PluginModel maintains information about the configuration of 
 * a pluggable subsystem.
 */
public interface CodeBaseModel extends Model
{
   /**
    * Return the uri of the plugin to be used for cache management.
    * @return the cache handler plugin uri
    */
    URI getCodeBaseURI() throws RemoteException;

   /**
    * Set the plugin uri value.
    * @param uri the plugin uri
    */
    void setCodeBaseURI( URI uri ) throws RemoteException;

   /**
    * Add a codebase listener to the model.
    * @param listener the listener to add
    */
    void addCodeBaseListener( CodeBaseListener listener ) throws RemoteException;

   /**
    * Remove a codebase listener from the model.
    * @param listener the listener to remove
    */
    void removeCodeBaseListener( CodeBaseListener listener ) throws RemoteException;

}
