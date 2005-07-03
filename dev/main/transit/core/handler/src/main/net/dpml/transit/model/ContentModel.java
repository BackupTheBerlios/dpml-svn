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
 * A ContentModel maintains information about the configuration
 * of a content handler.  Instances of ContentModel are used as 
 * constructor arguments to content handler plugins.
 */
public interface ContentModel extends CodeBaseModel, Disposable
{
   /**
    * Return the immutable content type identifier.
    * @return the content type
    */
    String getContentType() throws RemoteException;

   /**
    * Returns the human readable name of the content type handler.
    * @return the content type human readable name
    */
    public String getTitle() throws RemoteException;

   /**
    * Set the layout title.
    * @param title the layout title to assign
    */
    void setTitle( String title ) throws RemoteException;

    String getProperty( String key ) throws RemoteException;
   
    String getProperty( String key, String value ) throws RemoteException;

    void setProperty( String key, String value ) throws RemoteException;

    void removeProperty( String key ) throws RemoteException;

   /**
    * Add a content listener to the director.
    * @param listener the listener to add
    */
    void addContentListener( ContentListener listener ) throws RemoteException;

   /**
    * Remove a content listener from the director.
    * @param listener the listener to remove
    */
    void removeContentListener( ContentListener listener ) throws RemoteException;

}
