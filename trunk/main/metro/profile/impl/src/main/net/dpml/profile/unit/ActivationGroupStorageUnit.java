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

package net.dpml.profile.unit;

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

import net.dpml.profile.store.ActivationGroupStorage;
import net.dpml.profile.store.ActivationStorage;

/**
 * An ApplicationStorageUnit maintains persistent information 
 * about an application profile.
 */
public class ActivationGroupStorageUnit extends ProfileStorageUnit implements ActivationGroupStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public ActivationGroupStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ActivationGroupStorage
    // ------------------------------------------------------------------------

    public ActivationStorage[] getInitialActivationStorageArray()
    {
        try
        {
            Preferences prefs = getPreferences().node( "profiles" );
            String[] names = prefs.childrenNames();
            ActivationStorage[] stores = new ActivationStorage[ names.length ];
            for( int i=0; i<names.length; i++ )
            {
                String id = names[i];
                ActivationStorage store = getActivationStorage( id );
                stores[i] = store;
            }
            return stores;
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Internal error while resolving activation profiles "
              + "due to non-availability of the preferences store.";
            throw new StorageRuntimeException( error, e );   
        }
    }

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
}
