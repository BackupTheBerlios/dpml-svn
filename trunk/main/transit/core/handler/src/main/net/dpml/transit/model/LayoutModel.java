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
 * A LayoutModel maintains information about the configuration
 * of a location resolver.
 */
public interface LayoutModel extends CodeBaseModel, Disposable
{
   /**
    * Return the immutable model identifier.
    * @return the resolver identifier
    */
    String getID() throws RemoteException;

   /**
    * Return true if this is a bootstrap layout model.
    *
    * @return the bootstrap status of the resolver.
    */
    boolean isBootstrap() throws RemoteException;

   /**
    * Return a possibly null classname.  If the classname is not null the 
    * manager represents a bootstrap layout model. Bootstrap model plugin
    * uris shall return a null value.
    *
    * @return the resolver classname
    */
    String getClassname() throws RemoteException;

   /**
    * Returns the title of the layout model.
    * @return the layout human readable title
    */
    public String getTitle() throws RemoteException;

   /**
    * Set the title of the layout model.
    * @param title the new title
    */
    void setTitle( String title ) throws RemoteException;

   /**
    * Add a layout model listener.
    * @param listener the listener to add
    */
    void addLayoutListener( LayoutListener listener ) throws RemoteException;

   /**
    * Remove a layout model listener from the model.
    * @param listener the listener to remove
    */
    void removeLayoutListener( LayoutListener listener ) throws RemoteException;

}
