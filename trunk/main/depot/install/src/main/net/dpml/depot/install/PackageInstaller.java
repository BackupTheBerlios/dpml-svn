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
import java.io.InputStreamReader;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import net.dpml.depot.ShutdownHandler;

import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.DepotProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;
import net.dpml.transit.UnsupportedSchemeException;
import net.dpml.transit.MissingGroupException;
import net.dpml.transit.Repository;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.DefaultTransitRegistryModel;
import net.dpml.transit.model.Logger;
import net.dpml.transit.store.TransitStorageHome;

/**
 * The DepotInstaller is responsible for the establishment and integrity of 
 * the DPML installation.
 */
public class PackageInstaller implements Runnable
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    private static final String PROFILE_OPT = "-profile";
    private static final String VERSION_OPT = "-version";
    private static final String STANDARD_PROFILE = "standard";
    private static final String MAGIC_PROFILE = "magic";
    private static final String METRO_PROFILE = "metro";
    private static final String VERSION = "@VERSION@";

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final TransitRegistryModel m_transit;
    private final DepotProfile m_depot;
    private final String[] m_args;

    private ShutdownHandler m_handler;

    private boolean m_continue = true;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * The PackageInstaller class is a plugin that handles the installation 
    * of a target package via commandline instructions.
    *
    * @param logger the assigned logging channel
    * @param args suplimentary commandline arguments
    * @param handler the shutdown handler
    */
    public PackageInstaller( Logger logger, String[] args, ShutdownHandler handler, Preferences prefs ) 
      throws Exception
    {
        super();

        m_logger = logger;
        m_args = args;
        m_handler = handler;

        Repository repository = Transit.getInstance().getRepository();
        ClassLoader classloader = PackageInstaller.class.getClassLoader();
        URI uri = new URI( DEPOT_PROFILE_URI );
        m_depot = (DepotProfile) repository.getPlugin( classloader, uri, new Object[]{ prefs, logger } );

        TransitStorageHome home = new TransitStorageHome();
        m_transit = new DefaultTransitRegistryModel( logger, home );
    }

    public void run()
    {
        getLogger().debug( "processing [" + m_args.length + "] command options" );
        boolean reset = isFlagPresent( "-reset" );

        try
        {
            String version = getSetupVersion();
            if( VERSION.equals( version ) && false == reset )
            {
                //
                // we are working with the installed Depot system and rest has 
                // not been requested so we go ahead with the install based on 
                // this current classloader stack
                //

                getLogger().info( "initiating depot setup" );

                String profile = getProfileName();
                if( STANDARD_PROFILE.equals( profile ) || METRO_PROFILE.equals( profile ) )
                {
                    getLogger().info( "invoking standard profile setup" );
                    installMagic();
                    installMetro();
                }
                else if( MAGIC_PROFILE.equals( profile ) )
                {
                    getLogger().info( "invoking magic profile setup" );
                    installMagic();
                }
                else
                {
                    final String error = 
                      "Requested profile is not recognized. "  
                      + "Recognized profiles include 'standard', 'magic' and 'metro'.";
                    getLogger().error( error );
                    m_handler.exit( -1 );
                }
            }
            else
            {
                //
                // the request is for the setup of Depot using a different version
                // so we need to install the requested Depot version and launch
                // another process so that we pick up the version of Depot and Transit
                // classes corresponding to the install version argument
                //

                String spec = "artifact:zip:dpml/depot/dpml-depot#" + version;
                install( spec, getLogger(), new String[0] );
                getLogger().info( "switching to version: " + version );

                String[] command = new String[]{ 
                     "java",
                     "-Djava.ext.dirs=lib",
                     "net.dpml.depot.lang.Main",
                     "-setup",
                     "-postprocess" };
                File dir = Transit.DPML_DATA;
                getLogger().info( "launching process" );
                Process process = Runtime.getRuntime().exec( command, null, dir );
                StreamReader out = new StreamReader( process.getInputStream() );
                StreamReader err = new StreamReader( process.getErrorStream() );
                out.start();
                err.start();
                int result = process.waitFor();
                getLogger().info( "result: " + result );
            }
        }
        catch( IllegalArgumentException e )
        {
            getLogger().error( e.getMessage() );
            m_handler.exit( -1 );
        }
        catch( ArtifactNotFoundException e )
        {
            URI uri = e.getURI();
            getLogger().error( "Resource not found: [" + uri + "]" );
            m_handler.exit( -1 );
        }
        catch( Throwable e )
        {
            final String error = 
              "Installation failure.";
            getLogger().error( error, e );
            m_handler.exit( -1 );
        }

        getLogger().info( "setup procedure complete" );
        m_handler.exit();
    }

    private void installMagic() throws Exception
    {
        String spec = "@MAGIC_BUNDLE_URI@";
        Logger logger = getLogger().getChildLogger( "magic" );
        install( spec, logger, new String[0] );
    }

    private void installMetro() throws Exception
    {
        String spec = "@METRO_BUNDLE_URI@";
        Logger logger = getLogger().getChildLogger( "metro" );
        install( spec, logger, new String[0] );
    }

    private class StreamReader extends Thread
    {
        InputStream m_input;
        boolean m_error = false;
        
        public StreamReader( InputStream input )
        {
            m_input = input;
        }
  
        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader( m_input );
                BufferedReader reader = new BufferedReader( isr );
                String line = null;
                while( ( line = reader.readLine() ) != null )
                {
                    System.out.println( line );
                }
            }
            catch( IOException e )
            {
                 getLogger().error( "Process read error.", e );
            }
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private String getProfileName()
    {
        if( m_args.length == 0 )
        {
            return STANDARD_PROFILE;
        }
        else
        {
            for( int i=0; i<m_args.length; i++ )
            { 
                String arg = m_args[i];
                if( arg.equals( PROFILE_OPT ) )
                {
                    if( i+1 < m_args.length )
                    {
                        return m_args[ i+1 ];
                    }
                    else
                    {
                        final String error = 
                          "Invalid commandline. The -profile option must be followed by a profile name.";
                        throw new IllegalArgumentException( error );
                    }
                }
            }
        }
        return STANDARD_PROFILE;
    }

    private String getSetupVersion()
    {
        if( m_args.length == 0 )
        {
            return VERSION;
        }
        else
        {
            for( int i=0; i<m_args.length; i++ )
            { 
                String arg = m_args[i];
                if( arg.equals( VERSION_OPT ) )
                {
                    if( i+1 < m_args.length )
                    {
                        return m_args[ i+1 ];
                    }
                    else
                    {
                        final String error = 
                          "Invalid commandline. The -version option must be followed by a version value.";
                        throw new IllegalArgumentException( error );
                    }
                }
            }
        }
        return VERSION;
    }

    private boolean isFlagPresent( String flag )
    {
        for( int i=0; i < m_args.length; i++ )
        {
            String arg = m_args[i];
            if( arg.equals( flag ) )
            {
                return true;
            }
        }
        return false;
    }

    private void install( String spec, Logger logger, String[] args ) throws Exception
    {
        Artifact artifact = resolveArtifact( spec );
        String type = artifact.getType();
        if( "zip".equals( type ) )
        {
            installZipBundle( true, artifact, logger, args );
        }
        else
        {
            final String error =
              "Artifact [" + spec + "] is not zip bundle.\n";
            throw new IllegalArgumentException();
        }
    }

    private Artifact resolveArtifact( String arg ) throws Exception
    {
        try
        {
            return Artifact.createArtifact( arg );
        }
        catch( UnsupportedSchemeException e )
        {
            System.out.println( "\n  ERROR: " + e.getMessage() + "\n" );
            throw new HandledException();
        }
        catch( MissingGroupException e )
        {
            System.out.println( "\n  ERROR: " + e.getMessage() + "\n" );
            throw new HandledException();
        }
        catch( URISyntaxException e )
        {
            System.out.println( "\n  ERROR: " + "Could not convert argument to a uri." );
            System.out.println( "  " + e.toString() + "\n" );
            throw new HandledException();
        }
        catch( Throwable e )
        {
            m_logger.error( "Invalid install argument [" + arg + "]", e );
            throw new HandledException();
        }
    }

    private void installZipBundle( boolean flag, Artifact artifact, Logger logger, String[] args ) throws Exception
    {
        ZipInstaller installer = new ZipInstaller( logger, m_depot, m_transit, args );
        if( flag )
        {
            installer.install( artifact );
        }
        else
        {
            installer.deinstall( artifact );
        }
    }

    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";
}
