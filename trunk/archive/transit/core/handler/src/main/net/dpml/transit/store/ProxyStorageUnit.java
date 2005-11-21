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
import java.net.PasswordAuthentication;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ProxyStorageUnit extends AbstractStorageUnit implements ProxyStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new proxy stroage unit.
    * @param prefs the preference node to use as the storage solution
    */
    ProxyStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ProxyStorage
    // ------------------------------------------------------------------------

   /**
    * Set the proxy host value.
    * @param host the new proxy host value
    */
    public void setHost( URL host )
    {
        Preferences prefs = getPreferences();
        synchronized( prefs )
        {
            if( null == host )
            {
                prefs.remove( "host" );
                prefs.remove( "scheme" );
                prefs.remove( "prompt" );
            }
            else
            {
                setURL( "host", host );
                prefs.put( "scheme", "" );
                prefs.put( "prompt", "" );
            }
        }
    }

   /**
    * Set the proxy host authentication credentials.
    * @param auth the password authentication credentials
    */
    public void setAuthentication( PasswordAuthentication auth )
      throws StorageRuntimeException
    {
        Preferences prefs = getPreferences();
        if( null == auth )
        {
            prefs.putByteArray( "authentication", new byte[0] );
        }
        else
        {
            byte[] bytes = SerializationHelper.exportCredentials( auth );
            prefs.putByteArray( "authentication", bytes );
        }
    }

   /**
    * Set the proxy excludes value.
    * @param excludes the new proxy excludes value
    */
    public void setExcludes( String[] excludes )
    {
        try
        {
            Preferences prefs = getPreferences();
            Preferences node = prefs.node( "excludes" );
            String[] keys = node.keys();
            for( int i=0; i < keys.length; i++ )
            {
                node.remove( keys[i] );
            }

            if( null != excludes )
            {
                for( int i=0; i < excludes.length; i++ )
                {
                    String exclude = excludes[i];
                    if( null != exclude )
                    {
                        node.put( exclude, "" );
                    }
                }
            }
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Cannot resolve excludes due to preferences backing store error.";
            throw new StorageRuntimeException( error, e );
        }
    }

   /**
    * Update the persistent storage object with the aggregates data collection.
    * @param host the new proxy host value
    * @param auth the new proxy password authentication credentials
    * @param excludes the new proxy host excludes value
    */
    public void saveProxySettings( URL host, PasswordAuthentication auth, String[] excludes )
    {
        Preferences prefs = getPreferences();
        synchronized( prefs )
        {
            setHost( host );
            setAuthentication( auth );
            setExcludes( excludes );
        }
    }

   /**
    * Return the proxy host URL.
    * @return the proxy host url
    */
    public URL getHost()
    {
        return getURL( "host" );
    }

   /**
    * Return the proxy authentication credentials.
    * @return the credentials
    */
    public PasswordAuthentication getAuthentication()
      throws StorageRuntimeException
    {
        Preferences prefs = getPreferences();
        byte[] bytes = prefs.getByteArray( "authentication", new byte[0] );
        if( bytes.length == 0 )
        {
            return null;
        }
        else
        {
            return SerializationHelper.importCredentials( bytes );
        }
    }

   /**
    * Return the array of proxy host excludes.
    * @return the proxy excludes
    */
    public String[] getExcludes() throws StorageRuntimeException
    {
        try
        {
            Preferences prefs = getPreferences();
            Preferences excludes = prefs.node( "excludes" );
            return excludes.keys();
        }
        catch( BackingStoreException e )
        {
            final String error = 
              "Cannot resolve excludes due to preferences backing store error.";
            throw new StorageRuntimeException( error, e );
        }
    }
}


