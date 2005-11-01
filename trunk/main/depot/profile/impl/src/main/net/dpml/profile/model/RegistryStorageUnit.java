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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.store.AbstractStorageUnit;
import net.dpml.transit.store.StorageRuntimeException;

import net.dpml.station.Station;

/**
 * A DepotStorage maintains persistent records of application profiles.
 */
public class RegistryStorageUnit extends AbstractStorageUnit implements RegistryStorage
{
    private static final Preferences STATION_PREFS = 
      Preferences.userNodeForPackage( Station.class );

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new registry storage unit.
    */
    public RegistryStorageUnit() 
    {
        super( STATION_PREFS );
    }

    // ------------------------------------------------------------------------
    // DepotStorage
    // ------------------------------------------------------------------------

   /**
    * Return the inital array of application storage instances within the store.
    * @return the inital storage unit array
    */
    public ApplicationStorage[] getInitialApplicationStorageArray()
    {
        try
        {
            Preferences prefs = getProfilesNode();
            String[] names = prefs.keys();
            ApplicationStorage[] stores = new ApplicationStorage[ names.length ];
            for( int i=0; i<names.length; i++ )
            {
                String id = names[i];
                String path = prefs.get( id, id );
                ApplicationStorage store = getApplicationStorage( path );
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

   /**
    * Return the backing store.
    * @return the preference node
    */
    public Preferences getProfilesNode() 
    {
        return getPreferences().node( "profiles" );
    }

   /**
    * Get an identified storage unit.
    * @param id the storage unit id
    * @return the storage unit
    */
    public ApplicationStorage getApplicationStorage( String id ) 
    {
        Preferences prefs = getProfilesNode().node( id );
        return new ApplicationStorageUnit( prefs );
    }
}
