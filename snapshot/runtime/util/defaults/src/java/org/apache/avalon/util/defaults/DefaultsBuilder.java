/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.util.defaults;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.avalon.util.env.Env;


/**
 * A utility class that provides support for the establishment
 * of a set of installation properties.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultsBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultsBuilder
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

   /**
    * Return a home directory taking into account a supplied env symbol, 
    * a property key and a fallback directory.
    *
    * If the supplied key references a known system property
    * value of '[key].home' then that value will be used to establish the home 
    * directory. Otherwise, if the supplied env symbol cannot be resolved
    * to a value, a directory corresponding to ${user.home}/.[key]
    * will be returned.
    *
    * @param key an application key such as 'merlin'
    * @return the derived directory
    */
    public static File getHomeDirectory( String key ) throws IOException
    {
        final String homeKey = key + ".home";
        final String symbol = key.toUpperCase() + "_HOME";
        final String home = 
          System.getProperty( 
            homeKey, 
            getEnvironmentVariable( symbol ) );

        if( null != home )
        {
            return new File( home ).getCanonicalFile();
        }
        else
        {
            final File user = 
              new File( System.getProperty( "user.home" ) );
            final String path = "." + key;
            return new File( user, path ).getCanonicalFile();
        }
    }

    private static String getEnvironmentVariable( String symbol )
    {
        try
        {
            return Env.getEnvVariable( symbol );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

   /**
    * Create a installation properties object.  The implementation
    * will create a new properties object and read in a properties 
    * file if it exists relative to the filename [home]/[key].properties.  
    * Before returning the properties object the home directory will be 
    * assigned as the value of a property name [key].home if the supplied
    * flag argument is TRUE.
    *
    * @param home the home directory
    * @param key the application key 
    * @param flag if TRUE set the property '[key].home' to the home directory 
    * @return the application properties object
    */
    public static Properties getHomeProperties( 
      File home, String key, boolean flag ) throws IOException
    {
        Properties properties = getProperties( home, key );
        if( flag )
        {
            final String name = key + ".home";
            final String path = home.getCanonicalPath();
            properties.setProperty( name, path );
        }
        return properties;
    }

   /**
    * Create a user properties object.  The implementation
    * will create a new properties object and read in a properties 
    * file if it exists relative to the filename ${user.home}/[key].properties.  
    *
    * @param key the application key 
    * @return the user properties object
    */
    public static Properties getUserProperties( 
      String key ) throws IOException
    {
        final File user = new File( System.getProperty( "user.home" ) );
        return getProperties( user, key );
    }

   /**
    * Create a dir properties object.  The implementation
    * will create a new properties object and read in a properties 
    * file if it exists relative to [dir]/[key].properties.  
    *
    * @param dir the base directory 
    * @param key the application key 
    * @return the user properties object
    */
    public static Properties getProperties( 
      File dir, String key ) throws IOException
    {
        final String filename = key + ".properties";
        final File file = new File( dir, filename );
        return getProperties( file );
    }

   /**
    * Create a properties object from the supplied file.  If 
    * the file does not exists an empty property object will be 
    * returned.
    *
    * @param file the properties file
    * @return the properties object
    */
    public static Properties getProperties( File file ) throws IOException
    {
        if( null == file )
        {
            throw new NullPointerException( "file" );
        }

        Properties properties = new Properties();
        if( file.exists() )
        {
            properties.load( 
              new FileInputStream( file ) );
        }   
        return properties;
    }

    public static Properties getProperties( 
      ClassLoader classloader, String path ) throws IOException
    {
        Properties properties = new Properties();
        InputStream input = 
          classloader.getResourceAsStream( path );
        if( input != null ) 
        {
            properties.load( input );
        }
        return properties;
    }

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final String m_key;

    private final File m_work;

    private final File m_root;

    private final Properties m_home;

    private final Properties m_user;

    private final Properties m_dir;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public DefaultsBuilder( final String key, File work ) throws IOException
    {
        m_key = key;
        m_work = work;
        m_root = getHomeDirectory( m_key );
        m_home = getHomeProperties( m_root, m_key, true );
        m_user = getUserProperties( m_key );
        m_dir = getProperties( m_work, m_key );
    }

    //--------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------

   /**
    * Return the application home directory.
    * @return the home directory
    */
    public File getHomeDirectory()
    {
        return m_root;
    }

   /**
    * Return the application properties.  Properties returned are resolved
    * relative to a properties file named [key].properties in the 
    * application home directory.
    *
    * @return the home directory
    */
    public Properties getHomeProperties()
    {
        return m_home;
    }

   /**
    * Return the application specific properties from the users home directory.
    * @return the user's application properties
    */
    public Properties getUserProperties()
    {
        return m_user;
    }

   /**
    * Return the application specific properties from the current working directory.
    * @return the working application properties
    */
    public Properties getDirProperties()
    {
        return m_dir;
    }

   /**
    * Return a consolidated set of properties.
    * @param defaults the source properties
    * @param keys the simple property keys
    * @return the consolidated properties
    */
    public Properties getConsolidatedProperties( 
      final Properties defaults, final String[] keys ) throws IOException
    {
        return getConsolidatedProperties( defaults, keys, new String[0] );
    }

   /**
    * Return a consolidated set of properties.
    * @param defaults the source properties
    * @param keys the simple property keys
    * @param sequence the sequence property keys
    * @return the consolidated properties
    */
    public Properties getConsolidatedProperties( 
      final Properties defaults, final String[] keys, String[] sequence ) throws IOException
    {
        final Properties[] parameters = 
          new Properties[] { 
            defaults,
            m_home,
            m_user,
            m_dir };
        final DefaultsFinder[] finders = 
          new DefaultsFinder[]{
            new SimpleDefaultsFinder( 
              parameters, 
              false ), 
            new SystemDefaultsFinder() 
          };
        return new Defaults( keys, sequence, finders );
    }
}
