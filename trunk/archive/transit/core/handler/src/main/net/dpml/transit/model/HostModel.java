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
import java.net.MalformedURLException; 
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
public interface HostModel extends CodeBaseModel, Disposable
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
    * Return TRUE if this is a bootstrap host. Bootstrap hosts shall be 
    * provided such that they independent of the Transit respository 
    * service.
    * @return the bootstrap flag
    * @exception RemoteException if a remote exception occurs
    */
    boolean isBootstrap() throws RemoteException;

   /**
    * Return the name of the resource host.
    * @return the host name
    * @exception RemoteException if a remote exception occurs
    */
    String getHostName() throws RemoteException;

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
    public PasswordAuthentication getAuthentication() throws RemoteException; 

   /**
    * Return the host request identifier.
    * @return the identifier
    * @exception RemoteException if a remote exception occurs
    */
    public RequestIdentifier getRequestIdentifier() throws RemoteException; 

   /**
    * Return the layout strategy model.
    * @return the layout model
    * @exception RemoteException if a remote exception occurs
    */
    LayoutModel getLayoutModel() throws RemoteException;

   /**
    * Set the human readable name of the host to the supplied value.
    *
    * @param name the human readable name
    * @exception RemoteException if a remote exception occurs
    */
    void setName( String name ) throws RemoteException;

   /**
    * Set the host priority to the supplied value.
    * @param priority the host priority
    * @exception RemoteException if a remote exception occurs
    */
    void setPriority( int priority ) throws RemoteException;

   /**
    * Set the layout model assigned to the host.
    *
    * @param layout the layout model to assign
    * @exception BootstrapException if the host model is a bootstrap host and 
    *   the assigned layout model is not a bootstrap layout model
    * @exception RemoteException if a remote exception occurs
    */
    void setLayoutModel( LayoutModel layout ) throws BootstrapException, RemoteException;

   /**
    * Update the state of the host model.
    *
    * @param base the host base url path
    * @param index the host content index
    * @param enabled the enabled status of the host
    * @param trusted the trusted status of the host
    * @param layout the assigned host layout identifier
    * @param auth a possibly null host authentication username and password
    * @param scheme the host security scheme
    * @param prompt the security prompt raised by the host
    * @exception UnknownKeyException if the layout id is unknown
    * @exception MalformedURLException if the host base url path is malformed
    * @exception BootstrapException if the host is a bootstrap host and a 
    *   non-bootstrap layout is assigned
    * @exception RemoteException if a remote exception occurs
    */
    void update( 
      String base, String index, boolean enabled, boolean trusted, String layout, 
      PasswordAuthentication auth, String scheme, String prompt ) 
      throws BootstrapException, UnknownKeyException, MalformedURLException, RemoteException;

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

