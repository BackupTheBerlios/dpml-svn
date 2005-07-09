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

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.store.CacheHome;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.HostStorage;
import net.dpml.transit.util.PropertyResolver;

/**
 * The CacheStorageUnit class maintains persistent information about a 
 * cache location, a cache layout strategy, and a set of associated host 
 * storage units.  The class also provides support for creation and removal 
 * host storage units.
 */
class CacheStorageUnit extends CodeBaseStorageUnit implements CacheHome, Removable
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    CacheStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // CacheHome
    // ------------------------------------------------------------------------

   /**
    * Update the preferences cache location attribute with the value of 
    * the supplied file.  If the file argument is null the cache location 
    * will be set to "cache" otherwise the string form of the file will 
    * be assigned as the cache directory value.
    *
    * @param file the cache directory
    */
    public void setCacheDirectoryPath( String path )
    {
        Preferences prefs = getPreferences();
        if( null == path )
        {
            setValue( "location", null );
        }
        else
        {
            setValue( "location", path );
        }
    }

    public HostStorage getHostStorage( String id ) throws BuilderException
    {
        Preferences prefs = getHostsNodePreferences().node( id );
        return new HostStorageUnit( prefs );
    }

    public String getLayoutModelKey()
    {
        return getValue( "layout", "classic" );
    }

    public void setLayoutModelKey( String key )
    {
        setValue( "layout", key );
    }

    // ------------------------------------------------------------------------
    // Removable
    // ------------------------------------------------------------------------

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    public HostStorage[] getInitialHosts()
    {
        Preferences prefs = getHostsNodePreferences();
        try
        {
            ArrayList list = new ArrayList();
            String[] names = prefs.childrenNames();
            for( int i=0; i<names.length; i++ )
            {
                String name = names[i];
                HostStorage host = getHostStorage( name );
                list.add( host );
            }
            return (HostStorage[]) list.toArray( new HostStorage[0] );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while constructing cache manager hosts.";
            throw new BuilderException( error, e );
        }
    }

   /**
    * Resolve the cache directory using preferences.
    * @param prefs the cache preferences
    * @return the cache directory
    */
    public String getCacheDirectoryPath()
    {
        Preferences prefs = getPreferences();
        String path = prefs.get( "location", "${dpml.data}/cache" );
        return path;
    }

    private Preferences getHostsNodePreferences()
    {
        Preferences prefs = getPreferences();
        return prefs.node( "hosts" );
    }

    private String[] childrenNames( Preferences prefs )
    {
        try
        {
            return prefs.childrenNames();
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Unexected error raised by preferences backing store while resolving child nodes.";
            throw new BuilderException( error, e );
        }
    }
}

