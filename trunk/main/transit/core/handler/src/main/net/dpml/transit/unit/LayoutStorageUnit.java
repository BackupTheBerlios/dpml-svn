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
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.runtime.ClassicLayout;
import net.dpml.transit.runtime.EclipseLayout;
import net.dpml.transit.store.Strategy;
import net.dpml.transit.store.LocalStrategy;
import net.dpml.transit.store.PluginStrategy;
import net.dpml.transit.store.LayoutStorage;
import net.dpml.transit.store.Removable;

/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 */
class LayoutStorageUnit extends CodeBaseStorageUnit implements LayoutStorage, Removable
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    LayoutStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // LayoutStorage
    // ------------------------------------------------------------------------

    public String getID()
    {
        Preferences prefs = getPreferences();
        return prefs.name();
    }

    public String getTitle()
    {
        Preferences prefs = getPreferences();
        String id = getID();
        return prefs.get( "title", id );
    }

    public void setTitle( String title )
    {
        setValue( "title", title );
    }

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

    public void remove()
    {
        try
        {
            getPreferences().removeNode();
        }
        catch( BackingStoreException e )
        {
            throw new BuilderException( "storage removal failure", e );
        }
    }
}



