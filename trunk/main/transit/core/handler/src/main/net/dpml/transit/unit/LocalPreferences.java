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
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferencesFactory;

import net.dpml.transit.Transit;

/**
 * An implementation of Preferences based on java.util.Properties.
 */
public class LocalPreferences extends AbstractPreferences
{
    private final Properties m_properties;
    private Hashtable m_table = new Hashtable();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public LocalPreferences( LocalPreferences parent, String name )
    {
        super( parent, name );
        m_properties = new Properties();
    }

    // ------------------------------------------------------------------------
    // LocalPreferences
    // ------------------------------------------------------------------------

    protected void putSpi( String key, String value )
    {
        getProperties().setProperty( key, value );
    }

    protected String getSpi( String key )
    { 
        return getProperties().getProperty( key );
    }

    protected void removeSpi( String key )
    {
        getProperties().remove( key );
    }

    protected void removeNodeSpi() throws BackingStoreException
    {
        LocalPreferences parent = (LocalPreferences) parent();
        if( null != parent )
        {
            parent.removeNode( name() );
        }
    }

    protected String[] keysSpi() throws BackingStoreException 
    {
        return (String[]) m_properties.keySet().toArray( new String[0] );
    }

    protected String[] childrenNamesSpi() throws BackingStoreException 
    {
        return (String[]) m_table.keySet().toArray( new String[0] );
    }

    protected AbstractPreferences childSpi( String name )
    {
        synchronized( m_table )
        {
            LocalPreferences prefs = (LocalPreferences) m_table.get( name );
            if( null != prefs )
            {
                return prefs;
            }
            else
            {
                LocalPreferences p = new LocalPreferences( this, name );
                m_table.put( name, p );
                return p;
            }
        }
    }

    protected void syncSpi() throws BackingStoreException
    {
    }

    protected void flushSpi() throws BackingStoreException
    {
    }

    void removeNode( String name )
    {
        synchronized( m_table )
        {
            m_table.remove( name );
        }
    }

    private Properties getProperties()
    {
        return m_properties;
    }
}
