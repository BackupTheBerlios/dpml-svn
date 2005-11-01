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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.store.StorageRuntimeException;
import net.dpml.transit.store.LocalPreferences;
import net.dpml.transit.store.ContentStorageUnit;

import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.ApplicationProfile.StartupPolicy;

/**
 * An ApplicationStorageUnit maintains persistent information 
 * about an application profile.
 */
public class ApplicationStorageUnit extends ContentStorageUnit implements ApplicationStorage 
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new application storage unit.
    */
    public ApplicationStorageUnit()
    {
        super( new LocalPreferences( null, "" ) );
    }

   /**
    * Creation of a new application storage unit.
    * @param prefs the preferences backing store
    */
    public ApplicationStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ApplicationStorage
    // ------------------------------------------------------------------------

   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    */    
    public int getStartupTimeout()
    {
        Preferences prefs = getPreferences();
        return prefs.getInt( "startup-timeout", ApplicationProfile.DEFAULT_STARTUP_TIMEOUT );
    }

   /**
    * Set the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @param timeout the startup timeout value
    */
    public void setStartupTimeout( int timeout )
    {
        Preferences prefs = getPreferences();
        prefs.putInt( "startup-timeout", timeout );
    }

   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    */
    public int getShutdownTimeout()
    {
        Preferences prefs = getPreferences();
        return prefs.getInt( "shutdown-timeout", ApplicationProfile.DEFAULT_SHUTDOWN_TIMEOUT );
    }

   /**
    * Set the duration in seconds to wait for shutdown
    * of the application before considering the application as non-responsive.
    * 
    * @param timeout the shutdown timeout value
    */
    public void setShutdownTimeout( int timeout )
    {
        Preferences prefs = getPreferences();
        prefs.putInt( "shutdown-timeout", timeout );
    }

   /**
    * Return the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @return the system properties
    */
    public Properties getSystemProperties()
    {
        Preferences prefs = getPreferences().node( "system" );
        return getProperties( prefs );
    }

   /**
    * Set the system properties to be assigned to a target virtual machine
    * on application deployment.
    * 
    * @param properties the system properties set
    */
    public void setSystemProperties( Properties properties )
    {
        Preferences prefs = getPreferences().node( "system" );
        setProperties( prefs, properties );
    }

   /**
    * Set a system property to be assigned to a target virtual machine.
    * 
    * @param key the system property key
    * @param value the property value
    */
    public void setSystemProperty( String key, String value )
    {
        Preferences prefs = getPreferences().node( "system" );
        setProperty( prefs, key, value );
    }

   /**
    * Return the working directory path.
    * 
    * @return the path
    */
    public String getWorkingDirectoryPath()
    {
        Preferences prefs = getPreferences();
        return prefs.get( "working", "${user.dir}" );
    }

   /**
    * Set the working directory for the application.
    * 
    * @param path the working directory path
    */
    public void setWorkingDirectoryPath( String path )
    {
        Preferences prefs = getPreferences();
        prefs.put( "working", path );
    }

   /**
    * Return the startup policy for the application.  If the policy
    * is DISABLED the application cannot be started.  If the policy 
    * is MANUAL startup may be invoked manually.  If the policy is 
    * AUTOMATIC then startup will be handled by the Station.
    *
    * @return the startup policy
    */
    public StartupPolicy getStartupPolicy()
    {
        Preferences prefs = getPreferences();
        String policy = prefs.get( STARTUP_POLICY_KEY, ApplicationProfile.DISABLED.key() );
        if( ApplicationProfile.DISABLED.key().equals( policy ) )
        {
            return ApplicationProfile.DISABLED;
        }
        else if( ApplicationProfile.MANUAL.key().equals( policy ) )
        {
            return ApplicationProfile.MANUAL;
        }
        else if( ApplicationProfile.AUTOMATIC.key().equals( policy ) )
        {
            return ApplicationProfile.AUTOMATIC;
        }
        else
        {
            final String error = 
              "Corrupt or invalid startup policy value ["
              + policy 
              + "] in preferences node ["
              + prefs
              + "].";
            throw new IllegalStateException( error );
        }
    }

   /**
    * Set the the startup policy to one of DISABLED, MANUAL or AUTOMATIC.
    * @param policy the startup policy
    */
    public void setStartupPolicy( StartupPolicy policy )
    {
        Preferences prefs = getPreferences();
        if( ApplicationProfile.DISABLED.equals( policy ) )
        {
            prefs.put( STARTUP_POLICY_KEY, ApplicationProfile.DISABLED.key() );
        }
        else if( ApplicationProfile.MANUAL.equals( policy ) )
        {
            prefs.put( STARTUP_POLICY_KEY, ApplicationProfile.MANUAL.key() );
        }
        else if( ApplicationProfile.AUTOMATIC.equals( policy ) )
        {
            prefs.put( STARTUP_POLICY_KEY, ApplicationProfile.AUTOMATIC.key() );
        }
        else
        {
            final String error = 
              "Startup policy value ["
              + policy 
              + "] not recognized.";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Remove the storage unit.
    */
    public void remove()
    {
        try
        {
            getPreferences().removeNode();
        }
        catch( BackingStoreException e )
        {
            throw new StorageRuntimeException( "storage removal failure", e );
        }
    }
}
