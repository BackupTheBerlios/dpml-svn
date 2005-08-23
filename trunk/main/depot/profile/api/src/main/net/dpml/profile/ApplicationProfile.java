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

package net.dpml.profile;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Date;

import net.dpml.transit.model.CodeBaseModel;
import net.dpml.transit.model.Disposable;

/**
 * The ApplicationProfile interface describes an application deployment
 * scenario.  This interface suppliments a codebase uri with constructor
 * paramters and policies related enabled status as server status.
 */
public interface ApplicationProfile extends CodeBaseModel, Disposable
{
    String getID() throws RemoteException;

    String getTitle() throws RemoteException;

    void setTitle( String title ) throws RemoteException;

    Properties getSystemProperties() throws RemoteException;

    void setSystemProperties( Properties properties ) throws RemoteException;

   /**
    * Return the enabled status of the application. The default status
    * for an application is 'disabled'.  Automatic deloyment is achived
    * by setting the enabled status to TRUE.
    *
    * @return the enabled status
    * @exception RemoteException if a remote exception occurs
    */
    boolean isEnabled() throws RemoteException;

   /**
    * Set the enabled status of the application to the supplied value.
    * @param value the enabled status
    * @exception RemoteException if a remote exception occurs
    */
    void setEnabled( boolean value ) throws RemoteException;

   /**
    * Returns the server status of the application.  Applications that 
    * return false are viewed as executable command handlers and will be
    * decommmissioned following activation.  The default behaviour is 
    * server mode enabled.
    *
    * @return TRUE is this application is a server
    * @exception RemoteException if a remote exception occurs
    */
    boolean isaServer() throws RemoteException;

   /**
    * Set the server mode to the supplied value.
    * @param policy TRUE if this application is a server else FALSE
    * @exception RemoteException if a remote exception occurs
    */
    void setServerMode( boolean policy ) throws RemoteException;

   /**
    * Return the array of constructor parameters for the application.
    * @return the declare constructor parameters
    * @exception RemoteException if a remote exception occurs
    */
    Parameter[] getParameters() throws RemoteException;

   /**
    * Set the constructor parameters for the application.
    * @param params an array of constructor parameters
    * @exception RemoteException if a remote exception occurs
    */
    void setParameters( Parameter[] params ) throws RemoteException;

}


