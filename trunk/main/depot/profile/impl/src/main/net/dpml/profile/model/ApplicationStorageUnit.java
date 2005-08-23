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

package net.dpml.profile.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.store.StorageRuntimeException;
import net.dpml.transit.store.LocalPreferences;

import net.dpml.profile.ApplicationStorage;
import net.dpml.profile.Parameter;

/**
 * An ApplicationStorageUnit maintains persistent information 
 * about an application profile.
 */
public class ApplicationStorageUnit extends AbstractStorageUnit implements ApplicationStorage 
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public ApplicationStorageUnit()
    {
        super( new LocalPreferences( null, "" ) );
    }

    public ApplicationStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ApplicationStorage
    // ------------------------------------------------------------------------

    public String getID()
    {
        return getPreferences().name();
    }

    public String getTitle()
    {
        String id = getID();
        return getPreferences().get( "title", id );
    }

    public Properties getSystemProperties()
    {
        Preferences prefs = getPreferences().node( "system" );
        return getProperties( prefs );
    }

    public boolean getEnabled()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "enabled", true );
    }

    public boolean isaServer()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "server", true );
    }

    public Parameter[] getParameters()
    {
        return new Parameter[0]; // TODO
    }

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
