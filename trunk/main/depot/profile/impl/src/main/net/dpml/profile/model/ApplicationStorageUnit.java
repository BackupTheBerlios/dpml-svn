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

    public Properties getSystemProperties()
    {
        Preferences prefs = getPreferences().node( "system" );
        return getProperties( prefs );
    }

    public boolean getEnabled()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "enabled", true );
    }

    public boolean isaServer()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "server", true );
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
