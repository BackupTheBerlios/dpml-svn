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
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

import net.dpml.transit.model.Value;
/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 */
public abstract class CodeBaseStorageUnit extends AbstractStorageUnit implements CodeBaseStorage
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
    * Return the storage unit identifier.
    * @return the immutable identifier
    */
    public String getID()
    {
        Preferences prefs = getPreferences();
        return prefs.absolutePath();
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
    * values within the returned property instance.
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
            for( int i=0; i < keys.length; i++ )
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
    * Utility operation to store a set of properties into a preferences
    * node.
    *
    * @param prefs the preferences node
    * @param properties the property set to stre into the preferences node
    */
    protected void setProperties( Preferences prefs, Properties properties ) throws StorageRuntimeException
    {
        try
        {
            prefs.clear();
            String[] keys = (String[])properties.keySet().toArray();
            for( int i=0; i < keys.length; i++ )
            {
                String key = keys[i];
                String value = properties.getProperty( key );
                if( null != value )
                {
                    setProperty( prefs, key, value );
                }
            }
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

   /**
    * Return the array of codebase parameter values.
    *
    * @return the parameter value array
    * @exception RemoteException if a remote exception occurs
    */
    public Value[] getParameters()
    {
        Preferences prefs = getPreferences();
        byte[] bytes = prefs.getByteArray( "parameters", new byte[0] );
        if( bytes.length == 0 )
        {
            return new Value[0];
        }
        else
        {
            return SerializationHelper.importParameters( bytes );
        }
    }

   /**
    * Set the array of values assigned to the codebase model for use
    * as plugin constructor parameter arguments.
    *
    * @param values the array of values
    * @exception RemoteException if a remote exception occurs
    */
    public void setParameters( Value[] values )
    {
        Preferences prefs = getPreferences();
        if( null == values )
        {
            prefs.putByteArray( "parameters", new byte[0] );
        }
        else
        {
            byte[] bytes = SerializationHelper.exportParameters( values );
            prefs.putByteArray( "parameters", bytes );
        }
    }
}


