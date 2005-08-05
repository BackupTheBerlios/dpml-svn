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
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.prefs.Preferences;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitError;
import net.dpml.transit.util.PropertyResolver;

/**
 * The TransitStorageUnit is responsible for the maintenance of persistent
 * storage units for all Transit subsystems.
 */
public class TransitStorageUnit extends AbstractStorageUnit implements TransitStorage
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final ProxyStorage m_proxy;
    private final CacheHome m_cache;
    private final LayoutRegistryHome m_layout;
    private final ContentRegistryHome m_content;
    private final RepositoryStorageUnit m_repository;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new Transit storage unit using the default profile.
    */
    public TransitStorageUnit()
    {
        this( getDefaultProfileName() );
    }

   /**
    * Creation of a new Transit storage unit using the supplied
    * profile name.  If the profile does not exist the implementation will 
    * create and return a new profile with the supplied name as the 
    * profile identifier.
    *
    * @param profile the profile name
    *
    */
    public TransitStorageUnit( String profile )
    {
        this( getTransitPreferences( profile ) );
    }

   /**
    * Creation of a new Transit storage unit using an authorative 
    * URL as the source for the inital parameterization of the data structure.
    * The underlying stroage system will be based on a non-persistent 
    * preference implementation.
    *
    * @param url the authorative url
    */
    public TransitStorageUnit( URL url )
    {
        this( getLocalPreferences( url ) );
    }

   /**
    * Creation of a new Transit storage unit using a supplied prefernces
    * node.
    *
    * @param prefs the preferences node use as the storage solution
    */
    public TransitStorageUnit( Preferences prefs )
    {
        super( prefs );

        try
        {
            Preferences proxy = getPreferences().node( "proxy" );
            m_proxy = new ProxyStorageUnit( proxy );
            Preferences cache = getPreferences().node( "cache" );
            m_cache = new CacheStorageUnit( cache );
            Preferences layout = getPreferences().node( "cache" ).node( "layout" );
            m_layout = new LayoutRegistryStorageUnit( layout );
            Preferences content = getPreferences().node( "content" );
            m_content = new ContentRegistryStorageUnit( content );
            Preferences repo = getPreferences().node( "repository" );
            m_repository = new RepositoryStorageUnit( repo );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error occured whuile constructing a Transit storage unit."
              + "\nBacking store: " + prefs;
            throw new TransitError( error, e );
        }
    }

    // ------------------------------------------------------------------------
    // TransitStorage
    // ------------------------------------------------------------------------

   /**
    * Return the Transit storage instance identifier.
    * @return the transit persistent id
    */
    public String getID()
    {
        return getPreferences().name();
    }

   /**
    * Return the proxy strorage unit.
    * @return the proxy storage
    */
    public ProxyStorage getProxyStorage()
    {
        return m_proxy;
    }

   /**
    * Return the cache strorage unit.
    * @return the proxy storage
    */
    public CacheHome getCacheHome()
    {
        return m_cache;
    }

   /**
    * Return the layout registry storage unit.
    * @return the layout registry storage
    */
    public LayoutRegistryHome getLayoutRegistryHome()
    {
        return m_layout;
    }

   /**
    * Return the content registry storage unit.
    * @return the content storage
    */
    public ContentRegistryHome getContentRegistryHome()
    {
        return m_content;
    }

   /**
    * Return the repository storage unit.
    * @return the repository storage
    */
    public CodeBaseStorage getRepositoryStorage()
    {
        return m_repository;
    }

    private static Preferences getTransitPreferences( String profile )
    {
        URL authority = getAuthority();
        if( null == authority )
        {
            Preferences root = Preferences.userNodeForPackage( Transit.class );
            Preferences prefs = root.node( "profiles" ).node( profile );
            long install = prefs.getLong( "installation", -1 );
            if( install < 0 )
            {
                TransitPreferences.setupFactoryPreferences( prefs );
            }
            return prefs;
        }
        else
        {
           return getLocalPreferences( authority );
        }
    }

    private static String getDefaultProfileName()
    {
        return System.getProperty( PROFILE_KEY, DEFAULT_PROFILE_NAME );
    }

    private static Preferences getLocalPreferences( URL url )
    {
        LocalPreferences root = new LocalPreferences( null, "" );
        String selection = getDefaultProfileName();
        Preferences prefs = new LocalPreferences( root, selection );
        TransitPreferences.setupPreferences( prefs, url );
        return prefs;
    }

    private static final String DEFAULT_PROFILE_NAME = "default";
    private static final String PROFILE_KEY = "dpml.transit.profile";
    private static final String AUTHORITY_KEY = "dpml.transit.authority";

    private static URL getAuthority()
    {
        String auth = System.getProperty( AUTHORITY_KEY, null );
        if( null != auth )
        {
            Properties properties = new Properties( System.getProperties() );
            properties.setProperty( Transit.HOME_KEY, Transit.DPML_HOME.toString() );
            properties.setProperty( Transit.PREFS_KEY, Transit.DPML_PREFS.toString() );
            properties.setProperty( Transit.DATA_KEY, Transit.DPML_DATA.toString() );
            properties.setProperty( Transit.SYSTEM_KEY, Transit.DPML_SYSTEM.toString() );
            String path = PropertyResolver.resolve( properties, auth );
            try
            {
                return new URL( path );
            }
            catch( MalformedURLException e )
            {
                final String error = 
                  "Invaid authority url: " + path;
                throw new TransitError( error, e );
            }
        }
        else
        {
            return null;
        }
    }
}


