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

package net.dpml.profile.model;

import java.util.Properties;

import net.dpml.transit.store.CodeBaseStorage;
import net.dpml.transit.store.ContentStorage;
import net.dpml.transit.store.Removable;

import net.dpml.profile.ApplicationProfile.StartupPolicy;

/**
 *
 */
public interface ApplicationStorage extends CodeBaseStorage, Removable
{
   /**
    * The preferences attribute name for the startup policy.
    */
    String STARTUP_POLICY_KEY = "startup";

   /**
    * Returns the unique system wide identifier for the application.
    * @return the identifier
    */
    String getID();

   /**
    * Returns the application title.
    * @return the title
    */
    String getTitle();

   /**
    * Update the application title.
    * @param the title
    */
    void setTitle( String title );

   /**
    * Get the duration in mseconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    */    
    int getStartupTimeout();

   /**
    * Set the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @param timeout the startup timeout value
    */
    void setStartupTimeout( int timeout );

   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    */
    int getShutdownTimeout();

   /**
    * Set the duration in seconds to wait for shutdown
    * of the application before considering the application as non-responsive.
    * 
    * @param timeout the shutdown timeout value
    */
    void setShutdownTimeout( int timeout );

   /**
    * Return the startup policy for the application.  If the policy
    * is DISABLED the application cannot be started.  If the policy 
    * is MANUAL startup may be invoked manually.  If the policy is 
    * AUTOMATIC then startup will be handled by the Station.
    *
    * @return the startup policy
    */
    StartupPolicy getStartupPolicy();

   /**
    * Set the the startup policy to one of DISABLED, MANUAL or AUTOMATIC.
    * @param policy the startup policy
    */
    void setStartupPolicy( StartupPolicy policy );

   /**
    * Returns the system properties to be established when launching the 
    * application.
    * @return the system properties to be assigned
    */
    Properties getSystemProperties();

   /**
    * Set the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @param properties the system properties set
    */
    void setSystemProperties( Properties properties );

   /**
    * Set a system property to be assigned to a target virtual machine.
    * 
    * @param key the system property key
    * @param value the property value
    */
    void setSystemProperty( String key, String value );

   /**
    * Returns the working directory to be used for the application.
    * @return the working directory path
    */
    String getWorkingDirectoryPath();

   /**
    * Set the working directory path for the application.
    * @param path the working directory path
    */
    void setWorkingDirectoryPath( String path );

}
