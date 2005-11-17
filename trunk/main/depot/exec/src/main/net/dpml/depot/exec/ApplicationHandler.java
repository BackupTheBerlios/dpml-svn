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

package net.dpml.metro.exec;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.lang.reflect.InvocationTargetException;

import net.dpml.metro.part.Component;
import net.dpml.metro.part.HandlerException;
import net.dpml.metro.part.Instance;

import net.dpml.station.Station;
import net.dpml.station.Application;
import net.dpml.station.Callback;
import net.dpml.station.ApplicationException;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;
import net.dpml.transit.Repository;
import net.dpml.transit.PID;
import net.dpml.transit.Logger;
import net.dpml.transit.model.Value;
import net.dpml.transit.model.Construct;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.UnknownKeyException;

import net.dpml.cli.Option;
import net.dpml.cli.Group;
import net.dpml.cli.CommandLine;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.util.HelpFormatter;
import net.dpml.cli.OptionException;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.CommandBuilder;
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.NumberValidator;

/**
 * The Metro plugin handles the establishment of a part and optional 
 * invocatio of a callback to a central station.
 */
public class ApplicationHandler
{
    private static final PID PROCESS_ID = new PID();

    private final Logger m_logger;
    private final Callback m_callback;
    
   /**
    * The application handler class is a generic component handler that delegates
    * application deployment processes to dedicated handlers based on the codebase 
    * type of the target application.  Normally this plugin is activated as a 
    * consequence of invoking the <tt>metro</tt> commandline handler (which is 
    * the default haviour of the <tt>station</tt> when deployment local processes).
    * The following command list help about the <tt>metro</tt> commandline handler:
    * <pre>$ metro help
Usage:
metro [exec <uri> help]
options
  exec <uri>               Execute deployment of an application codebase.
    -key -port
      -key (-k) <key>      Station callback application key.
      -port (-p) <port>    Override default RMI registry port selection.
  help                     Print command help.
    * </pre>
    * A typical example of a commandline and resulting log of an application launch 
    * using <tt>metro</tt> is show below:
    * <pre>
$ metro exec link:part:dpml/planet/http/dpml-http-demo
[2236 ] [INFO   ] (demo): Starting
[2236 ] [INFO   ] (org.mortbay.http.HttpServer): Version Jetty/5.1.x
[2236 ] [INFO   ] (org.mortbay.util.Container): Started net.dpml.http.impl.HttpServerImpl@6355dc
[2236 ] [INFO   ] (org.mortbay.util.Credential): Checking Resource aliases
[2236 ] [INFO   ] (org.mortbay.util.Container): Started HttpContext[/,/]
[2236 ] [INFO   ] (org.mortbay.http.SocketListener): Started SocketListener on 0.0.0.0:8080
    * </pre>
    *
    * When invoked in conjuction with the <tt>station</tt> two optional parameters may be 
    * supplied by the station.  These include <tt>-port</tt> option which directs the implementation 
    * to locate the system wide <tt>station</tt> from an RMI registry on the selected port.  
    * The second option is the <tt>-key</tt> argument that the handler uses to to resolve a 
    * station callback through which the implementation notifies the station of the process 
    * identity and startup status.  The callback process effectivly hands control over 
    * management of the JVM process lietime and root component to to the station.
    * 
    * @param logger the assigned logging channel
    * @param args command line arguments
    * @exception Exception if an error occurs
    */
    public ApplicationHandler( 
      Logger logger, String[] args ) throws Exception
    {
        super();
        m_logger = logger;
        
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        
        if( logger.isDebugEnabled() )
        {
            for( int i=0; i<args.length; i++ )
            {
                logger.debug( "arg: " + ( i + 1 ) + ": " + args[i] );
            }
        }
        
        Parser parser = new Parser();
        parser.setGroup( COMMAND_GROUP );
        CommandLine line = parser.parse( args );
        
        if( line.hasOption( HELP_COMMAND ) || !line.hasOption( EXECUTE_COMMAND ) )
        {
            processHelp();
            System.exit( 0 );
        }
        
        URI uri = (URI) line.getValue( EXECUTE_COMMAND, null );
        if( null == uri )
        {
            throw new NullPointerException( "uri" ); // will not happen
        }
        
        String key = (String) line.getValue( KEY_OPTION, null );
        if( null != key )
        {
            int port = Registry.REGISTRY_PORT;
            if( line.hasOption( PORT_OPTION ) )
            {
                Number number = 
                  (Number) line.getValue( PORT_OPTION, null );
                port = number.intValue();
            }
            
            Station station = getStation( port );
            m_callback = station.getCallback( key );
        }
        else
        {
            m_callback = new LocalCallback();
        }
        
        if( Artifact.isRecognized( uri ) )
        {
            Artifact artifact = Artifact.createArtifact( uri );
            String type = artifact.getType();
            if( type.equals( "part" ) )
            {
                Component handler = new ComponentHandler( logger, uri );
                m_callback.started( PROCESS_ID, handler );
            }
            else
            {
                Component handler = new AbstractHandler( logger );
                m_callback.started( PROCESS_ID, handler );
            }
        }
        else
        {
            final String error = 
              "URI not supported [" + uri + "].";
            throw new ApplicationException( error );
        }
    }
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
    private Station getStation( int port ) throws Exception
    {
        Registry registry = LocateRegistry.getRegistry( port );
        return (Station) registry.lookup( Station.STATION_KEY );
    }
    
    
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
    /*
    ApplicationHandler( 
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
    */

   /**
    * If the application handler is running as a Station subprocess the parent
    * may be monitoring our output stream for a startup notification - the 
    * protocol is based on a system property "dpml.station.notify.startup"
    * that declares the startup message - we simply return this message 
    * together with our process id separted by the colon character
    */
    /*
    private void handleStartupNotification()
    {
        String startupMessage = System.getProperty( "dpml.station.notify.startup" );
        if( null != startupMessage )
        {
            System.out.println( startupMessage + ":" + PROCESS_ID );
        }
    }
    */

   /**
    * If the application handler is running as a Station subprocess the parent
    * may be monitoring our output stream for a shutdown notification - the 
    * protocol is based on a system property "dpml.station.notify.shutdown"
    * that declares the shutdown message - we return this message 
    * together with our process id separted by the colon character
    */
    /*
    private void handleShutdown()
    {
        String shutdownMessage = System.getProperty( "dpml.station.notify.shutdown" );
        if( null != shutdownMessage )
        {
            System.out.println( shutdownMessage + ":" + PROCESS_ID );
        }
        m_handler.exit();
    }
    */

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
    /*
    private void handleError( ShutdownHandler handler, String error )
    {
        handleError( handler, error, null );
    }
    */

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
    /*
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
    */
    
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
    /*
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
    */
    /*
    private ApplicationRegistry getApplicationRegistry()
    {
        return m_depot;
    }
    */
    /*
    private void setupApplicationRegistry( Preferences prefs, Logger logger ) throws Exception
    {
        Repository repository = Transit.getInstance().getRepository();
        ClassLoader classloader = getClass().getClassLoader();
        URI uri = new URI( DEPOT_PROFILE_URI );
        m_depot = (ApplicationRegistry) repository.getPlugin( 
           classloader, uri, new Object[]{prefs, logger} );
    }
    */

    private Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Setup the system properties for the target profile.
    * @param properties system properties asserted by a profile
    */
    /*
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
    */
    /*
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
            //pluginClass = loader.getPluginClass( system, controllerUri );
            pluginClass = loader.getPluginClass( PartHandler.class.getClassLoader(), controllerUri );
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
                Object object = loader.instantiate( pluginClass, parameters );
                if( object instanceof PartHandler )
                {
                    PartHandler partHandler = (PartHandler) object;
                
                    //
                    // activate an object using a runtime handler
                    //
                
                    //Context context = application.getContext();
                    //Component handler = partHandler.getHandler( context );
                    //handler.activate( context );
                    //handler.getInstance().getValue( false );
                
                    Value value = partHandler.resolve( uri );
                    value.resolve( false );
                    
                    if( null != application )
                    {
                        application.handleCallback( PROCESS_ID );
                    }
                }
                else
                {
                    Class c = object.getClass();
                    final String error = 
                      "Resolved class ["
                      + object.getClass().getName()
                      + "] is not an instance of a part handler."
                      + ")"
                      + "\n" + pluginClassLoader.toString();
                    throw new IllegalArgumentException( error );
                }
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
        }
    }
    */
    /*
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
    */

    //private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";
    
   /**
    * List general command help to the console.
    * @exception IOException if an I/O error occurs
    */
    private void processHelp() throws IOException
    {
        HelpFormatter formatter = new HelpFormatter( 
          HelpFormatter.DEFAULT_GUTTER_LEFT, 
          HelpFormatter.DEFAULT_GUTTER_CENTER, 
          HelpFormatter.DEFAULT_GUTTER_RIGHT, 
          100 );
        
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_GROUP_OUTER );
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_GROUP_OUTER );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        formatter.getFullUsageSettings().remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        
        formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_GROUP_EXPANDED );
        
        formatter.setGroup( COMMAND_GROUP );
        formatter.setShellCommand( "metro" );
        formatter.print();
    }
    
    //-----------------------------------------------------------------------------
    // CLI
    //-----------------------------------------------------------------------------

    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();

    private static final PropertyOption PROPERTY_OPTION = new PropertyOption();
    private static NumberValidator portValidator = NumberValidator.getIntegerInstance();
      
    private static final Option PORT_OPTION = 
        OPTION_BUILDER
          .withShortName( "port" )
          .withShortName( "p" )
          .withDescription( "Override default RMI registry port selection." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Port." )
              .withName( "port" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( portValidator )
              .create() )
          .create();

    private static final Option KEY_OPTION = 
        OPTION_BUILDER
          .withShortName( "key" )
          .withShortName( "k" )
          .withDescription( "Station callback application key." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Key." )
              .withName( "key" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .create() )
          .create();
        
    //private static final Option URI_OPTION = 
    //    OPTION_BUILDER
    //      .withShortName( "uri" )
    //      .withDescription( "Application codebase uri." )
    //      .withRequired( false )
    //      .withArgument(
    //        ARGUMENT_BUILDER 
    //          .withDescription( "Codebase uri." )
    //          .withName( "uri" )
    //          .withMinimum( 1 )
    //          .withMaximum( 1 )
    //          .withValidator( new URIValidator() )
    //          .create() )
    //      .create();
    
    private static final Option HELP_COMMAND =
      COMMAND_BUILDER
        .withName( "help" )
        .withDescription( "Print command help." )
        .create();

    private static final Group EXECUTE_GROUP =
      GROUP_BUILDER
        .withOption( KEY_OPTION )
        .withOption( PORT_OPTION )
        .create();
    
    private static final Option EXECUTE_COMMAND =
      COMMAND_BUILDER
        .withName( "exec" )
        .withDescription( "Execute deployment of an application codebase." )
        .withChildren( EXECUTE_GROUP )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Application codebase uri." )
            .withName( "uri" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( new URIValidator() )
            .create() )
        .create();
    
    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withName( "options" )
        .withOption( EXECUTE_COMMAND )
        .withOption( HELP_COMMAND )
        .create();

   /**
    * Internal class supporting callback semantics used in cases where the application
    * handler has to assume management of a target.
    */
    private class LocalCallback implements Callback
    {
        private PID m_pid;
        private Component m_handler;
        
       /**
        * Method invoked by a process to signal that the process has started.
        *
        * @param pid the process identifier
        * @param handler optional handler reference
        * @exception RemoteException if a remote error occurs
        */
        public void started( PID pid, Component handler )
        {
            m_pid = pid;
            m_handler = handler;
            try
            {
                handler.activate();
            }
            catch( Exception e )
            {
                try
                {
                    handler.deactivate();
                }
                catch( Exception ee )
                {
                    // ignore
                }
            }
        }
    
        /**
        * Method invoked by a process to signal that the process has 
        * encounter an error condition.
        *
        * @param throwable the error condition
        * @param fatal if true the process is requesting termination
        * @exception RemoteException if a remote error occurs
        */
        public void error( Throwable throwable, boolean fatal )
        {
            final String error = "Application error.";
            if( fatal )
            {
                getLogger().error( error, throwable );
                try
                {
                    m_handler.deactivate();
                }
                catch( Throwable e )
                {
                }
            }
            else
            {
                getLogger().warn( error, throwable );
            }
        }
    
        /**
        * Method invoked by a process to send a arbitary message to the 
        * the callback handler.
        *
        * @param message the message
        * @exception RemoteException if a remote error occurs
        */
        public void info( String message )
        {
            getLogger().info( message );
        }
    
        /**
        * Method invoked by a process to signal its imminent termination.
        *
        * @exception RemoteException if a remote error occurs
        */
        public void stopped()
        {
            Thread thread = new Thread(
              new Runnable()
              {
                  public void run()
                  {
                    System.exit( 0 );
                  }
              }
            );
            thread.start();
        }
    }
}
