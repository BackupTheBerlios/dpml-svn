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
import java.util.Date;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.model.Logger;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.StorageRuntimeException;
import net.dpml.transit.unit.CodeBaseStorageUnit;

import net.dpml.depot.store.ProfileStorage;

/**
 * A ProfileHome maintains a persistent application profile.
 */
public abstract class ProfileStorageUnit extends CodeBaseStorageUnit implements ProfileStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public ProfileStorageUnit( Preferences prefs ) 
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ProfileStorage
    // ------------------------------------------------------------------------

    public String getID()
    {
        return getPreferences().name();
    }

    public String getTitle()
    {
        String id = getID();
        return getPreferences().get( "title", id );
    }

    public Properties getSystemProperties()
    {
        Preferences prefs = getPreferences().node( "system" );
        return getProperties( prefs );
    }

    // ------------------------------------------------------------------------
    // Removable
    // ------------------------------------------------------------------------

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
