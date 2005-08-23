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

package net.dpml.depot.install;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URI;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Date;

import net.dpml.profile.ApplicationRegistry;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.Artifact;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.util.StreamUtils;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
public class ZipInstaller
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final TransitModel m_model;
    private final TransitRegistryModel m_transit;
    private final ApplicationRegistry m_depot;
    private final String[] m_args;

    private File m_bundle = null;
    private File m_cache = null;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * The PackageInstaller class is a plugin that handles the installation 
    * of a target package via commandline instructions.
    *
    * @param logger the assigned logging channel
    * @param depot the depot application registry
    * @param transit the transit profile registry
    * @param model the current transit model
    * @param args the remaining command line arguments
    * @exception Exception if an error occurs
    */
    public ZipInstaller( 
      Logger logger, ApplicationRegistry depot, TransitRegistryModel transit, 
      TransitModel model, String[] args ) throws Exception
    {
        m_logger = logger;
        m_depot = depot;
        m_transit = transit;
        m_model = model;
        m_args = args;

        m_cache = m_model.getCacheModel().getCacheDirectory();
    }

    private Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Install an platform independent zip bundle.
    * @param artifact the artifact to install
    * @exception Exception if an error occurs during the install process
    */
    synchronized void install( Artifact artifact ) throws Exception
    {
        m_bundle = null;
        try
        {
            URL url = artifact.toURL();
            getLogger().info( "loading: " + url );
            File file = (File) url.getContent( new Class[]{File.class} );
            getLogger().debug( "cached as: " + file );
            ZipFile zip = new ZipFile( file );
            int size = zip.size();
            getLogger().info( "unpacking " + size + " entries" );
            unpack( zip );
            getLogger().info( "update complete" );
        }
        catch( ArtifactNotFoundException e )
        {
            throw e;
        }

        //
        // if the installation declared a bundle then fire it up
        //

        if( m_bundle != null )
        {
            getLogger().debug( "resolving package installer" );
            Properties properties = new Properties();
            try
            {
                FileInputStream input = new FileInputStream( m_bundle );
                properties.load( input );
            }
            catch( IOException e )
            {
                final String error = 
                  "Unable to load installer parameters."
                  + "\n  Cause: " + e.getClass().getName()
                  + "\n  Message: " + e.getMessage()
                  + "\n";
                getLogger().error( error, e );
                throw new HandledException();
            }

            String plugin = properties.getProperty( "dpml.depot.installer.uri", null );
            if( null == plugin )
            {
                final String error = 
                  "Installer does not declare a plugin uri."
                  + "\nBundle: " + m_bundle;
                getLogger().error( error );
                throw new HandledException();
            }

            String classname = properties.getProperty( "dpml.depot.installer.class", null );
            if( null == classname )
            {
                final String error = 
                  "Installer does not declare a classname.\n"
                  + "\nBundle: " + m_bundle;
                getLogger().error( error );
                throw new HandledException();
            }

            getLogger().debug( "  installer: " + plugin );
            getLogger().debug( "  class: [" + classname + "]" );

            try
            {
                Repository loader = Transit.getInstance().getRepository();
                URI uri = new URI( plugin );
                ClassLoader parent = ZipInstaller.class.getClassLoader();
                ClassLoader classloader = loader.getPluginClassLoader( parent, uri );
                Class c = classloader.loadClass( classname );
                Boolean flag = new Boolean( true );
                loader.instantiate( c, new Object[]{m_logger, m_transit, m_depot, m_args, flag} );
                getLogger().info( "installation complete" );
                m_bundle.deleteOnExit();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Installer deployment failure.";
                throw new Exception( error, e );
            }
        }
    }
  
   /**
    * Deinstall an platform independent zip bundle.
    * @param artifact the artifact to deinstall
    * @exception Exception if an error occurs during the deinstall process
    */
    void deinstall( Artifact artifact )
    {
        getLogger().warn( "deinstallation service not available in this version.\n" );
    }

    private void unpack( ZipFile zip ) throws Exception
    {
        Enumeration entries = zip.entries();
        while( entries.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if( !entry.isDirectory() )
            {
                unpackEntry( zip, entry );
            }
        }
    }

    private void unpackEntry( ZipFile zip, ZipEntry entry ) throws Exception
    {
        String name = entry.getName();
        if( name.equals( "BUNDLE" ) )
        {
            long time = new Date().getTime();
            m_bundle = File.createTempFile( "depot-bundle", "" + time );
            expand( zip, entry, m_bundle );
        }
        else if( name.startsWith( "share/" ) )
        {
            //
            // shared resources go into into DPML_SYSTEM
            //

            String path = name.substring( "share/".length() );
            File target = new File( Transit.DPML_SYSTEM, path );
            expand( zip, entry, target );

            //
            // check if its a link and if so scrub the entry from the cache
            //

            if( name.startsWith( "share/local/" ) && name.endsWith( ".link" ) )
            {
                String filename = name.substring( "share/local/".length() );
                File cached = new File( m_cache, filename );
                if( cached.exists() )
                {
                    getLogger().info( "updating cached link: " + filename );
                    cached.deleteOnExit();
                }
            }
        }
        else if( name.startsWith( "data/" ) )
        {
            //
            // data resources go into into DPML_DATA
            //

            String path = name.substring( "data/".length() );
            File target = new File( Transit.DPML_DATA, path );
            expand( zip, entry, target );
        }
        else if( name.startsWith( "prefs/" ) )
        {
            //
            // preferences resources go into into DPML_PREFS
            //

            String path = name.substring( "prefs/".length() );
            File target = new File( Transit.DPML_PREFS, path );
            expand( zip, entry, target );
        }
        else
        {
            //
            // otherwise use DPML_HOME
            //

            File target = new File( Transit.DPML_HOME, name );
            expand( zip, entry, target );
        }
    }

    private void expand( ZipFile zip, ZipEntry entry, File out ) throws Exception
    {
        File parent = out.getParentFile();
        parent.mkdirs();
        //if( out.exists() )
        //{
        //    boolean deleted = out.delete();
        //    if( !deleted )
        //    {
        //        getLogger().warn( "failed to delete: " + out );
        //    }
        //}
        InputStream input = zip.getInputStream( entry );
        OutputStream output = new FileOutputStream( out );
        getLogger().debug( "" + out );
        StreamUtils.copyStream( input, output, true );
    }
}
