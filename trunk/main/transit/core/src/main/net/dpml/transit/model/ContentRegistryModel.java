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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A RegistryModel maintains information about the configuration of the Transit
 * content registry subsystem.  Instances of RegistryModel shall be supplied to 
 * registry implementations as constructor arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ContentRegistryModel extends Remote
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
