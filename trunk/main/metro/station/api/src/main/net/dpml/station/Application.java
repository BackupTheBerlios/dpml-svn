/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.transit.PID;

import net.dpml.station.info.ApplicationDescriptor;

/**
 * Application process controller.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Application extends Remote
{
   /**
    * The default startup timeout in seconds.
    */
    public int DEFAULT_STARTUP_TIMEOUT = 6;

   /**
    * The default shutdown timeout in seconds.
    */
    public int DEFAULT_SHUTDOWN_TIMEOUT = 6;

   /**
    * Return the process identifier of the process within which the 
    * application is running.
    * @return the pid
    * @exception RemoteException if a rmote error occurs
    */
    PID getPID() throws RemoteException;

   /**
    * Return the profile associated with this application 
    * @return the application profile
    * @exception RemoteException if a remote error occurs
    */
    ApplicationDescriptor getApplicationDescriptor() throws RemoteException;

   /**
    * Return the current deployment state of the process.
    * @return the current process state
    * @exception RemoteException if a remote error occurs
    */
    ProcessState getState() throws RemoteException;

   /**
    * Start the application.
    * @exception RemoteException if a remote error occurs
    */
    void start() throws RemoteException;

   /**
    * Stop the application.
    * @exception RemoteException if a rmote error occurs
    */
    void stop() throws RemoteException;

   /**
    * Restart the application.
    * @exception RemoteException if a rmote error occurs
    */
    void restart() throws RemoteException;

   /**
    * Add an application listener.
    * @param listener the listener to add
    * @exception RemoteException if a rmote error occurs
    */
    void addApplicationListener( ApplicationListener listener ) throws RemoteException;
    
   /**
    * Remove an application listener.
    * @param listener the listener to remove
    * @exception RemoteException if a rmote error occurs
    */
    void removeApplicationListener( ApplicationListener listener ) throws RemoteException;
}

