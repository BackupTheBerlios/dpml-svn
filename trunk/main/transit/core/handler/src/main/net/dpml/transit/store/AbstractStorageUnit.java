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

import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.util.prefs.Preferences;

/**
 * Abstract base class providing a framework for preferences based persistance.
 */
public abstract class AbstractStorageUnit
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private Preferences m_prefs;

    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Creation of a new abstract storage unit.
    * @param prefs a preferences node
    */
    public AbstractStorageUnit( Preferences prefs )
    {
        m_prefs = prefs;
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

   /**
    * Return the preferences node.
    * @return the preference node assigned to the storage unit
    */
    protected Preferences getPreferences()
    {
        return m_prefs;
    }

   /**
    * Utility operation to return a URI given a preferences attribute key.
    * @param key the attribute key
    * @return the uri value or null if the key is unknown
    * @exception StorageRuntimeException if a value was located but was not resolvable to a URI.
    */
    protected URI getURI( String key ) throws StorageRuntimeException
    {
        Preferences prefs = getPreferences();
        return getURI( prefs, key );
    }

   /**
    * Utility operation to return a URI given a preferences attribute key.
    * @param prefs a preferences node
    * @param key the attribute key
    * @return the uri value or null if the key is unknown
    * @exception StorageRuntimeException if a value was located but was not resolvable to a URI.
    */
    protected URI getURI( Preferences prefs, String key ) throws StorageRuntimeException
    {
        String path = prefs.get( key, null );
        try
        {
            if( null == path )
            {
                return null;
            }
            else
            {
                return new URI( path );
            }
        } 
        catch( URISyntaxException e )
        {
            final String error = 
              "Invalid URI attribute value."
              + "\nPreferences: " + prefs
              + "\nAttribute: " + key
              + "\nValue: " + path;
            throw new StorageRuntimeException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to construct a uri."
              + "\nPreferences: " + prefs
              + "\nAttribute: " + key
              + "\nValue: " + path;
            throw new StorageRuntimeException( error, e );
        }
    }

   /**
    * Utility operation to set a URI value under a supplied attrbute key.
    * @param key the attribute key
    * @param uri the URI value
    */
    protected void setURI( String key, URI uri )
    {
        if( null == uri )
        {
            getPreferences().remove( key );
        }
        else
        {
            String ascii = uri.toASCIIString();
            getPreferences().put( key, ascii );
        }
    }

   /**
    * Utility operation to return a URL given a preferences attribute key.
    * @param key the attribute key
    * @return the URL value or null if the key is unknown
    * @exception StorageRuntimeException if a value was located but was not resolvable to a URI.
    */
    protected URL getURL( String key ) throws StorageRuntimeException
    {
        Preferences prefs = getPreferences();
        return getURL( prefs, key );
    }

   /**
    * Utility operation to return a URL given a preferences attribute key.
    * @param prefs a preferences node
    * @param key the attribute key
    * @return the URL value or null if the key is unknown
    * @exception StorageRuntimeException if a value was located but was not resolvable to a URI.
    */
    protected URL getURL( Preferences prefs, String key ) throws StorageRuntimeException
    {
        String path = prefs.get( key, null );
        try
        {
            if( null == path )
            {
                return null;
            }
            else
            {
                return new URL( path );
            }
        } 
        catch( MalformedURLException e )
        {
            final String error = 
              "Invalid URL attribute value."
              + "\nPreferences: " + prefs
              + "\nAttribute: " + key
              + "\nValue: " + path;
            throw new StorageRuntimeException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to construct url."
              + "\nPreferences: " + prefs
              + "\nAttribute: " + key
              + "\nValue: " + path;
            throw new StorageRuntimeException( error, e );
        }
    }

   /**
    * Utility operation to set a URL value under a supplied attribute key.
    * @param key the attribute key
    * @param url the URL value
    */
    protected void setURL( String key, URL url )
    {
        if( null == url )
        {
            getPreferences().remove( key );
        }
        else
        {
            try
            {
                String path = url.toString();
                URI uri = new URI( path );
                String ascii = uri.toASCIIString();
                getPreferences().put( key, ascii );
            }
            catch( Exception e )
            {
                getPreferences().put( key, url.toString() );
            }
        }
    }

   /**
    * Utility operation to set an attribute value.
    * @param key the attribute key
    * @param value the value to assign to the attribute
    */
    protected void setValue( String key, String value )
    {
        Preferences prefs = getPreferences();
        if( null == value )
        {
            prefs.remove( key );
        }
        else
        {
            prefs.put( key, value );
        }
    }

   /**
    * Utility operation to get an attribute value.
    * @param key the attribute key
    * @param value the default value
    * @return the attribute value if assigned otherwise the default value
    */
    protected String getValue( String key, String value )
    {
        Preferences prefs = getPreferences();
        return prefs.get( key, value );
    }

}

