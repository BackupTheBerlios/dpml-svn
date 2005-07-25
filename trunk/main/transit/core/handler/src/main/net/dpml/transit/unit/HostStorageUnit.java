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
 * Default implementation of a persistent host storage using java.util.Preferences.
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

   /**
    * Creation of the hew host storage unit.
    * @param prefs the preferences to use as the persistent store
    */
    HostStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // HostStorage
    // ------------------------------------------------------------------------

   /**
    * Update the storage unit with the supplied values.
    *
    * @param base the host base url path
    * @param index the host content index
    * @param enabled the enabled status of the host
    * @param trusted the trusted status of the host
    * @param layout the assigned host layout identifier
    * @param auth a possibly null host authentication username and password
    * @param scheme the host security scheme
    * @param prompt the security prompt raised by the host
    */
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

   /**
    * Return an immutable host identifier.  The host identifier shall be 
    * guranteed to be unique and constant for the life of the storage unit.
    * 
    * @return the immutable host model identifier
    */
    public String getID()
    {
        Preferences prefs = getPreferences();
        return prefs.name();
    }

   /**
    * Return the name of the resource host.
    * @return the host name
    */
    public String getName()
    {
        String id = getID();
        return getValue( "name", id );
    }

   /**
    * Return a striong identify the authentication scheme employed by the host.
    * @return the scheme
    */
    public String getScheme()
    {
        return getValue( "scheme", "" );
    }

   /**
    * Return the authentication prompt.
    * Return the prompt value
    */
    public String getPrompt()
    {
        return getValue( "prompt", "" );
    }

   /**
    * Return the bootstrap status of the host.
    * @return TRUE if this is a bootstrap host
    */
    public boolean getBootstrap()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "bootstrap", false );
    }

   /**
    * Return the trusted status.
    * @return TRUE if trusted 
    */
    public boolean getTrusted()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "trusted", false );
    }

   /**
    * Return the enabled status of the host.
    * @return TRUE if enabled 
    */
    public boolean getEnabled()
    {
        Preferences prefs = getPreferences();
        return prefs.getBoolean( "enabled", false );
    }

   /**
    * Return the host priority.
    * @return the host priority setting
    */
    public int getPriority()
    {
        Preferences prefs = getPreferences();
        return prefs.getInt( "priority", 600 );
    }

   /**
    * Return the layout strategy model key.
    * @return the layout model key7
    */
    public String getLayoutModelKey()
    {
        Preferences prefs = getPreferences();
        return getValue( "layout", "classic" );
    }

   /**
    * Return the strategy using to establish a host model.
    * @return the strategy
    */
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

   /**
    * Return the host base path.
    * @return the base path
    */
    public String getBasePath() throws BuilderException
    {
        Preferences prefs = getPreferences();
        return prefs.get( "base", "http://localhost" );
    }

   /**
    * Return the host password authentication credentials.
    * @return the password authentication credentials
    */
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

   /**
    * Return index path.
    * @return the index path
    */
    public String getIndexPath() throws BuilderException
    {
        Preferences prefs = getPreferences();
        return prefs.get( "index", null );
    }

   /**
    * Set the host name.
    * @param name the name
    */
    public void setName( String name )
    {
        setValue( "name", name );
    }

   /**
    * Set the host priority.
    * @param priority the priority value
    */
    public void setPriority( int priority )
    {
        Preferences prefs = getPreferences();
        synchronized( prefs )
        {
            prefs.putInt( "priority", priority );
        }
    }

   /**
    * Set the host layout model identitfier.
    * @param layout the layout key
    */
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

    // ------------------------------------------------------------------------
    // Removable
    // ------------------------------------------------------------------------

   /**
    * Remove the storage unit.
    */
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

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

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
}

