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

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.model.Parameter;
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

    public ApplicationStorageUnit()
    {
        super( new LocalPreferences( null, "" ) );
    }

    public ApplicationStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ApplicationStorage
    // ------------------------------------------------------------------------

    public String getID()
    {
        return getPreferences().name();
    }

   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    * @exception RemoteException if a transport error occurs
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
    * @exception RemoteException if a transport error occurs
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
    * @exception RemoteException if a transport error occurs
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
    * @exception RemoteException if a transport error occurs
    */
    public void setShutdownTimeout( int timeout )
    {
        Preferences prefs = getPreferences();
        prefs.putInt( "shutdown-timeout", timeout );
    }

    public Properties getSystemProperties()
    {
        Preferences prefs = getPreferences().node( "system" );
        return getProperties( prefs );
    }

    public String getWorkingDirectoryPath()
    {
        Preferences prefs = getPreferences();
        return prefs.get( "working", "${user.dir}" );
    }

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
    * @exception RemoteException if a remote exception occurs
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
    * @param value the startup policy
    * @exception RemoteException if a remote exception occurs
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
