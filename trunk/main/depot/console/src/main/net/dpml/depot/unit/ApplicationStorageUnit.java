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

package net.dpml.depot.unit;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.store.StorageRuntimeException;

import net.dpml.depot.store.ApplicationStorage;

/**
 * An ApplicationStorageUnit maintains persistent information 
 * about an application profile.
 */
public class ApplicationStorageUnit extends ProfileStorageUnit implements ApplicationStorage 
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public ApplicationStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ApplicationStorage
    // ------------------------------------------------------------------------

    public boolean getEnabled()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "enabled", true );
    }

    public boolean getCommandPolicy()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "command", false );
    }

    public Connection getConnection()
    {
        Preferences root = getPreferences();
        try
        {
            if( false == root.nodeExists( "connection" ) )
            {
                return null;
            }
            else
            {
                Preferences prefs = root.node( "connection" );
                String host = prefs.get( "host", null );
                int port = prefs.getInt( "port", 1099 );
                boolean optional = prefs.getBoolean( "optional", true );
                boolean enabled = prefs.getBoolean( "enabled", true );
                return new Connection( host, port, optional, enabled );
            }
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Cannot construct a connection due to non-availability of the preferences store.";
            throw new StorageRuntimeException( error, e );   
        }
    }

    public Properties getProperties()
    {
        Preferences prefs = getPreferences().node( "properties" );
        return getProperties( prefs );
    }
}
