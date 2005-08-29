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
import net.dpml.transit.store.AbstractStorageUnit;
import net.dpml.transit.store.StorageRuntimeException;

/**
 * A DepotStorage maintains persistent records of application profiles.
 */
public class RegistryStorageUnit extends AbstractStorageUnit implements RegistryStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public RegistryStorageUnit( Preferences prefs ) 
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // DepotStorage
    // ------------------------------------------------------------------------

    public ApplicationStorage[] getInitialApplicationStorageArray()
    {
        try
        {
            Preferences prefs = getPreferences().node( "profiles" );
            String[] names = prefs.childrenNames();
            ApplicationStorage[] stores = new ApplicationStorage[ names.length ];
            for( int i=0; i<names.length; i++ )
            {
                String id = names[i];
                ApplicationStorage store = getApplicationStorage( id );
                stores[i] = store;
            }
            return stores;
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Internal error while resolving profiles due to non-availability of the preferences store.";
            throw new StorageRuntimeException( error, e );   
        }
    }

    public ApplicationStorage getApplicationStorage( String id ) 
    {
        Preferences prefs = getPreferences().node( "profiles" ).node( id );
        return new ApplicationStorageUnit( prefs );
    }
}
