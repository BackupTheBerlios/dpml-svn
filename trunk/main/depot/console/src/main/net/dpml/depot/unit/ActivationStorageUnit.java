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

import net.dpml.depot.store.ActivationGroupStorage;
import net.dpml.depot.store.ActivationStorage;

/**
 * An ApplicationStorageUnit maintains persistent information 
 * about an application profile.
 */
public class ActivationStorageUnit extends ProfileStorageUnit implements ActivationStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public ActivationStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ActivationStorage
    // ------------------------------------------------------------------------

    public ActivationStorage getActivationStorage( String id )
    {
        Preferences prefs = getPreferences().node( "profiles" ).node( id );
        return new ActivationStorageUnit( prefs );
    }

    public boolean getEnabled()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "enabled", true );
    }

    public boolean getRestartPolicy()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "restart", true );
    }

    public void setRestartPolicy( boolean policy )
    {
        Preferences prefs = getPreferences();
        prefs.putBoolean( "restart", policy );
    }

    public String getClassname()
    {
        return getValue( "classname", null );
    }

    public void setClassname( String classname )
    {
        setValue( "classname", classname );
    }
}
