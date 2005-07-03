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
import java.net.PasswordAuthentication;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.store.CodeBaseStorage;

/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 */
public abstract class CodeBaseStorageUnit extends AbstractStorageUnit implements CodeBaseStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public CodeBaseStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // PluginHome
    // ------------------------------------------------------------------------

    public URI getCodeBaseURI()
    {
        return getURI( "uri" );
    }

    public void setCodeBaseURI( URI uri )
    {
        setURI( "uri", uri );
    }

    protected Properties getProperties( Preferences prefs ) throws BuilderException
    {
        Properties properties = new Properties();
        try
        {
            String[] keys = prefs.keys();
            for( int i=0; i<keys.length; i++ )
            {
                String key = keys[i];
                String value = prefs.get( key, null );
                if( null != value )
                {
                    properties.setProperty( key, value );
                }
            }
            return properties;
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Internal error while resolving persistent application properties.";
            throw new BuilderException( error, e );   
        }
    }

    protected void setProperty( Preferences prefs, String key, String value )
    {
        prefs.put( key, value );
    }

    public void removeProperty( Preferences prefs, String key )
    {
        prefs.remove( key );
    }


}


