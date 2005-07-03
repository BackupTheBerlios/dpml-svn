/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.transit.unit;

import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.registry.Registry;
import java.rmi.activation.ActivationSystem;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitError;
import net.dpml.transit.store.TransitHome;
import net.dpml.transit.store.TransitStorage;

/**
 * The TransitStorageUnit is responsible for the construction of persistent
 * storage units for all Transit subsystems.
 */
public class TransitStorageHome extends AbstractStorageUnit implements TransitHome
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public TransitStorageHome()
    {
        super( Preferences.userNodeForPackage( Transit.class ) );
    }

    // ------------------------------------------------------------------------
    // TransitHome
    // ------------------------------------------------------------------------

    public TransitStorage[] getInitialTransitStores()
    {
        String[] names = getProfileNames();
        TransitStorage[] stores = new TransitStorage[ names.length ];
        for( int i=0; i<names.length; i++ )
        {
            String id = names[i];
            stores[i] = getTransitStorage( id );
        }
        return stores;
    }

    public TransitStorage getTransitStorage( String id )
    {
        return new TransitStorageUnit( id );
    }

    public String[] getProfileNames()
    {
        try
        {
            return getProfilesNode().childrenNames();
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Preferences storage is unavailable.";
            throw new TransitError( error, e );
        }
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    private Preferences getProfilesNode()
    {
        return getPreferences().node( "profiles" );
    }
}
