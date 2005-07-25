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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import net.dpml.depot.profile.DepotProfile;
import net.dpml.depot.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.Artifact;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.model.Logger;
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
    private final TransitRegistryModel m_transit;
    private final DepotProfile m_depot;

    private File m_bundle = null;

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
    */
    public ZipInstaller( Logger logger, DepotProfile depot, TransitRegistryModel transit ) throws Exception
    {
        m_logger = logger;
        m_depot = depot;
        m_transit = transit;
    }

    public synchronized void install( Artifact artifact ) throws Exception
    {
        m_bundle = null;

        try
        {
            URL url = artifact.toURL();
            System.out.println( "\n  loading: " + url );
            File file = (File) url.getContent( new Class[]{ File.class } );
            System.out.println( "  cached to: " + file );
            ZipFile zip = new ZipFile( file );
            int size = zip.size();
            System.out.println( "  unpacking " + size + " entries\n" );
            unpack( zip );

            System.out.println( "\n  content update complete" );

        }
        catch( ArtifactNotFoundException e )
        {
            System.out.println( "  Artifact not found.\n" );
            throw new HandledException();
        }

        //
        // if the installation declared a bundle then fire it 
        //

        if( m_bundle != null )
        {
            System.out.println( "  resolving package installer" );
            Properties properties = new Properties();
            try
            {
                FileInputStream input = new FileInputStream( m_bundle );
                properties.load( input );
            }
            catch( IOException e )
            {
                final String error = 
                  "  Unable to load installer parameters."
                  + "\n  Cause: " + e.getClass().getName()
                  + "\n  Message: " + e.getMessage()
                  + "\n";
                System.out.println( error );
                throw new HandledException();
            }

            String plugin = properties.getProperty( "dpml.depot.installer.uri", null );
            if( null == plugin )
            {
                final String error = 
                  "  ERROR: Installer does not declare a plugin uri.\n";
                System.out.println( error );
                throw new HandledException();
            }

            String classname = properties.getProperty( "dpml.depot.installer.class", null );
            if( null == classname )
            {
                final String error = 
                  "  ERROR: Installer does not declare a classname.\n";
                System.out.println( error );
                throw new HandledException();
            }

            System.out.println( "  installer: " + plugin );
            System.out.println( "  class: [" + classname + "]" );

            try
            {
                Repository loader = Transit.getInstance().getRepository();
                URI uri = new URI( plugin );
                ClassLoader parent = ZipInstaller.class.getClassLoader();
                ClassLoader classloader = loader.getPluginClassLoader( parent, uri );
                Class c = classloader.loadClass( classname );
                Boolean flag = new Boolean( true );
                loader.instantiate( c, new Object[]{ m_logger, m_transit, m_depot, flag } );
                System.out.println( "  Installation complete.\n" );
            }
            catch( Throwable e )
            {
                final String error = 
                  "  ERROR: Installer deployment failure.";
                System.out.println( error );
                e.printStackTrace();
                throw new HandledException();
            }
        }
    }
  
    public void deinstall( Artifact artifact )
    {
        System.out.println( "\n  WARNING: deinstallation service not available in this version.\n" );
    }

    private void unpack( ZipFile zip ) throws Exception
    {
        Enumeration entries = zip.entries();
        while( entries.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if( false == entry.isDirectory() )
            {
                unpackEntry( zip, entry );
            }
        }
    }

    private void unpackEntry( ZipFile zip, ZipEntry entry ) throws Exception
    {
        File out = getDestination( entry );
        File parent = out.getParentFile();
        parent.mkdirs();
        InputStream input = zip.getInputStream( entry );
        OutputStream output = new FileOutputStream( out );
        System.out.println( "" + out );
        StreamUtils.copyStream( input, output, true );
    }

    private File getDestination( ZipEntry entry ) throws Exception
    {
        String name = entry.getName();
        if( name.equals( "BUNDLE" ) )
        {
            long time = new Date().getTime();
            m_bundle = File.createTempFile( "depot-bundle", "" + time );
            return m_bundle;
        }
        else if( name.startsWith( "share/" ) )
        {
            //
            // shared resources go into into DPML_SYSTEM
            //

            String path = name.substring( 6 );
            return new File( Transit.DPML_SYSTEM, path );
        }
        else if( name.startsWith( "data/" ) )
        {
            //
            // data resources go into into DPML_DATA
            //

            String path = name.substring( 5 );
            return new File( Transit.DPML_DATA, path );
        }
        else if( name.startsWith( "prefs/" ) )
        {
            //
            // preferences resources go into into DPML_PREFS
            //

            String path = name.substring( 6 );
            return new File( Transit.DPML_PREFS, path );
        }
        else
        {
            //
            // otherwise use DPML_HOME
            //

            return new File( Transit.DPML_HOME, name );
        }
    }
}
