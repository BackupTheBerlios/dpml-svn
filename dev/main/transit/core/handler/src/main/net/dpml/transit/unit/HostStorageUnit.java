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
import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.net.PasswordAuthentication;

import net.dpml.transit.runtime.DefaultResourceHost;
import net.dpml.transit.store.HostStorage;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.Strategy;
import net.dpml.transit.store.PluginStrategy;
import net.dpml.transit.store.LocalStrategy;

/**
 * The LayoutHelper class is responsible for the setup of initial factory
 * default preference settings.
 */
class HostStorageUnit extends CodeBaseStorageUnit implements HostStorage, Removable
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private static final String DEFAULT_HOST_CLASSNAME = DefaultResourceHost.class.getName();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    HostStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // HostStorage
    // ------------------------------------------------------------------------

    public void setHostSettings( 
      String base, String index, boolean enabled, boolean trusted, String layout, 
      PasswordAuthentication auth, String scheme, String prompt )
    {
        Preferences prefs = getPreferences();
        synchronized( prefs )
        {
            setValue( "base", base );
            setValue( "index", index );
            setTrusted( trusted );
            setEnabled( enabled );
            setLayoutModelKey( layout );
            setAuthentication( auth );
            setValue( "scheme", scheme );
            setValue( "prompt", prompt );
        }
    }


    public String getID()
    {
        Preferences prefs = getPreferences();
        return prefs.name();
    }

    public String getName()
    {
        String id = getID();
        return getValue( "name", id );
    }

    public String getScheme()
    {
        return getValue( "scheme", "" );
    }

    public String getPrompt()
    {
        return getValue( "prompt", "" );
    }

    public boolean getBootstrap()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "bootstrap", false );
    }

    public boolean getTrusted()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "trusted", false );
    }

    public boolean getEnabled()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "enabled", false );
    }

    public int getPriority()
    {
        Preferences prefs = getPreferences();
        return prefs.getInt( "priority", 600 );
    }

    public String getLayoutModelKey()
    {
        Preferences prefs = getPreferences();
        return getValue( "layout", "classic" );
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
            String classname = prefs.get( "classname", DEFAULT_HOST_CLASSNAME );
            return new LocalStrategy( classname, false );
        }
    }

    public String getBasePath() throws BuilderException
    {
        Preferences prefs = getPreferences();
        return prefs.get( "base", "http://localhost" );
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

    public String getIndexPath() throws BuilderException
    {
        Preferences prefs = getPreferences();
        return prefs.get( "index", null );
    }

    private void setTrusted( boolean trusted )
    {
        Preferences prefs = getPreferences();
        prefs.putBoolean( "trusted", trusted );
    }

    private void setEnabled( boolean enabled )
    {
        Preferences prefs = getPreferences();
        prefs.putBoolean( "enabled", enabled );
    }

    public void setName( String name )
    {
        setValue( "name", name );
    }

    public void setPriority( int priority )
    {
        Preferences prefs = getPreferences();
        synchronized( prefs )
        {
            prefs.putInt( "priority", priority );
        }
    }

    public void setLayoutModelKey( String layout )
    {
        if( null == layout )
        {
            setValue( "layout", null );
        }
        else
        {
            setValue( "layout", layout );
        }
    }

   /**
    * Set the host authentication.
    * @param auth the password authentication to use for host sign-on
    */
    private void setAuthentication( PasswordAuthentication auth ) 
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

