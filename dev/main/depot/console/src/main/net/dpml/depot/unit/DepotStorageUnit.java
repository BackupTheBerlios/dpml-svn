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
import net.dpml.transit.store.StorageRuntimeException;
import net.dpml.transit.unit.AbstractStorageUnit;

import net.dpml.depot.store.DepotHome;
import net.dpml.depot.store.ApplicationStorage;
import net.dpml.depot.store.ActivationGroupStorage;

/**
 * A DepotStorage maintains persistent records of application profiles.
 */
public class DepotStorageUnit extends AbstractStorageUnit implements DepotHome
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DepotStorageUnit( Preferences prefs ) 
    {
        super( prefs );

        setupTestProfile();
        setupMexicoProfile();
        setupMetroProfile();
        setupHttpProfile();
    }

    // ------------------------------------------------------------------------
    // DepotStorage
    // ------------------------------------------------------------------------

    public ActivationGroupStorage[] getInitialActivationGroupStorageArray()
    {
        try
        {
            Preferences prefs = getPreferences().node( "groups" );
            String[] names = prefs.childrenNames();
            ActivationGroupStorage[] stores = new ActivationGroupStorage[ names.length ];
            for( int i=0; i<names.length; i++ )
            {
                String id = names[i];
                ActivationGroupStorage store = getActivationGroupStorage( id );
                stores[i] = store;
            }
            return stores;
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Internal error while resolving group due to non-availability of the preferences store.";
            throw new StorageRuntimeException( error, e );   
        }
    }

    public ActivationGroupStorage getActivationGroupStorage( String id )
    {
        Preferences prefs = getPreferences().node( "groups" ).node( id );
        return new ActivationGroupStorageUnit( prefs );
    }

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

    private void setupTestProfile()
    {
        String id = "test";
        Preferences prefs = getPreferences().node( "profiles" ).node( id );
        prefs.put( "uri", "@TEST-PLUGIN-URI@" );
        prefs.put( "title", "Depot Cient Test" );
        prefs.putBoolean( "command", true );
    }

    private void setupMexicoProfile()
    {
        String id = "mexico";
        Preferences prefs = getPreferences().node( "groups" ).node( "demo" ).node( "profiles" ).node( id );
        prefs.put( "uri", "@TEST-SERVER-URI@" );
        prefs.put( "title", "Depot Activation Test" );
        prefs.put( "classname", "net.dpml.test.mexico.Server" );
    }

    private void setupMetroProfile()
    {
        String id = "metro";
        Preferences prefs = getPreferences().node( "profiles" ).node( id );
        prefs.put( "uri", "@METRO-CENTRAL-URI@" );
        prefs.put( "title", "Metro" );
    }

    private void setupHttpProfile()
    {
        String id = "http";
        Preferences prefs = getPreferences().node( "profiles" ).node( id );
        prefs.put( "uri", "link:part:dpml/planet/http/dpml-http-demo#LATEST" );
        prefs.put( "title", "HTTP Demo" );
    }

}
