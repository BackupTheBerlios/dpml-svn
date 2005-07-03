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
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.rmi.RemoteException;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.Date;

import net.dpml.transit.store.ProxyStorage;

/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 */
class ProxyStorageUnit extends AbstractStorageUnit implements ProxyStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    ProxyStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // ProxyStorage
    // ------------------------------------------------------------------------

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

    public void setAuthentication( PasswordAuthentication auth )
      throws BuilderException
    {
        Preferences prefs = getPreferences();
        if( null == auth )
        {
            prefs.putByteArray( "authentication", new byte[0] );
        }
        else
        {
            byte[] bytes = CredentialsHelper.exportCredentials( auth );
            prefs.putByteArray( "authentication", bytes );
        }
    }

    public void setExcludes( String[] excludes )
    {
        // TODO: 
    }

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

    public URL getHost()
    {
        return getURL( "host" );
    }

    public PasswordAuthentication getAuthentication()
      throws BuilderException
    {
        Preferences prefs = getPreferences();
        byte[] bytes = prefs.getByteArray( "authentication", new byte[0] );
        if( bytes.length == 0 )
        {
            return null;
        }
        else
        {
            return CredentialsHelper.importCredentials( bytes );
        }
    }

    public String[] getExcludes() throws BuilderException
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
            throw new BuilderException( error, e );
        }
    }

}


