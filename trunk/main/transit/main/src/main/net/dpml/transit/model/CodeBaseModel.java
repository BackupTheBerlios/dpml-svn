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
 * A CodeBaseModel maintains information about the configuration of 
 * a pluggable subsystem.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface CodeBaseModel extends Model
{
   /**
    * Return the immutable model identifier.
    * @return the resolver identifier
    * @exception RemoteException if a remote exception occurs
    */
    String getID() throws RemoteException;

   /**
    * Return the uri of the plugin to be used for the subsystem.
    * @return the codebase plugin uri
    * @exception RemoteException if a remote exception occurs
    */
    URI getCodeBaseURI() throws RemoteException;

   /**
    * Set the plugin uri value.
    * @param uri the plugin uri
    * @exception RemoteException if a remote exception occurs
    */
    void setCodeBaseURI( URI uri ) throws RemoteException;

   /**
    * Add a codebase listener to the model.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addCodeBaseListener( CodeBaseListener listener ) throws RemoteException;

   /**
    * Remove a codebase listener from the model.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeCodeBaseListener( CodeBaseListener listener ) throws RemoteException;

   /**
    * Return the array of codebase parameter values.
    *
    * @return the parameter value array
    * @exception RemoteException if a remote exception occurs
    */
    Value[] getParameters() throws RemoteException;

   /**
    * Set the array of values assigned to the codebase model for use
    * as plugin constructor parameter arguments.
    *
    * @param values the array of values
    * @exception RemoteException if a remote exception occurs
    */
    void setParameters( Value[] values ) throws RemoteException;

}
