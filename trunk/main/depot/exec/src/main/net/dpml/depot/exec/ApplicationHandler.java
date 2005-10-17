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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import net.dpml.depot.Main;
import net.dpml.depot.ShutdownHandler;
import net.dpml.depot.GeneralException;

import net.dpml.part.Part;
import net.dpml.part.PartHandler;
import net.dpml.part.Context;
import net.dpml.part.Handler;

import net.dpml.station.Station;
import net.dpml.station.Application;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.PID;
import net.dpml.transit.Logger;
import net.dpml.transit.model.Value;
import net.dpml.transit.model.Construct;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.util.CLIHelper;

/**
 * Depot application deployment plugin.  This plugin handles the deployment of 
 * a target application based on commandline arguments supplied by the Depot 
 * Console.  It uses the Application Profile sub-system to retireve information 
 * about registered applications and criteria for JVM setup.
 */
public class ApplicationHandler 
{
    private static final PID PROCESS_ID = new PID();

    private final Logger m_logger;
    private final TransitModel m_model;
    private final ShutdownHandler m_handler;

    private ApplicationRegistry m_depot;
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
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        if( null == model )
        {
            throw new NullPointerException( "model" );
        }
        if( null == prefs )
        {
            throw new NullPointerException( "prefs" );
        }
        if( null == args )
        {
            throw new NullPointerException( "args" );
        }

        m_logger = logger;
        m_handler = handler;
        m_args = args;
        m_model = model;

        if( args.length < 1 )
        {
            final String error = 
              "Missing application profile specification (usage $ depot -exec [spec])";
            handleError( handler, error );
        }

        m_spec = args[0];
        m_args = CLIHelper.consolidate( args, m_spec );

        getLogger().info( "target profile: " + m_spec );
       
        Object object = null;
        ApplicationProfile profile = null;
        boolean flag = CLIHelper.isOptionPresent( m_args, "-command" );
        if( flag )
        {
            m_args = CLIHelper.consolidate( args, "-command" );
        }

        //
        // There are a number of things we could be doing here.  First off 
        // is publication of an application profile.  Another option is to 
        // execute the profile.  A third option is to publish and start.  A 
        // forth is to stop a started profile.  A last option is to stop and 
        // retract a profile.
        //
        
        try
        {
            setupApplicationRegistry( prefs, logger );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected failure while attempting to establish the depot profile.";
            handleError( handler, error, e );
        }

        boolean applySysProperties = !m_spec.startsWith( "registry:" );

        try
        {
            // prepare application context
    
            profile = getApplicationProfile( m_spec );
            String key = profile.getID();
            URI codebase = profile.getCodeBaseURI();
            getLogger().info( "profile codebase: " + codebase );

            if( applySysProperties )
            {
                Properties properties = profile.getSystemProperties();
                applySystemProperties( properties );
            }
            
            // light the fires and spin the tyres

            try
            {
                Station station = getStation();
                getLogger().info( "located station - requesting application" );
                Application application = station.getApplication( key );
                resolveTargetObject( 
                    m_model, codebase, m_args, m_logger, application, profile );
                getLogger().info( "target established" );
            }
            catch( IOException e )
            {
                resolveTargetObject( 
                    m_model, codebase, m_args, m_logger, null, profile );
                getLogger().info( "target established" );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Application deployment failure.";
                handleError( handler, error, e );
            }
        }
        catch( GeneralException e )
        {
            handleError( handler, e.getMessage() );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected failure while attempting to deploy: " + m_spec;
            handleError( handler, error, e );
        }

        if( flag )
        {
            handleShutdown();
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
                handleError( handler, error, e );
            }
        }

        handleStartupNotification();
    }

   /**
    * If the application handler is running as a Station subprocess the parent
    * may be monitoring our output stream for a startup notification - the 
    * protocol is based on a system property "dpml.station.notify.startup"
    * that declares the startup message - we simply return this message 
    * together with our process id separted by the colon character
    */
    private void handleStartupNotification()
    {
        String startupMessage = System.getProperty( "dpml.station.notify.startup" );
        if( null != startupMessage )
        {
            System.out.println( startupMessage + ":" + PROCESS_ID );
        }
    }

   /**
    * If the application handler is running as a Station subprocess the parent
    * may be monitoring our output stream for a shutdown notification - the 
    * protocol is based on a system property "dpml.station.notify.shutdown"
    * that declares the shutdown message - we return this message 
    * together with our process id separted by the colon character
    */
    private void handleShutdown()
    {
        String shutdownMessage = System.getProperty( "dpml.station.notify.shutdown" );
        if( null != shutdownMessage )
        {
            System.out.println( shutdownMessage + ":" + PROCESS_ID );
        }
        m_handler.exit();
    }

   /**
    * If the application handler is running as a Station subprocess the parent
    * may be monitoring our error stream for a error notification - the 
    * protocol is based on a system property "dpml.station.notify.error"
    * that declares the error notification header - we return this message 
    * together with our process id and the error message separted by the colon 
    * character.
    * 
    * @param handler the shutdown handler
    * @param error the error message
    */
    private void handleError( ShutdownHandler handler, String error )
    {
        handleError( handler, error, null );
    }

   /**
    * If the application handler is running as a Station subprocess the parent
    * may be monitoring our error stream for a error notification - the 
    * protocol is based on a system property "dpml.station.notify.error"
    * that declares the error notification header - we return this message 
    * together with our process id and the error message separted by the colon 
    * character
    * 
    * @param handler the shutdown handler
    * @param error the error message
    * @param cause the causal exception
    */
    private void handleError( ShutdownHandler handler, String error, Throwable cause )
    {
        //
        // perform normal logging of the error condition
        //

        System.out.println( "E2" );
        if( ( null != cause ) && !( cause instanceof GeneralException ) )
        {
            getLogger().error( error, cause );
        }

        //
        // check for station notification
        //

        String errorHeader = System.getProperty( "dpml.station.notify.error" );
        if( null != errorHeader )
        {
            if( null != error )
            {
                System.err.println( errorHeader + ":" + PROCESS_ID + ":" + error );
            }
            else
            {
                System.err.println( errorHeader + ":" + PROCESS_ID );
            }
        }

        //
        // terminate the process
        //

        handler.exit( -1 );
    }
    
   /**
    * Return an application profile matching the supplied id.  If the id 
    * is a artifact or link uri then we construct and return a new memory
    * resident profile.  If the id is a registry uri the profile will be 
    * resolved using the registry protocol handler.  Otherwise the id will
    * be assumed to be the name of a registered profile in the application
    * registry.
    *
    * @param id the profile id
    * @return the application profile
    * @exception Exception if an error occurs
    */ 
    private ApplicationProfile getApplicationProfile( String id ) throws Exception
    {
        if( id.startsWith( "artifact:" ) || id.startsWith( "link:" ) )
        {
            URI uri = new URI( id );
            return getApplicationRegistry().createAnonymousApplicationProfile( uri );
        }
        else if( id.startsWith( "registry:" ) )
        {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( ApplicationProfile.class.getClassLoader() );
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
                return getApplicationRegistry().getApplicationProfile( id );
            }
            catch( UnknownKeyException e )
            {
                final String error = 
                  "Application key [" + e.getKey() + "] not found.";
                throw new GeneralException( error );
            }
        }
    }
    
    private ApplicationRegistry getApplicationRegistry()
    {
        return m_depot;
    }

    private void setupApplicationRegistry( Preferences prefs, Logger logger ) throws Exception
    {
        Repository repository = Transit.getInstance().getRepository();
        ClassLoader classloader = getClass().getClassLoader();
        URI uri = new URI( DEPOT_PROFILE_URI );
        m_depot = (ApplicationRegistry) repository.getPlugin( 
           classloader, uri, new Object[]{prefs, logger} );
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

    private void resolveTargetObject( 
      TransitModel model, URI uri, String[] args, Logger logger, Application application, ApplicationProfile profile ) 
      throws Exception
    {
        //String key = profile.getID();
        Repository loader = Transit.getInstance().getRepository();
        Artifact artifact = Artifact.createArtifact( uri );
        String type = artifact.getType();
        Value[] params = profile.getParameters();
        ClassLoader system = ClassLoader.getSystemClassLoader();

        Class pluginClass = null;
        if( "plugin".equals( type ) )
        {
            pluginClass = loader.getPluginClass( system, uri );
        }
        else if( "part".equals( type ) )
        {
            URI controllerUri = new URI( "@COMPOSITION-CONTROLLER-URI@" );
            pluginClass = loader.getPluginClass( system, controllerUri );
        }
        else
        {
            final String error = 
              "Artifact type [" + type + "] is not supported.";
            throw new Exception( error );
        }

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginClassLoader = pluginClass.getClassLoader();

        try
        {
            Map map = new Hashtable();
            map.put( "logger", logger );
            map.put( "args", args );
            Thread.currentThread().setContextClassLoader( pluginClassLoader );
            if( "plugin".equals( type ) )
            {
                Object[] parameters = Construct.getArgs( map, params, new Object[]{logger, args, m_model} );
                Object object = loader.instantiate( pluginClass, parameters );
                if( application != null )
                {
                    application.handleCallback( PROCESS_ID );
                }
            }
            else
            {
                String path = uri.toASCIIString();
                final String message = 
                  "loading part ["
                  + path 
                  + "] with [" 
                  + model.getID()
                  + "] profile";
                getLogger().info( message );
                
                //
                // locate the station and retrieve the application reference, the
                // management context, and the runtime hander
                //
                
                Object[] parameters = Construct.getArgs( map, params, new Object[]{logger, args, m_model} );
                PartHandler partHandler = (PartHandler) loader.instantiate( pluginClass, parameters );
                
                //
                // activate an object using a runtime handler
                //
                
                /*
                Context context = application.getContext();
                Handler handler = partHandler.getHandler( context );
                handler.activate( context );
                handler.getInstance().getValue( false );
                */
                
                Value value = partHandler.resolve( uri );
                value.resolve( false );
                application.handleCallback( PROCESS_ID );
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
        }
    }

    private Station getStation() throws Exception
    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        try
        {
            return (Station) new URL( "registry:/dpml/station" ).getContent();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( classloader );
        }
    }

    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";

}
