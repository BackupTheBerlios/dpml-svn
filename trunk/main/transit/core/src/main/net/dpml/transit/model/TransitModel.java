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
 * The TransitModel is an interface implemented by objects that
 * maintain an active Tranist configuration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface TransitModel extends Remote
{
   /**
    * Return the proxy model.
    * @return the proxy model
    * @exception RemoteException if a remote exception occurs
    */
    ProxyModel getProxyModel() throws RemoteException;

   /**
    * Return the cache model.
    * @return the cache model
    * @exception RemoteException if a remote exception occurs
    */
    CacheModel getCacheModel() throws RemoteException;
}
