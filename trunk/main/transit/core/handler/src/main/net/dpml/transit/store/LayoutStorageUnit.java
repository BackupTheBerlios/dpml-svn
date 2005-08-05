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

import java.net.URI;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.ClassicLayout;

/**
 * The LayoutStorageUnit class maintains persistent information about a 
 * layout model.
 */
class LayoutStorageUnit extends CodeBaseStorageUnit implements LayoutStorage, Removable
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new layout storage unit.
    * @param prefs the preferences to use as the underlying storage model
    */
    LayoutStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // LayoutStorage
    // ------------------------------------------------------------------------

   /**
    * The layout model identifier.
    * @return the layout id
    */
    public String getID()
    {
        Preferences prefs = getPreferences();
        return prefs.name();
    }

   /**
    * The layout model title.
    * @return the layout title
    */
    public String getTitle()
    {
        Preferences prefs = getPreferences();
        String id = getID();
        return prefs.get( "title", id );
    }

   /**
    * Set the layout model title.
    * @param title the new layout title
    */
    public void setTitle( String title )
    {
        setValue( "title", title );
    }

   /**
    * Return the strategy object for the layout model.
    * @return the strategy
    */
    public Strategy getStrategy()
    {
        URI uri = getCodeBaseURI();
        if( null != uri )
        {
            return new PluginStrategy( uri );
        }
        else
        {
            Preferences prefs = getPreferences();
            String classname = prefs.get( "classname", ClassicLayout.class.getName() );
            return new LocalStrategy( classname, false );
        }
    }

   /**
    * Set the model construction strategy.
    * @param strategy the construction strategy
    */
    public void setStrategy( Strategy strategy )
    {
        if( strategy instanceof PluginStrategy )
        {
            PluginStrategy plugin = (PluginStrategy) strategy;
            setCodeBaseURI( plugin.getURI() );
        }
        else
        {
            LocalStrategy local = (LocalStrategy) strategy;
            String classname = local.getClassname();
            setValue( "classname", classname );
        }
    }

    // ------------------------------------------------------------------------
    // Removable
    // ------------------------------------------------------------------------

   /**
    * Remove the storage unit.
    */
    public void remove()
    {
        try
        {
            getPreferences().removeNode();
        }
        catch( BackingStoreException e )
        {
            throw new StorageRuntimeException( "storage removal failure", e );
        }
    }
}



