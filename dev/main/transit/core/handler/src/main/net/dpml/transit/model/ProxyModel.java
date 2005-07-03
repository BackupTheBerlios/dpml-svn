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

import net.dpml.transit.network.RequestIdentifier;

/**
 * The ProxyModel is an interface implemented by objects that
 * manage the configuration of a running transit system.
 */
public interface ProxyModel extends Model
{
   /**
    * Return the proxy host url.
    *
    * @return the proxy host (possibly null)
    */
    public URL getHost() throws RemoteException;

   /**
    * Return the proxy authentication or null if not defined.
    * @return the proxy authentication credentials
    */
    PasswordAuthentication getAuthentication() throws RemoteException;

   /**
    * Return the proxy host request identifier.
    * @return the request identifier for the proxy host or null if not defined.
    */
    RequestIdentifier getRequestIdentifier() throws RemoteException;

   /**
    * Return the set of excluded hosts as an array.
    * @return the excluded host array
    */
    String[] getExcludes() throws RemoteException;

    void update( URL host, PasswordAuthentication auth, String[] excludes ) throws RemoteException;

    void setHost( URL host ) throws RemoteException;

    void setExcludes( String[] excludes ) throws RemoteException;

    void setAuthentication( PasswordAuthentication auth ) throws RemoteException;

   /**
    * Add a proxy listener to the model.
    * @param listener the listener to add
    */
    void addProxyListener( ProxyListener listener ) throws RemoteException;

   /**
    * Remove a proxy listener from the model.
    * @param listener the listener to remove
    */
    void removeProxyListener( ProxyListener listener ) throws RemoteException;
   
}
