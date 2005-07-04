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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import net.dpml.depot.profile.DepotProfile;
import net.dpml.depot.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.Logger;
import net.dpml.transit.unit.TransitStorageHome;
import net.dpml.transit.model.DefaultTransitRegistryModel;

/**
 * Table model that maps table rows to child nodes of a supplied preferences node.
 */
public class PackageInstaller implements Runnable
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final TransitRegistryModel m_transit;
    private final DepotProfile m_depot;

    private boolean m_continue = true;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * The PackageInstaller class is a plugin that handles the installation 
    * of a target package via commandline instructions.
    *
    * @param transit the registry of available transit profiles
    * @param depot the profile application and activatable server registry 
    */
    public PackageInstaller( Logger logger, DepotProfile depot, String[] args ) throws Exception
    {
        super();

        m_depot = depot;
        TransitStorageHome home = new TransitStorageHome();
        m_transit = new DefaultTransitRegistryModel( logger, home );
    }

    public void run()
    {
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
            catch( IOException e )
            {
                System.out.println( e.toString() );
                m_continue = false;
            }
        }

        System.exit( 0 );
    }

    private void processCommand( String command )
    {
        if( "exit".equals( command ) )
        {
            m_continue = false;
        }
        else if( "list".equals( command ) )
        {
            System.out.println( "\nListing installed applications:\n" );
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
                System.out.println( "Internal error while processing application list." );
                System.out.println( e.toString() );
            }
            System.out.println( "" );
        }
        else if( "install".equals( command ) )
        {
            System.out.println( "  Implementation pending: " + command );
        }
        else if( "help".equals( command ) )
        {
            System.out.println( "\n  Depot Installation Manager (Command Help)" );
            System.out.println( "\n" );
            System.out.println( HELP );
            System.out.println( "\n  Available commands:" );
            System.out.println( "\n    exit  -- exit the session" );
            System.out.println( "    list  -- list installed applications" );
            System.out.println( "    install [key] [urn]  -- install a new application" );
            System.out.println( "\n" );
        }
        else
        {
            System.out.println( "Invalid command: " + command );
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

    private static final String HELP = 
      "  This is a minimalist commandline processor that should "
      + "\n  eventually handle package installation. Typical "
      + "\n  scenarios are the installation of Magic or the "
      + "\n  installation of something like the Metro HTTP "
      + "\n  facility.  This package is also intended to provide "
      + "\n  an example of a small Depot plugin suitable for "
      + "\n  other CLI based scenarios such as configuration "
      + "\n  updates, listing, etc.";
}
