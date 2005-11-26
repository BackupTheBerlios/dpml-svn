/*
 * Copyright 2005 Stephen J. McConnell.
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
 * The HostModel interface is implemented by objects that control the
 * the configuration of resource host implementations.  An instance of an 
 * implementation of HostModel may be passed as a constructor argument 
 * to a resource host implmentation. Implementation shall maintain 
 * synchronization via change events raised by implementations of this 
 * interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface HostModel extends Model
{
   /**
    * HTTP port number.
    */
    static final int HTTP_PORT = 80;

   /**
    * FTP port number.
    */
    static final int FTP_PORT = 21;

   /**
    * HTTPS port number.
    */
    static final int HTTPS_PORT = 443;

   /**
    * Return an immutable host identifier.  The host identifier shall be 
    * guranteed to be unique and constant for the life of the model.
    * @return the host id
    * @exception RemoteException if a remote exception occurs
    */
    String getID() throws RemoteException;

   /**
    * Return the host priority.
    * @return the host priority setting
    * @exception RemoteException if a remote exception occurs
    */
    int getPriority() throws RemoteException;

   /**
    * Return the host base path.
    * @return the base path
    * @exception RemoteException if a remote exception occurs
    */
    String getBasePath() throws RemoteException;

   /**
    * Return the host base url.
    * @return the base url
    * @exception RemoteException if a remote exception occurs
    */
    URL getBaseURL() throws RemoteException;

   /**
    * Return index path.
    * @return the index path
    * @exception RemoteException if a remote exception occurs
    */
    String getIndexPath() throws RemoteException;

   /**
    * Return index url.
    * @return the index url
    * @exception RemoteException if a remote exception occurs
    */
    URL getIndexURL() throws RemoteException;

   /**
    * Return the enabled status of the host.
    * @return TRUE if enabled 
    * @exception RemoteException if a remote exception occurs
    */
    boolean getEnabled() throws RemoteException;

   /**
    * Return the trusted status.
    * @return TRUE if trusted 
    * @exception RemoteException if a remote exception occurs
    */
    boolean getTrusted() throws RemoteException;
   
   /**
    * Return the host password authentication credentials.
    * @return the password authentication credentials
    * @exception RemoteException if a remote exception occurs
    */
    PasswordAuthentication getAuthentication() throws RemoteException; 

   /**
    * Return the host request identifier.
    * @return the identifier
    * @exception RemoteException if a remote exception occurs
    */
    RequestIdentifier getRequestIdentifier() throws RemoteException; 

   /**
    * Return the layout strategy model.
    * @return the layout model
    * @exception RemoteException if a remote exception occurs
    */
    LayoutModel getLayoutModel() throws RemoteException;

   /**
    * Add a host change listener to the director.
    * @param listener the host change listener to add
    * @exception RemoteException if a remote exception occurs
    */
    void addHostListener( HostListener listener ) throws RemoteException;

   /**
    * Remove a host change listener from the director.
    * @param listener the host change listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    void removeHostListener( HostListener listener ) throws RemoteException;

}

