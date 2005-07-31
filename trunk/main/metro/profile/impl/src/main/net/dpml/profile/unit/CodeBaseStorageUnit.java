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

package net.dpml.profile.unit;

import java.net.URI;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.store.CodeBaseStorage;
import net.dpml.transit.store.StorageRuntimeException;

/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 */
abstract class CodeBaseStorageUnit extends AbstractStorageUnit implements CodeBaseStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new codebase storage unit.
    * @param prefs the preferences to use as the undrlying storage model
    */
    public CodeBaseStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // CodeBaseStorage
    // ------------------------------------------------------------------------

   /**
    * Return the URI identifying a codebase.  Typically the value returned from 
    * this operation identifes a plugin uses as a system extension or customization 
    * point.  The implement maps codebase uris under the attribute key "uri".
    *
    * @return the codebase uri
    */
    public URI getCodeBaseURI()
    {
        return getURI( "uri" );
    }

   /**
    * Set the codebase uri under the attribute "uri".
    * @param uri the uri identifying the codebase for a system extension
    */
    public void setCodeBaseURI( URI uri )
    {
        setURI( "uri", uri );
    }

   /**
    * Utility operation to construct a set of properties from a preferences
    * node wherein all attributes in the supplied node are mapped to property 
    * values witin the returned property instahnce.
    *
    * @param prefs the preferences node
    * @return the property set
    */
    protected Properties getProperties( Preferences prefs ) throws StorageRuntimeException
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
            throw new StorageRuntimeException( error, e );   
        }
    }

   /**
    * Utility operation to set a property value on a supplied node.
    * @param prefs the preference node
    * @param key the property key
    * @param value the properrty value
    */
    protected void setProperty( Preferences prefs, String key, String value )
    {
        prefs.put( key, value );
    }

   /**
    * Utility operation to remove a property.
    * @param prefs the preference node
    * @param key the property key
    */
    public void removeProperty( Preferences prefs, String key )
    {
        prefs.remove( key );
    }
}


