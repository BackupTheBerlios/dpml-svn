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
import java.net.URL;
import java.util.Properties;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import net.dpml.depot.profile.DepotProfile;
import net.dpml.depot.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.artifact.UnsupportedSchemeException;
import net.dpml.transit.artifact.MissingGroupException;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.DefaultTransitRegistryModel;
import net.dpml.transit.model.Logger;
import net.dpml.transit.unit.TransitStorageHome;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
public class PackageInstaller implements Runnable
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final TransitRegistryModel m_transit;
    private final DepotProfile m_depot;
    private final String[] m_args;
    private final Properties m_properties;

    private boolean m_continue = true;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * The PackageInstaller class is a plugin that handles the installation 
    * of a target package via commandline instructions.
    *
    * @param logger the assigned logging channel
    * @param depot the profile application and activatable server registry 
    * @param args commandline args
    */
    public PackageInstaller( Logger logger, DepotProfile depot, Properties properties, String[] args ) throws Exception
    {
        super();

        m_depot = depot;
        m_logger = logger;
        m_args = args;
        m_properties = properties;

        TransitStorageHome home = new TransitStorageHome();
        m_transit = new DefaultTransitRegistryModel( logger, home );
    }

    public void run()
    {
        if( m_args.length > 0 )
        {
            if( m_args.length == 1 )
            {
                m_continue = false;

                String flag = m_properties.getProperty( "dpml.depot.install", "true" );
                boolean isInstallation = "true".equals( flag );
                String arg = m_args[0];
                try
                {
                    processCommand( isInstallation, arg );
                }
                catch( HandledException e )
                {
                }
                catch( Exception e )
                {
                    System.out.println( e.toString() );
                }
            }
            else
            {
                final String error = 
                  "  ERROR: Invalid command: too many parameters.\n";
                System.out.println( error );
            }
        }

        while( m_continue )
        {
            try
            {
                String command = getInputString( "INSTALL> " );
                if( command.length() > 0 )
                {
                    processCommand( command );
                }
            }
            catch( HandledException e )
            {
            }
            catch( Exception e )
            {
                System.out.println( e.toString() );
            }
        }

        System.exit( 0 );
    }

    private void processCommand( String command ) throws Exception
    {
        processCommand( true, command );
    }

    private void processCommand( boolean flag, String command ) throws Exception
    {
        if( "exit".equals( command ) )
        {
            m_continue = false;
        }
        else if( "list".equals( command ) )
        {
            processListCommand();
        }
        else if( command.startsWith( "install " ) )
        {
            String args = command.substring( "install ".length() );
            processInstallCommand( true, args );
        }
        else if( command.startsWith( "remove " ) )
        {
            String args = command.substring( "remove ".length() );
            processInstallCommand( false, args );
        }
        else if( "magic".equals( command ) )
        {
            processInstallCommand( flag, "magic" );
        }
        else if( "metro".equals( command ) )
        {
            processInstallCommand( flag, "metro" );
        }
        else if( "help".equals( command ) )
        {
            System.out.println( "\n  Depot Installation Manager (Command Help)" );
            System.out.println( "\n  Available commands:\n" );
            System.out.println( "    list           -- list installed applications" );
            System.out.println( "    install magic  -- installs the magic build system" );
            System.out.println( "    install metro  -- installs the metro container" );
            System.out.println( "    install [urn]  -- installs an application" );
            System.out.println( "    remove magic   -- deinstall magic" );
            System.out.println( "    remove metro   -- deinstall metro" );
            System.out.println( "    remove [urn]   -- deinstalls an application" );
            System.out.println( "    exit           -- exit the manager" );
            System.out.println( "\n" );
        }
        else
        {
            System.out.println( "  Invalid command: [" + command + "]" );
        }
    }

    private void processListCommand()
    {
        System.out.println( "\n  Listing installed applications:\n" );
        try
        {
            ApplicationProfile[] applications = m_depot.getApplicationProfiles();
            for( int i=0; i<applications.length; i++ )
            {
                ApplicationProfile profile = applications[i];
                System.out.println( "\t" + profile.getID()
                  + "\t" + profile.getCodeBaseURI() );
            }
        }
        catch( Exception e )
        {
            final String error = "Internal error while processing application list.";
            m_logger.error( error, e );
        }
        System.out.println( "" );
    }

    private void processInstallCommand( boolean flag, String args ) throws Exception
    {
        if( "magic".equals( args ) )
        {
            processInstallCommand( flag, "@MAGIC_BUNDLE_URI@" );
        }
        else if( "metro".equals( args ) )
        {
            processInstallCommand( flag, "@METRO_BUNDLE_URI@" );
        }
        else
        {
            Artifact artifact = resolveArtifact( args );
            String type = artifact.getType();
            if( "zip".equals( type ) )
            {
                installZipBundle( flag, artifact );
            }
            else
            {
                final String error =
                  "  Artifact type [" + type + "] is not supported at this time.\n";
                System.out.println( error );
                throw new HandledException();
            }
        }
        System.out.println( "" );
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

    private void installZipBundle( boolean flag, Artifact artifact ) throws Exception
    {
        ZipInstaller installer = new ZipInstaller( m_logger, m_depot, m_transit );
        if( flag )
        {
            installer.install( artifact );
        }
        else
        {
            installer.deinstall( artifact );
        }
    }

    //--------------------------------------------------------------------------
    // stuic utils
    //--------------------------------------------------------------------------

    private static BufferedReader m_INPUT = new BufferedReader( new InputStreamReader( System.in ) );

    private static String getInputString( String prompt ) throws IOException
    {
        System.out.print( prompt );
        return m_INPUT.readLine();
    }
}
