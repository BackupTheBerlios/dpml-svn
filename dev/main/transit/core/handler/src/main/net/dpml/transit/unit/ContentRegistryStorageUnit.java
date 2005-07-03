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

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.store.ContentRegistryHome;
import net.dpml.transit.store.ContentStorage;

/**
 * The LayoutRegistryHelper class provides support for the crreating of a 
 * ContentRegistryModel based on a supplied preferences node.
 */
class ContentRegistryStorageUnit extends CodeBaseStorageUnit implements ContentRegistryHome
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    ContentRegistryStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ContentRegistryHome
    // ------------------------------------------------------------------------

    public ContentStorage createContentStorage( String id, String title, URI uri )
    {
        Preferences prefs = getPreferences();
        Preferences p = prefs.node( id );
        p.put( "title", title );
        p.put( "uri", uri.toASCIIString() );
        return createContentStorage( id );
    }

    public ContentStorage createContentStorage( String id ) throws BuilderException
    {
        Preferences prefs = getPreferences();
        Preferences p = prefs.node( id );
        return new ContentStorageUnit( p );
    }

    public ContentStorage[] getInitialContentStores()
    {
        Preferences prefs = getPreferences();
        try
        {
            ArrayList list = new ArrayList();
            String[] names = prefs.childrenNames();
            for( int i=0; i<names.length; i++ )
            {
                String name = names[i];
                ContentStorage manager = createContentStorage( name );
                list.add( manager );
            }
            return (ContentStorage[]) list.toArray( new ContentStorage[0] );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while constructing content registry manager.";
            throw new BuilderException( error, e );
        }
    }
}
