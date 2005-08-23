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
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.Properties;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import net.dpml.depot.Main;
import net.dpml.depot.ShutdownHandler;
import net.dpml.depot.GeneralException;

import net.dpml.station.Station;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.Parameter;

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
    private final TransitModel m_model;
    private final ShutdownHandler m_handler;
    private final ApplicationRegistry m_depot;

    private String m_spec;
    private String[] m_args;

   /**
    * Plugin class used to handle the deployment of a target application.  The 
    * plugin assumes that the first argument of the supplied args parameter is
    * the specification of the deployment unit.  This may be (a) a uri to a Transit 
    * plugin, (b) a part uri, or (c) the name of a stored application profile.
    * 
    * @param logger the assigned logging channel
    * @param handler the shutdown handler
    * @param model the current transit model
    * @param prefs the depot root preferences
    * @param args command line arguments
    * @exception Exception if an error occurs
    */
    public ApplicationHandler( 
      Logger logger, ShutdownHandler handler, TransitModel model, 
      Preferences prefs, String[] args ) throws Exception
    {
        m_logger = logger;
        m_handler = handler;
        m_args = args;
        m_model = model;

        if( args.length < 1 )
        {
            final String error = 
              "Missing application profile specification (usage $ depot -exec [spec])";
            getLogger().error( error );
            handler.exit( -1 );
        }

        try
        {
            Repository repository = Transit.getInstance().getRepository();
            ClassLoader classloader = getClass().getClassLoader();
            URI uri = new URI( DEPOT_PROFILE_URI );
            m_depot = (ApplicationRegistry) repository.getPlugin( 
               classloader, uri, new Object[]{prefs, logger} );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected failure while attempting to establish the depot profile.";
                getLogger().error( error, e );
            throw new HandledException();
        }

        m_spec = args[0];
        m_args = Main.consolidate( args, m_spec );
       
        getLogger().info( "target profile: " + m_spec );

        Object object = null;
        ApplicationProfile profile = null;
        boolean flag = false;

        //
        // There are a number of things we could be doing here.  First off 
        // is publication of an application profile.  Another option is to 
        // execute the profile.  A third option is to publish and start.  A 
        // forth is to stop a started profile.  A last option is to stop and 
        // retract a profile.
        //

        boolean applySysProperties = !m_spec.startsWith( "registry:" );

        try
        {
            // prepare application context
    
            profile = getApplicationProfile( m_spec );
            flag = profile.isaServer();
            URI codebase = profile.getCodeBaseURI();
            getLogger().info( "profile codebase: " + codebase );
            ClassLoader system = ClassLoader.getSystemClassLoader();

            if( applySysProperties ) 
            {
                Properties properties = profile.getSystemProperties();
                applySystemProperties( properties );
            }   
 
            // light the fires and spin the tyres

            try
            {
                object = 
                  resolveTargetObject( 
                    m_model, system, codebase, m_args, m_logger, profile );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Application deployment failure.";
                getLogger().error( error, e );
                m_handler.exit( -1 );
            }
        }
        catch( GeneralException e )
        {
            throw e;
        }
        catch( HandledException e )
        {
            m_handler.exit( -1 );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected failure while attempting to deploy: " + m_spec;
                getLogger().error( error, e );
            m_handler.exit( -1 );
        }

        if( !flag )
        {
            m_handler.exit();
        }
        else if( object instanceof Runnable )
        {
            try
            {
                getLogger().info( "starting " + object.getClass().getName() );
                Thread thread = new Thread( (Runnable) object );
                Main.setShutdownHook( thread );
                thread.start();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Terminating due to process error.";
                getLogger().error( error, e );
                m_handler.exit( -1 );
            }
        }
    }
    
   /**
    * Return an application profile matching the supplied id.  If the id 
    * is a artifact or link uri then we construct and return a new profile
    * otherwise the profile id will be assumed to be a name of a profile 
    * registered within the set of stored profiles.
    * 
    * @param logger the assigned logging channel
    * @param prefs the root depot prefs
    * @param id the profile id
    * @return the application profile
    */ 
    private ApplicationProfile getApplicationProfile( String id ) throws Exception
    {
        if( id.startsWith( "artifact:" ) || id.startsWith( "link:" ) )
        {
            URI uri = new URI( id );
            return m_depot.createAnonymousApplicationProfile( uri );
        }
        else if( id.startsWith( "registry:" ) )
        {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            try
            {
                return (ApplicationProfile) new URL( id ).getContent();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( classloader );
            }
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
                throw new GeneralException( error );
            }
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
        for( int i=0; i < keys.length; i++ )
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
        Parameter[] params = profile.getParameters();
        if( "plugin".equals( type ) )
        {
            ArrayList list = new ArrayList();
            for( int i=0; i<params.length; i++ )
            {
                Parameter param = params[i];
                list.add( param.getValue() );
            }
            list.add( args );
            list.add( model );
            list.add( logger );
            list.add( profile );
            Object[] parameters = list.toArray();
            return loader.getPlugin( parent, uri, parameters );
        }
        else if( "part".equals( type ) )
        {
            //
            // TODO change to this include a fully embedded controller
            //

            String path = uri.toASCIIString();
            final String message = 
              "loading part ["
              + path 
              + "] with Transit [" 
              + model.getID()
              + "] profile";
            getLogger().info( message );
            URL url = new URL( path );
            return url.getContent( new Class[]{Object.class} );
        }
        else
        {
            final String error = 
              "Artifact type [" + type + "] is not supported.";
            throw new Exception( error );
        }
    }

    private Registry getRegistry( String host, int port ) throws Exception
    {
        return LocateRegistry.getRegistry( host, port );
    }

    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";

}
