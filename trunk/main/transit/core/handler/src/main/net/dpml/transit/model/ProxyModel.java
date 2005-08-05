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

import java.net.URL;
import java.net.PasswordAuthentication; 
import java.rmi.RemoteException; 

/**
 * The ProxyModel is an interface implemented by objects that
 * manage the configuration of transit proxy settings.
 */
public interface ProxyModel extends Model
{
   /**
    * Return the proxy host url.
    *
    * @return the proxy host (possibly null)
    * @exception RemoteException if a remote exception occurs
    */
    public URL getHost() throws RemoteException;

   /**
    * Return the proxy authentication or null if not defined.
    * @return the proxy authentication credentials
    * @exception RemoteException if a remote exception occurs
    */
    PasswordAuthentication getAuthentication() throws RemoteException;

   /**
    * Return the proxy host request identifier.
    * @return the request identifier for the proxy host or null if not defined.
    * @exception RemoteException if a remote exception occurs
    */
    RequestIdentifier getRequestIdentifier() throws RemoteException;

   /**
    * Return the set of excluded hosts as an array.
    * @return the excluded host array
    * @exception RemoteException if a remote exception occurs
    */
    String[] getExcludes() throws RemoteException;

   /**
    * Update the state of the proxy model.
    * @param host the proxy host
    * @param auth the proxy host authentication settings
    * @param excludes the set of roxy excludes
    * @exception RemoteException if a remote exception occurs
    */
    void update( URL host, PasswordAuthentication auth, String[] excludes ) throws RemoteException;

   /**
    * Update the proxy host url.
    * @param host the proxy host
    * @exception RemoteException if a remote exception occurs
    */
    void setHost( URL host ) throws RemoteException;

   /**
    * Update the proxy excludes.
    * @param excludes the proxy excludes
    * @exception RemoteException if a remote exception occurs
    */
    void setExcludes( String[] excludes ) throws RemoteException;

   /**
    * Update the proxy authentication settings.
    * @param auth the proxy authentication settings
    * @exception RemoteException if a remote exception occurs
    */
    void setAuthentication( PasswordAuthentication auth ) throws RemoteException;

   /**
    * Add a proxy listener to the model.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addProxyListener( ProxyListener listener ) throws RemoteException;

   /**
    * Remove a proxy listener from the model.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeProxyListener( ProxyListener listener ) throws RemoteException;
   
}
