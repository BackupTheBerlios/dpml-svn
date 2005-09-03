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

import java.io.Serializable;
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
    StartupPolicy DISABLED = new StartupPolicy( -1, "disabled", "Disabled" );
    StartupPolicy MANUAL = new StartupPolicy( 0, "manual", "Manual" );
    StartupPolicy AUTOMATIC = new StartupPolicy( 1, "automatic", "Automatic" );

   /**
    * The default startup timeout in seconds.
    */
    int DEFAULT_STARTUP_TIMEOUT = 6;

   /**
    * The default shutdown timeout in seconds.
    */
    int DEFAULT_SHUTDOWN_TIMEOUT = 6;

   /**
    * Add a change listener.
    * @param listener the application profile change listener to add
    */
    void addApplicationProfileListener( ApplicationProfileListener listener ) throws RemoteException;

   /**
    * Remove a depot content change listener.
    * @param listener the registry change listener to remove
    */
    void removeApplicationProfileListener(  ApplicationProfileListener listener ) throws RemoteException;

   /**
    * Return the system wide unique application identifier.
    *
    * @return the application identifier
    * @exception RemoteException if a transport error occurs
    */
    String getID() throws RemoteException;

   /**
    * Return the application title.  The value returned is a short one line
    * descriptive name of the application.
    *
    * @return the application title
    * @exception RemoteException if a transport error occurs
    */
    String getTitle() throws RemoteException;

   /**
    * Set the application title.
    * @param title the title to assign to the application
    * @exception RemoteException if a transport error occurs
    */
    void setTitle( String title ) throws RemoteException;

   /**
    * Get the working directory path.  The value returned may include
    * symbolic references to system properties in the form ${name} where 
    * 'name' corresponds to a system property name.
    * 
    * @return the working directory path
    * @exception RemoteException if a transport error occurs
    */
    String getWorkingDirectoryPath() throws RemoteException;

   /**
    * Set the working directory path.  The value supplied may include
    * symbolic references to system properties in the form ${name} where 
    * 'name' corresponds to a system property name.
    * 
    * @param path the working directory path
    * @exception RemoteException if a transport error occurs
    */
    void setWorkingDirectoryPath( String path ) throws RemoteException;

   /**
    * Get the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @return the system properties set
    * @exception RemoteException if a transport error occurs
    */
    Properties getSystemProperties() throws RemoteException;

   /**
    * Set the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @param properties the system properties set
    * @exception RemoteException if a transport error occurs
    */
    void setSystemProperties( Properties properties ) throws RemoteException;

   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    * @exception RemoteException if a transport error occurs
    */    
    int getStartupTimeout() throws RemoteException;

   /**
    * Set the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @param timeout the startup timeout value
    * @exception RemoteException if a transport error occurs
    */
    void setStartupTimeout( int timeout ) throws RemoteException;

   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    * @exception RemoteException if a transport error occurs
    */
    int getShutdownTimeout() throws RemoteException;

   /**
    * Set the duration in seconds to wait for shutdown
    * of the application before considering the application as non-responsive.
    * 
    * @param timeout the shutdown timeout value
    * @exception RemoteException if a transport error occurs
    */
    void setShutdownTimeout( int timeout ) throws RemoteException;

   /**
    * Return the startup policy for the application.  If the policy
    * is DISABLED the application cannot be started.  If the policy 
    * is MANUAL startup may be invoked manually.  If the policy is 
    * AUTOMATIC then startup will be handled by the Station.
    *
    * @return the startup policy
    * @exception RemoteException if a remote exception occurs
    */
    StartupPolicy getStartupPolicy() throws RemoteException;

   /**
    * Set the the statrtup policy to one of DISABLED, MANUAL or AUTOMATIC.
    * @param value the startup policy
    * @exception RemoteException if a remote exception occurs
    */
    void setStartupPolicy( StartupPolicy policy ) throws RemoteException;

   /**
    * Startup policy enumeration.
    */
    public static final class StartupPolicy implements Serializable
    {
        private final int m_index;
        private final String m_label;
        private final String m_key;

       /**
        * Internal constructor.
        * @param index the enumeration index.
        */
        private StartupPolicy( int index, final String key, final String label )
        {
            m_index = index;
            m_label = label;
            m_key = key;
        }

       /**
        * Return the key used to identify this policy instance.
        * @return the key
        */
        public String key()
        {
            return m_key;
        }

       /**
        * Test this policy for equality with the supplied instance.
        * @param other the object to test against
        * @return true if the instances are equivalent
        */
        public boolean equals( Object other )
        {
            if( null == other ) 
            {
                return false;
            }
            else if( other.getClass() == StartupPolicy.class )
            {
                StartupPolicy policy = (StartupPolicy) other;
                return policy.m_index == m_index;
            }
            else
            {
                return false;
            }
        }

       /**
        * Return the hascode for this instance.
        * @return the instance hashcode
        */
        public int hashCode()
        {
            return m_index;
        }

       /**
        * Return the string representation of this instance.
        * @return the string
        */
        public String toString()
        {
            return m_label;
        }
    }

}


