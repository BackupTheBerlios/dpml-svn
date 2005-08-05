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

package net.dpml.transit.store;

import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * The ContentSorageUnit class is responsible for the setup of initial factory
 * default preference settings.
 */
class ContentStorageUnit extends CodeBaseStorageUnit implements ContentStorage, Removable
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new content stroage unit.
    * @param prefs the preferences to use as the underlying storage model
    */
    protected ContentStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ContentStorage
    // ------------------------------------------------------------------------

   /**
    * Set the content title to the supplied value.
    * @param title the content title
    */
    public void setTitle( String title )
    {
        Preferences prefs = getPreferences();
        setValue( "title", title );
    }

   /**
    * Set a property to the supplied value.
    * @param key the property key
    * @param value the property value
    */
    public void setProperty( String key, String value )
    {
        Preferences prefs = getPreferences().node( "properties" );
        setProperty( prefs, key, value );
    }

   /**
    * Remove a property.
    * @param key the property key
    */
    public void removeProperty( String key )
    {
        Preferences prefs = getPreferences().node( "properties" );
        removeProperty( prefs, key );
    }

   /**
    * Return the content properties.
    * @return the properties
    */
    public Properties getProperties()
    {
        Preferences prefs = getPreferences().node( "properties" );
        return getProperties( prefs );
    }

   /**
    * Return the content storage type identifier.
    * @return the content type immutable identifier
    */
    public String getType()
    {
        Preferences prefs = getPreferences();
        return prefs.name();
    }

   /**
    * Return the content title
    * @return the content type title
    */
    public String getTitle()
    {
        Preferences prefs = getPreferences();
        return prefs.get( "title", prefs.name() );
    }

    // ------------------------------------------------------------------------
    // Removable
    // ------------------------------------------------------------------------

   /**
    * Removal of the storage unit.
    */
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
            throw new StorageRuntimeException( error, e );
        }
    }

}

