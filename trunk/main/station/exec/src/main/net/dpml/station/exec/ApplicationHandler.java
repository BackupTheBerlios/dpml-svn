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

package net.dpml.station.exec;

import java.io.IOException;
import java.net.URI;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.Iterator;
import java.util.Set;

import net.dpml.component.Component;

import net.dpml.station.Station;
import net.dpml.station.Callback;
import net.dpml.station.ApplicationException;

import net.dpml.transit.Artifact;
import net.dpml.lang.PID;
import net.dpml.lang.Logger;

import net.dpml.cli.Option;
import net.dpml.cli.Group;
import net.dpml.cli.CommandLine;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.util.HelpFormatter;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.CommandBuilder;
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.NumberValidator;

/**
 * Generic application handler that establishes a dedicated handler based 
 * on the type of resource exposed by a codebase uri.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
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
        
        
        URI config = getConfigurationURI( line );
        URI params = getParametersURI( line );
        URI categories = getCategoriesURI( line );
        Properties properties = getCommandLineProperties( line );
        
        Component component = resolveTargetComponent( logger, uri, config, params, categories, properties );
        m_callback.started( PROCESS_ID, component );
    }
    
   /**
    * Return a properties instance composed of the <tt>-C&lt;key&gt;=&lt;value&gt;</tt>
    * commandline arguments.
    * @param line the commandline
    * @return the resolved properties
    */
    private Properties getCommandLineProperties( CommandLine line )
    {
        Properties properties = new Properties();
        Set propertyValue = line.getProperties();
        Iterator iterator = propertyValue.iterator();
        while( iterator.hasNext() )
        {
            String name = (String) iterator.next();
            String value = line.getProperty( name );
            properties.setProperty( name, value );
        }
        return properties;
    }
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
    private URI getConfigurationURI( CommandLine line )
    {
        return (URI) line.getValue( CONFIG_OPTION, null );
    }
    
    private URI getParametersURI( CommandLine line )
    {
        return (URI) line.getValue( PARAMS_OPTION, null );
    }
    
    private URI getCategoriesURI( CommandLine line )
    {
        return (URI) line.getValue( LOGGING_OPTION, null );
    }
    
    private Component resolveTargetComponent( 
      Logger logger, URI uri, URI config, URI params, URI categories, Properties properties ) throws Exception
    {
        if( Artifact.isRecognized( uri ) )
        {
            Artifact artifact = Artifact.createArtifact( uri );
            String type = artifact.getType();
            if( type.equals( "part" ) )
            {
                return new ComponentAdapter( logger, uri, config, params, categories, properties );
            }
        }
        
        final String error = 
          "URI not supported [" + uri + "].";
        throw new ApplicationException( error );
    }
    
    private Station getStation( int port ) throws Exception
    {
        Registry registry = LocateRegistry.getRegistry( port );
        return (Station) registry.lookup( Station.STATION_KEY );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

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

    private static final PropertyOption CONTEXT_OPTION = 
      new PropertyOption( "-C", "Set a context entry value.", 'C' );
    private static final NumberValidator PORT_VALIDATOR = NumberValidator.getIntegerInstance();
    private static final URIValidator URI_VALIDATOR = new URIValidator();
    
    private static final Option LOGGING_OPTION = 
        OPTION_BUILDER
          .withShortName( "categories" )
          .withDescription( "Set logging category priorities." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "URI." )
              .withName( "uri" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( URI_VALIDATOR )
              .create() )
          .create();

    private static final Option PORT_OPTION = 
        OPTION_BUILDER
          .withShortName( "port" )
          .withDescription( "Override default RMI registry port selection." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Port." )
              .withName( "port" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( PORT_VALIDATOR )
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
        

    private static final Option CONFIG_OPTION = 
        OPTION_BUILDER
          .withShortName( "config" )
          .withShortName( "c" )
          .withDescription( "Application configuration uri." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "URI." )
              .withName( "uri" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( URI_VALIDATOR )
              .create() )
          .create();
        
    private static final Option PARAMS_OPTION = 
        OPTION_BUILDER
          .withShortName( "params" )
          .withShortName( "p" )
          .withDescription( "Application parameters uri." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "URI." )
              .withName( "uri" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( URI_VALIDATOR )
              .create() )
          .create();
        
    private static final Option HELP_COMMAND =
      COMMAND_BUILDER
        .withName( "help" )
        .withDescription( "Print command help." )
        .create();

    private static final Group EXECUTE_GROUP =
      GROUP_BUILDER
        .withOption( KEY_OPTION )
        .withOption( PORT_OPTION )
        .withOption( CONFIG_OPTION )
        .withOption( PARAMS_OPTION )
        .withOption( CONTEXT_OPTION )
        .withOption( LOGGING_OPTION )
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
        .withMinimum( 1 )
        .withMaximum( 1 )
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
                handler.commission();
            }
            catch( Exception e )
            {
                final String error = 
                  "Initiating process deactivation due to an application error.";
                getLogger().error( error, e );
                try
                {
                    handler.decommission();
                }
                catch( Exception deactivationError )
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
                    m_handler.decommission();
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
