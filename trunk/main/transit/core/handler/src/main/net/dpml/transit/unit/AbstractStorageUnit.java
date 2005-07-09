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

import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.security.AccessControlException;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

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

    public AbstractStorageUnit( Preferences prefs )
    {
        m_prefs = prefs;
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    protected Preferences getPreferences()
    {
        return m_prefs;
    }

    protected URI getURI( String key ) throws BuilderException
    {
        Preferences prefs = getPreferences();
        return getURI( prefs, key );
    }

    protected URI getURI( Preferences prefs, String key ) throws BuilderException
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
            throw new BuilderException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to construct a uri."
              + "\nPreferences: " + prefs
              + "\nAttribute: " + key
              + "\nValue: " + path;
            throw new BuilderException( error, e );
        }
    }

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

    protected URL getURL( String key ) throws BuilderException
    {
        Preferences prefs = getPreferences();
        return getURL( prefs, key );
    }

    protected URL getURL( Preferences prefs, String key ) throws BuilderException
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
            throw new BuilderException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to construct url."
              + "\nPreferences: " + prefs
              + "\nAttribute: " + key
              + "\nValue: " + path;
            throw new BuilderException( error, e );
        }
    }

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

    protected String getValue( String key, String value )
    {
        Preferences prefs = getPreferences();
        return prefs.get( key, value );
    }

}

