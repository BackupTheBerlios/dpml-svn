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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Date;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.Transit;
import net.dpml.transit.TransitException;
import net.dpml.transit.Layout;
import net.dpml.transit.ClassicLayout;
import net.dpml.transit.EclipseLayout;
import net.dpml.transit.CacheHandler;
import net.dpml.transit.util.Util;

import net.dpml.transit.model.CacheModel;

/**
 * The TransitPreferences class is responsible for the setup of initial factory
 * default preference settings.
 */
public class TransitPreferences
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    public static final String HOST_ID_KEY = "dpml.transit.host.id";
    public static final String HOST_BASE_KEY = "dpml.transit.host.base";
    public static final String HOST_INDEX_KEY = "dpml.transit.host.index";
    public static final String HOST_PRIORITY_KEY = "dpml.transit.host.priority";
    public static final String HOST_USERNAME_KEY = "dpml.transit.host.username";
    public static final String HOST_PASSWORD_KEY = "dpml.transit.host.password";
    public static final String HOST_PROMPT_KEY = "dpml.transit.host.prompt";
    public static final String HOST_SCHEME_KEY = "dpml.transit.host.scheme";
    public static final String HOST_LAYOUT_KEY = "dpml.transit.host.layout";
    public static final String HOST_ENABLED_KEY = "dpml.transit.host.enabled";
    public static final String HOST_TRUSTED_KEY = "dpml.transit.host.trusted";
    public static final String HOST_BOOTSTRAP_KEY = "dpml.transit.host.bootstrap";

   /**
    * Default priority string.
    */
    private static final String DEFAULT_PRIORITY_STRING = "10";

   /**
    * Setup the default factory preferences for the Transit system.
    * @param root the preferences node to use for the population of transit preferences
    * @exception BuilderException if a error occcurs during preferences initialization
    */
    public static void setupFactoryPreferences( Preferences root ) throws BuilderException
    {
        ClassLoader classloader = Transit.class.getClassLoader();
        URL cacheUrl = classloader.getResource( "transit/authority/cache.properties" );
        try
        {
            Preferences cache = root.node( "cache" );
            Preferences layout = cache.node( "layout" );
            Preferences hosts = cache.node( "hosts" );
            setupLayoutPreferences( layout );
            setupCachePreferences( cacheUrl, cache );
            setupHostsPreferences( hosts );
            root.putLong( "installation", new Date().getTime() );
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to resolve default transit preferences due to an unexpected error.";
            throw new BuilderException( error, e );
        }
    }

   /**
    * Setup the factory preferences for the Transit system using the value declared by   
    * the authority attribute.
    *
    * @param root the preferences node to use for the population of transit preferences
    * @param authority URL referencing an authorative source
    * @exception BuilderException if a error occcurs during preferences initialization
    */
    public static void setupPreferences( Preferences root, URL authority ) throws BuilderException
    {
        String protocol =  authority.getProtocol();
        Preferences cachePrefs = root.node( "cache" );
        Preferences hostsPrefs = cachePrefs.node( "hosts" );
        Preferences layoutPrefs = cachePrefs.node( "layout" );
        setupLayoutPreferences( layoutPrefs );

        try
        {
            if( "file".equals( protocol ) )
            {
                //
                // convert the url to a local file and work from there
                //
    
                String authorative = authority.toExternalForm().substring( FILE_PROTOCOL_LENGTH );
                while ( authorative.startsWith( "//" ) )
                {
                    authorative = authorative.substring( 1 );
                }
    
                File base = new File( authorative );
                if( !base.exists() )
                {
                    final String error =
                      "Authorative base url references to a location that does not exist."
                      + "\nAuthorative URL: " + authority
                      + "\nResolve authority base directory: " + base;
                    throw new FileNotFoundException( error );
                }
    
                //
                // setup cache preferences
                //
    
                File cacheConfig = new File( base, "cache.properties" );
                if( cacheConfig.exists() )
                {
                    URL cacheUrl = cacheConfig.toURL();
                    setupCachePreferences( cacheUrl, cachePrefs );
                }
    
                //
                // setup host preferences
                //
    
                File hosts = new File( base, "hosts" );
                if( hosts.exists() )
                {
                    File[] files = hosts.listFiles();
                    for( int i=0; i < files.length; i++ )
                    {
                        File file = files[i];
                        String name = file.getName();
                        //if( name.indexOf( "." ) > -1 )
                        //{
                        //    name = name.substring( 0, name.indexOf( "." ) );
                        //}
                        Preferences host = hostsPrefs.node( name );
                        URL hostUrl = fileToURL( file );
                        Properties properties = Util.readProps( hostUrl, false );
                        setupHost( properties, host );
                    }
                }
            }
            else
            {
                URL index = new URL( authority, REMOTE_LIST_FILENAME );
                String[] paths = Util.readListFile( index );
                for( int i=0; i < paths.length; i++ )
                {
                    String path = paths[i];
                    URL spec = null;
                    try
                    {
                        spec = new URL( path );
                    }
                    catch( MalformedURLException e )
                    {
                        spec = new URL( authority, path );
                    }
                    setupHost( hostsPrefs, spec );
                }
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to resolve transit preferences due to an unexpected error.";
            throw new BuilderException( error, e );
        }
    }

   /**
    * Setup the bootrap resolver entries.
    * @param prefs the layout preferences node
    */
    private static void setupLayoutPreferences( Preferences prefs )
    {
        Preferences classic = prefs.node( "classic" );
        classic.put( "title", "Classic Layout" );
        classic.put( "classname", ClassicLayout.class.getName() );
        Preferences eclipse = prefs.node( "eclipse" );
        eclipse.put( "title", "Eclipse Layout" );
        eclipse.put( "classname", EclipseLayout.class.getName() );
    }

    private static void setupCachePreferences( URL resource, Preferences prefs ) 
      throws BuilderException
    {
        try
        {
            Properties properties = Util.readProps( resource, false );
            setupCachePreferences( properties, prefs );
        }
        catch( IOException ioe )
        {
            final String error = 
              "IO exception while attempting to read cache properties."
              + "\nProperties URL: " + resource;
            throw new BuilderException( error, ioe ); 
        }
    }

    private static void setupCachePreferences( Properties properties, Preferences prefs ) 
      throws BuilderException
    {
        String location = properties.getProperty( CacheModel.CACHE_LOCATION_KEY );
        if( null != location )
        {
            prefs.put( "location", location );
        }
        String resolver = properties.getProperty( CacheModel.CACHE_LAYOUT_KEY );
        if( null != resolver )
        {
            prefs.put( "layout", resolver );
        }
    }

    private static void setupHostsPreferences( Preferences prefs ) throws BuilderException
    {
        setupNamedHost( prefs, "local", "transit/authority/hosts/local.host" );
        setupNamedHost( prefs, "repository.dpml.net", "transit/authority/hosts/repository.dpml.net.host" );
        setupNamedHost( prefs, "www.apache.org", "transit/authority/hosts/www.apache.org.host" );
        setupNamedHost( prefs, "www.ibiblio.org", "transit/authority/hosts/www.ibiblio.org.host" );
    }

    private static void setupNamedHost( Preferences prefs, String name, String resource ) throws BuilderException
    {
        URL url = Transit.class.getClassLoader().getResource( resource );
        try
        {
            Preferences host = prefs.node( name );
            Properties properties = Util.readProps( url, false );
            setupHost( properties, host );
        }
        catch( IOException ioe )
        {
            final String error = 
              "IO exception while attempting to read named host properties."
              + "\nHost: " + name
              + "\nURL: " + url;
            throw new BuilderException( error, ioe ); 
        }
    }

    private static void setupHost( Preferences prefs, URL hostDef ) throws BuilderException
    {
        try
        {
            Properties properties = Util.readProps( hostDef, false );
            String id = properties.getProperty( HOST_ID_KEY );
            if( null == id )
            {
                final String error =
                  "Remote host description does not declare '" 
                  + HOST_ID_KEY 
                  + "' property."
                 + "\nHost descriptor URL: " + hostDef;
                throw new BuilderException( error );
            }
            Preferences host = prefs.node( id );
            setupHost( properties, host );
        }
        catch( IOException ioe )
        {
            final String error = 
              "IO exception while attempting to read host properties."
              + "\nURL: " + hostDef ;
            throw new BuilderException( error, ioe ); 
        }
    }

    private static void setupHost( Properties properties, Preferences prefs ) throws BuilderException
    {
        if( null == properties )
        {
            throw new NullArgumentException( "properties" );
        }
        if( null == prefs )
        {
            throw new NullArgumentException( "prefs" );
        }

        String name = prefs.name();
        try
        {
            String base = Util.getProperty( properties, HOST_BASE_KEY, null );
            if( base == null )
            {
                final String error = 
                  "Invalid host defintion (no base url declaration)."
                  + "\nHost ID: " + prefs.name();
                throw new BuilderException( error );
            }
            String index = Util.getProperty( properties, HOST_INDEX_KEY, null );
            String priorityText = Util.getProperty( properties, HOST_PRIORITY_KEY, DEFAULT_PRIORITY_STRING );
            if( "".equals( priorityText ) )
            {
                priorityText = DEFAULT_PRIORITY_STRING;
            }
            int priority = Integer.parseInt( priorityText );
            String username = Util.getProperty( properties, HOST_USERNAME_KEY, "" );
            String password = Util.getProperty( properties, HOST_USERNAME_KEY, "" );
            String prompt = Util.getProperty( properties, HOST_PROMPT_KEY, "" );
            String scheme = Util.getProperty( properties, HOST_SCHEME_KEY, "" );
            String resolverID = Util.getProperty( properties, HOST_LAYOUT_KEY, "classic" );
            boolean enabled =
              Util.getProperty( properties, HOST_ENABLED_KEY, "true" ).equalsIgnoreCase( "true" );
            boolean trusted =
              Util.getProperty( properties, HOST_TRUSTED_KEY, "false" ).equalsIgnoreCase( "true" );
            boolean bootstrap =
              Util.getProperty( properties, HOST_BOOTSTRAP_KEY, "false" ).equalsIgnoreCase( "true" );

            prefs.put( "base", base );
            if( null != index ) 
            {
                prefs.put( "index", index );
            }
            prefs.putInt( "priority", priority );
            prefs.putBoolean( "enabled", enabled ); 
            prefs.putBoolean( "trusted", trusted ); 
            prefs.put( "username", username ); // TODO
            prefs.put( "password", password ); // TODO
            prefs.put( "scheme", scheme );
            prefs.put( "layout", resolverID );
            prefs.putBoolean( "available", true );
            prefs.putBoolean( "bootstrap", bootstrap );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error occured while attempting to construct a default host preferences."
              + "\nHost Name: " + name
              + "\nProperties: " + properties;
            throw new BuilderException( error, e );
        }
    }

    private static File convertToFile( URL url ) throws BuilderException
    {
        String protocol = url.getProtocol();
        if( "file".equals( protocol ) )
        {
            String spec = url.toExternalForm();
            String s = spec.substring( 6 );
            return new File( s );
        }
        else
        {
            final String error = 
              "Don't know how to convert the '" + protocol + "' protocol to a file.";
            throw new BuilderException( error );
        }
    }

   /**
    * Disabled.
    */
    private TransitPreferences()
    {
    }

   /**
    * File protocol name length.
    */
    private static final int FILE_PROTOCOL_LENGTH = 5;

   /**
    * Filename of the remote file index.
    */
    private static final String REMOTE_LIST_FILENAME = "index.lst";

   /**
    * Return the url of a file.
    * @param file the file to convert
    * @return the equivalent url
    * @exception BuilderException if a error occurs in file to url conversion
    */
    private static URL fileToURL( File file ) throws BuilderException
    {
        try
        {
            return file.toURL();
        }
        catch( MalformedURLException mue )
        {
            final String error =
              "The file ["
              + file
              + "] ("
              + file.exists()
              + ") raised a malformed url exception with the message '"
              + mue.getMessage()
              + "'.";
            throw new BuilderException( error );
        }
    }

    private static URL resolveAuthority( Preferences prefs ) throws BuilderException
    {
        String authority = prefs.get( "authority", null );
        if( null == authority )
        {
            final String error = 
              "Authority attribute is undefined."
              + "\nPreferences: " + prefs;
            throw new BuilderException( error );
        }
        try
        {
            if( authority.indexOf( ":" ) > -1 )
            {
                return new URL( authority );
            }
            else
            {
                return new File( authority ).toURL();
            }
        }
        catch( IOException e )
        {
            final String error = 
              "Unable to resolve the Transit authority url."
              + "\nPreferences: " + prefs;
            throw new BuilderException( error, e ); 
        }
    }
}

