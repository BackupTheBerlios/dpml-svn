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

package net.dpml.depot.audit;

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
public class AuditProcessor implements Runnable
{

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
    public AuditProcessor( Logger logger, String[] args, ShutdownHandler handler, Preferences prefs ) 
      throws Exception
    {
        super();

        m_logger = logger;
        m_args = args;
        m_handler = handler;

        Repository repository = Transit.getInstance().getRepository();
        ClassLoader classloader = getClass().getClassLoader();
        URI uri = new URI( DEPOT_PROFILE_URI );
        m_depot = (DepotProfile) repository.getPlugin( classloader, uri, new Object[]{ prefs, logger } );

        TransitStorageHome home = new TransitStorageHome();
        m_transit = new DefaultTransitRegistryModel( logger, home );
    }

    public void run()
    {
        getLogger().debug( "processing [" + m_args.length + "] command options" );
        m_handler.exit();
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private void handleHelp()
    {
        final String message = 
          "Audit Help"
          + "\n"
          + "\nUsage: depot -audit"
          + "\nDefault: depot -audit "
          + "\n";
        getLogger().info( message );
    }

    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    private static final String VERSION = "@VERSION@";
    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";
}
