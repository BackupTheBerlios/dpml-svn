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

import java.net.URI;
import java.util.prefs.Preferences;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import net.dpml.depot.ShutdownHandler;

import net.dpml.profile.ApplicationRegistry;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.DefaultTransitRegistryModel;
import net.dpml.transit.Logger;
import net.dpml.transit.store.TransitStorageHome;

/**
 * Command handler that lists information about installed applications.
 */
public class AuditProcessor
{

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final TransitRegistryModel m_transit;
    private final Registry m_registry;
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
    * @param prefs the root depot prefs
    * @exception Exception if an error occurs
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
        m_registry = (Registry) repository.getPlugin( classloader, uri, new Object[]{prefs, logger} );

        TransitStorageHome home = new TransitStorageHome();
        m_transit = new DefaultTransitRegistryModel( logger, home );
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( classloader );

        try
        {
            Registry registry = LocateRegistry.getRegistry( Registry.REGISTRY_PORT );
            String[] names = registry.list();
            logger.info( "registry entries: " + names.length );
            for( int i=0; i<names.length; i++ )
            {
                String message = "entry (" + (i+1) + "): " + names[i];
                try
                {
                    Object obj = registry.lookup( names[i] );
                    message = message.concat( ", " + obj.getClass().getName() );
                }
                catch( Throwable e )
                {
                    message = message.concat( " [" + e.toString() + "]" );
                }
                finally
                {
                    logger.info( message );
                }
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
        }
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
