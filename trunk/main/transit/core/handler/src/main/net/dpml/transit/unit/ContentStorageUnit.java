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
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.store.ContentStorage;
import net.dpml.transit.store.Removable;

/**
 * The ContentSorageUnit class is responsible for the setup of initial factory
 * default preference settings.
 */
public class ContentStorageUnit extends CodeBaseStorageUnit implements ContentStorage, Removable
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    protected ContentStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ContentStorage
    // ------------------------------------------------------------------------

    public void setTitle( String title )
    {
        Preferences prefs = getPreferences();
        setValue( "title", title );
    }

    public void setProperty( String key, String value )
    {
        Preferences prefs = getPreferences().node( "properties" );
        setProperty( prefs, key, value );
    }

    public void removeProperty( String key )
    {
        Preferences prefs = getPreferences().node( "properties" );
        removeProperty( prefs, key );
    }

    public Properties getProperties()
    {
        Preferences prefs = getPreferences().node( "properties" );
        return getProperties( prefs );
    }

    public String getType()
    {
        Preferences prefs = getPreferences();
        return prefs.name();
    }

    public String getTitle()
    {
        Preferences prefs = getPreferences();
        return prefs.get( "title", prefs.name() );
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
            final String error = 
              "Stroage removal failure.";
            throw new BuilderException( error, e );
        }
    }

}

