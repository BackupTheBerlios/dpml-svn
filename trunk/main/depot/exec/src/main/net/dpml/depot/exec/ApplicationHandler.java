/*
 * Copyright 2005 Stephen McConnell
 *
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

package net.dpml.depot.exec;

import java.net.URI;
import java.net.URL;
import java.util.prefs.Preferences;
import java.util.Properties;

import net.dpml.depot.ShutdownHandler;

import net.dpml.profile.DepotProfile;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.UnknownKeyException;


/**
 * Depot application deployment plugin.  This plugin handles the deployment of 
 * a target application based on commandline arguments supplied by the Depot 
 * Console.  It uses the Application Profile sub-system to retireve information 
 * about registered applications and criteria for JVM setup.
 */
public class ApplicationHandler
{
    private final Logger m_logger;
    private final ShutdownHandler m_handler;
    private final DepotProfile m_depot;

   /**
    * Plugin class used to handle the deployment of a target application.
    * 
    * @param logger the assigned logging channel
    * @param handler the shutdown handler
    * @param prefs the depot root preferences
    * @param args command line arguments
    */
    public ApplicationHandler( 
      Logger logger, ShutdownHandler handler, TransitModel model, Preferences prefs, String[] args ) throws Exception
    {
        m_logger = logger;
        m_handler = handler;

        if( args.length < 1 )
        {
            final String error = 
              "Missing application profile name (usage $ depot -exec [name])";
            getLogger().error( error );
            handler.exit( -1 );
        }

        try
        {
            Repository repository = Transit.getInstance().getRepository();
            ClassLoader classloader = getClass().getClassLoader();
            URI uri = new URI( DEPOT_PROFILE_URI );
            m_depot = (DepotProfile) repository.getPlugin( 
               classloader, uri, new Object[]{ prefs, logger } );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected failure while attempting to establish the depot profile.";
                getLogger().error( error, e );
            throw new HandledException();
        }

        String id = args[0];
        getLogger().info( "target profile: " + id );

        try
        {
            ApplicationProfile profile = getApplicationProfile( logger, prefs, id );

            // prepare application context
    
            String[] arguments = getTargetArgs( args );
            Logger channel = getLogger().getChildLogger( id );
            URI codebase = profile.getCodeBaseURI();
            getLogger().info( "profile codebase: " + codebase );
            ClassLoader system = ClassLoader.getSystemClassLoader();
            Properties properties = profile.getSystemProperties();
            applySystemProperties( properties );
    
            // light the fires and spin the tyres

            try
            {
                Object object = resolveTargetObject( model, system, codebase, arguments, channel, profile );
                if( profile.getCommandPolicy() )
                {
                    handler.exit();
                }
            }
            catch( Throwable e )
            {
                final String error = 
                  "Application deployment failure.";
                getLogger().error( error, e );
                handler.exit( -1 );
            }
        }
        catch( HandledException e )
        {
            handler.exit( -1 );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected failure.";
                getLogger().error( error, e );
            handler.exit( -1 );
        }
    }
    
   /**
    * Return an application profile matching the supplied id.  If the id 
    * is a artifact or link uri then we construct and return a new profile
    * otherwise the profile id will be assumed to be a name of a profile 
    * registered within the set of stored profiles.
    * 
    * @param logger the assigned lohgging channel
    * @param prefs the root depot prefs
    * @param id the profile id
    * @return the application profile
    */ 
    private ApplicationProfile getApplicationProfile( 
      Logger logger, Preferences prefs, String id ) throws Exception
    {
        if( id.startsWith( "artifact:" ) || id.startsWith( "link:" ) )
        {
            URI uri = new URI( id );
            return m_depot.createAnonymousApplicationProfile( uri );
        }
        else
        {
            try
            {
                return m_depot.getApplicationProfile( id );
            }
            catch( UnknownKeyException e )
            {
                final String error = 
                  "Application key [" + e.getKey() + "] not found.";
                getLogger().error( error );
                throw new HandledException();
            }
        }
    }

    private String[] getTargetArgs( String[] args )
    {
        if( args.length < 2 )
        {
            return new String[0];
        }
        else
        {
            String[] result = new String[ args.length - 1 ];
            for( int i=1; i<args.length; i++ )
            {
                result[i-1] = args[i];
            }
            return result;
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Setup the system properties for the target profile.
    * @param properties system properties asserted by a profile
    */
    private void applySystemProperties( Properties properties )
    {
        if( null == properties )
        {
            return;
        }
        String[] keys = (String[]) properties.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            String value = properties.getProperty( key, null );
            if( null != value )
            {
                getLogger().info( "setting sys property: " + key + " to: " + value );
                System.setProperty( key, value );
            }
        }
    }

    private Object resolveTargetObject( 
      TransitModel model, ClassLoader parent, URI uri, String[] args, Logger logger, ApplicationProfile profile ) 
      throws Exception
    {
        Repository loader = Transit.getInstance().getRepository();
        Artifact artifact = Artifact.createArtifact( uri );
        String type = artifact.getType();
        Properties params = profile.getProperties();
        if( "plugin".equals( type ) )
        {
            return loader.getPlugin( 
              parent, uri, new Object[]{ args, logger, profile, params } );
        }
        else if( "part".equals( type ) )
        {
            String path = uri.toASCIIString();
            final String message = 
              "loading part ["
              + path 
              + "] with Transit [" 
              + model.getID()
              + "] profile";
            getLogger().info( message );
            URL url = new URL( path );
            return url.getContent( new Class[]{ Object.class } );
        }
        else
        {
            final String error = 
              "Artifact type [" + type + "] is not supported.";
            throw new Exception( error );
        }
    }


    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";

}
