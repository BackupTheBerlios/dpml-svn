/*
 * Copyright 2005-2007 Stephen McConnell
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

package dpml.station.console;

import dpml.appliance.ApplianceHelper;
import dpml.station.connector.LocalConnector;
import dpml.station.info.InfoDescriptor;
import dpml.station.info.ProcessDescriptor;
import dpml.station.info.ApplianceDescriptor;
import dpml.station.util.OutputStreamReader;
import dpml.station.util.StreamReader;
import dpml.station.util.LoggingServer;

import dpml.cli.Option;
import dpml.cli.Group;
import dpml.cli.CommandLine;
import dpml.cli.OptionException;
import dpml.cli.TooManyArgumentsException;
import dpml.cli.commandline.Parser;
import dpml.cli.util.HelpFormatter;
import dpml.cli.DisplaySetting;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.CommandBuilder;
import dpml.cli.option.PropertyOption;
import dpml.cli.validation.URIValidator;
import dpml.cli.validation.NumberValidator;

import dpml.util.PropertyResolver;
import dpml.util.DefaultLogger;
import dpml.util.PID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.ConnectException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.Iterator;
import java.util.Set;
import java.util.EnumSet;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.tools.Tool;
import javax.lang.model.SourceVersion;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
    
import net.dpml.annotation.Context;
import net.dpml.annotation.Component;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceError;
import net.dpml.appliance.ApplianceException;
import net.dpml.appliance.ApplianceListener;
import net.dpml.appliance.ApplianceEvent;
import net.dpml.appliance.ApplianceConnector;

import net.dpml.lang.Strategy;
import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.SimpleServiceRegistry;
import net.dpml.lang.DuplicateKeyException;

import net.dpml.runtime.ComponentListener;
import net.dpml.runtime.ComponentEvent;
import net.dpml.runtime.ProviderEvent;
import net.dpml.runtime.Status;

import net.dpml.station.Station;

import net.dpml.transit.Artifact;
import net.dpml.transit.InvalidArtifactException;
import net.dpml.transit.ContentHandler;
import net.dpml.transit.Transit;

import net.dpml.util.Logger;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/**
 * Generic application handler that establishes a dedicated handler based 
 * on the type of resource exposed by a codebase uri.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( lifestyle=SINGLETON )
public class Main extends UnicastRemoteObject implements Tool, Station
{
    //------------------------------------------------------------------------------
    // static
    //------------------------------------------------------------------------------
    
    private static final String DEFAULT_PLAN_URI_VALUE = "local:plan:dpml/metro/default";
    private static final String STATION_CONNECTOR_VALUE = "dpml/station";
    
    //------------------------------------------------------------------------------
    // state
    //------------------------------------------------------------------------------
    
    private final Logger m_logger;
    private Appliance m_plan;
    private Thread m_thread;
    //private JMXServiceURL m_jmx;
    //private JMXConnectorServer m_server;
    
   /**
    * The application handler class is a generic component handler that delegates
    * application deployment processes to dedicated handlers based on the codebase 
    * type of the target application.  Normally this plugin is activated as a 
    * consequence of invoking the <tt>metro</tt> commandline handler (which is 
    * the default haviour of the <tt>station</tt> when deployment local processes).
    * The following command list help about the <tt>metro</tt> commandline handler:
    * <pre>$ metro help
Usage:
metro <uri> [-server] | -help
options
  <uri>                    Application codebase part uri.
  -command                 Enables command mode.
  -help                    Prints command help and exits.
  -version                 Prints version info and exits.
    * </pre>
    * An example commandline and resulting log of an application launch 
    * using <tt>metro</tt> is show below:
    * <pre>
$ metro -command link:part:dpml/lang/dpml-lang-sample

[2756 ] [INFO   ] (dpml.metro): commissioning hello
[2756 ] [INFO   ] (hello): pid: [2756]
[2756 ] [INFO   ] (hello): message: Hello
[2756 ] [INFO   ] (hello): port: 0
[2756 ] [INFO   ] (hello): target: artifact:jar:dpml/lang/dpml-lang-sample#SNAPSHOT
[2756 ] [INFO   ] (hello): starting
[2756 ] [INFO   ] (hello): started
[2756 ] [INFO   ] (dpml.metro): decommissioning hello
[2756 ] [INFO   ] (hello): stopping
[2756 ] [INFO   ] (hello): stopped
    * </pre>
    *
    * @param logger the assigned logging channel
    * @exception Exception if an error occurs
    */
    public Main( Logger logger ) throws Exception
    {
        super();
        
        m_logger = logger;
    }
    
    public Set<SourceVersion> getSourceVersions()
    {
        return Collections.unmodifiableSet(
          EnumSet.range(
            SourceVersion.RELEASE_4,
            SourceVersion.latest() ) );
    }
    
   /**
    * Run the Metro tool.
    */
    public int run( InputStream in, OutputStream out, OutputStream err, String... arguments )
    {
        Thread.currentThread().setContextClassLoader( 
          getClass().getClassLoader() );
          
        for( int i=0; i<arguments.length; i++ )
        {
            String arg = arguments[i];
            if( m_logger.isTraceEnabled() )
            {
                m_logger.trace( "  arg: " + ( i + 1 ) + ": " + arg );
            }
            if( "-help".equals( arg ) )
            {
                printHelpMessage();
                return 0;
            }
            if( "-version".equals( arg ) )
            {
                printVersionMessage();
                return 0;
            }
        }
        
        try
        {
            return execute( arguments );
        }
        catch( Throwable e )
        {
            m_logger.error( "Execution error.", e );
            return -1;
        }
    }
    
    private int execute( String... args ) throws Exception
    {
        String name = System.getProperty( STATION_CONNECTOR_KEY, STATION_CONNECTOR_VALUE );
        CommandLine line = getCommandLine( args );
        if( null == line )
        {
            return -1;
        }
        
        int port = getPortValue( line, Registry.REGISTRY_PORT );
        if( line.hasOption( SERVER_COMMAND ) )
        {
            Registry registry = resolveRegistry( port );
            
            LoggingServer.init();
            
            //m_jmx = getJMXServiceURL( port );
            //m_server = getJMXConnectorServer( m_jmx );
            //m_server.start();
            
            URI uri = getURI( line );
            m_thread = Thread.currentThread();
            
            URL url = Artifact.toURL( uri );
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "resolving plan [" + url + "]" );
            }
            try
            {
                m_plan = (Appliance) url.getContent( new Class[]{ Appliance.class } );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( current );
            }
            if( null == m_plan )
            {
                if( getLogger().isErrorEnabled() )
                {
                    final String error =
                      "Unable to resolve ["
                      + url
                      + "] to an appliance.";
                    getLogger().error( error );
                }
                return -1;
            }
            
            registry.bind( name, this );
            
            try
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().trace( "commissioning plan" );
                }
                m_plan.commission();
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().trace( "plan commissioning complete" );
                }
                return 1;
            }
            catch( Exception e )
            {
                final String error = 
                  "Plan deployment error.";
                if( getLogger().isErrorEnabled() )
                {
                    getLogger().error( error, e );
                }
                m_plan.decommission();
                return -1;
            }
        }
        else if( line.hasOption( STARTUP_COMMAND ) )
        {
            URI uri = getURI( line );
            if( getLogger().isInfoEnabled() )
            {
                getLogger().info( "station startup" );
            }
            
            //
            // otherwise we are creating a sub-process with a sticky connector
            // and we locate the appliance from the connector, initiate appliance 
            // commissioning, and given a successfull startup we exit without 
            // disrupting the launched sub-process
            //
            
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "creating station subprocess" );
            }
            List<String> commands = new ArrayList<String>();
            commands.add( "station" );
            commands.add( 
              "-D" 
              + STATION_CONNECTOR_KEY 
              + "=" 
              + STATION_CONNECTOR_VALUE );
            //commands.add( 
            //  "-D" 
            //  + STATION_JMX_URI_KEY 
            //  + "=" 
            //  + m_jmx.toString() );
            commands.add( "server" );
            commands.add( "-port" );
            commands.add( "" + port );
            commands.add( uri.toASCIIString() );
            
            ProcessBuilder builder = new ProcessBuilder( commands );
            builder.redirectErrorStream( true );
            
            if( getLogger().isTraceEnabled() )
            {
                StringBuffer buffer = new StringBuffer( "station command: " );
                for( String command : commands )
                {
                    buffer.append( command );
                    buffer.append( " " );
                }
                getLogger().trace( buffer.toString() );
            }
            
            // execute the process
            
            Process process = builder.start();
            //setShutdownHook( process ); // <-- need to enable this, but add removeShutdownHook before we return
            OutputStreamReader output = 
              new OutputStreamReader( m_logger, process.getInputStream() );
            output.setDaemon( true );
            output.start();
            
            waitForThis( port, name );
            
            if( getLogger().isInfoEnabled() )
            {
                getLogger().info( "startup complete" );
            }
            
            return 0;
        }
        else if( line.hasOption( SHUTDOWN_COMMAND ) )
        {
            try
            {
                Station station = getStation( port );
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "initiating remote shutdown" );
                }
                station.shutdown();
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "remote shutdown request completed" );
                }
                return 0;
            }
            catch( ConnectException e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "Station not running" );
                }
                return -1;
            }
            catch( Exception e )
            {
                final String error = "Shutdown error.";
                if( getLogger().isErrorEnabled() )
                {
                    getLogger().error( error, e );
                }
                return -1;
            }
        }
        else if( line.hasOption( INFO_COMMAND ) )
        {
            try
            {
                Station station = getStation( port );
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "Station running on port [" + port + "]" );
                }
            }
            catch( StationNotBoundException e )
            {
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "Station not bound on port [" + port + "]"  );
                }
            }
            catch( ConnectException e )
            {
                if( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "Station not running" );
                }
            }
            return 0;
        }
        else
        {
            final String error = 
              "Invalid command state.";
            if( getLogger().isErrorEnabled() )
            {
                getLogger().error( error );
            }
            return -1;
        }
    }
    
    private JMXServiceURL getJMXServiceURL( int port ) throws MalformedURLException
    {
        return new JMXServiceURL( 
            "service:jmx:rmi:///jndi/rmi://localhost:" 
            + port 
            + "/dpml-station-jmx");
    }
    
    private JMXConnectorServer getJMXConnectorServer( JMXServiceURL url ) throws IOException
    {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        return JMXConnectorServerFactory.newJMXConnectorServer( url, null, server ); 
    }

    private void waitForThis( int port, String name ) // TODO: add timeout support
    {
        Station station = null;
        while( null == station )
        {
            try
            {
                Registry registry = LocateRegistry.getRegistry( port );
                station = (Station) registry.lookup( name );
            }
            catch( Exception ie )
            {
                try
                {
                    Thread.currentThread().sleep( 100 );
                }
                catch( InterruptedException e )
                {
                }
            }
        }
    }
    
    public void shutdown() throws RemoteException
    {
        if( null != m_plan )
        {
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "initiating station process shutdown" );
            }
            m_plan.decommission();
            Terminator terminator = new Terminator( m_thread, this );
            terminator.setName( "DPML Station Terminator" );
            terminator.start();
            /*
            if( null != m_server )
            {
                try
                {
                    m_server.stop();
                }
                catch( IOException e )
                {
                    if( getLogger().isWarnEnabled() )
                    {
                        getLogger().warn( "JMX connector server shutdown error.", e );
                    }
                }
            }
            */
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "station process shutdown complete" );
            }
        }
    }
    
    private class Terminator extends Thread
    {
        private Thread m_thread;
        private Station m_station;
        
        Terminator( Thread thread, Station station )
        {
            m_thread = thread;
            m_station = station;
        }
        
        public void run()
        {
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "executing terminator" );
            }
            try
            {
                UnicastRemoteObject.unexportObject( m_station, false );
            }
            catch( Throwable e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    final String error = 
                      "Ignoring unexpected error while unexporting the station.";
                    getLogger().warn( error, e );
                }
            }
            try
            {
                UnicastRemoteObject.unexportObject( m_plan, false );
            }
            catch( Throwable e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    final String error = 
                      "Ignoring unexpected error while unexporting the plan.";
                    getLogger().warn( error, e );
                }
            }
            try
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().trace( "interrupting " + m_thread );
                }
                m_thread.interrupt();
            }
            catch( Throwable e )
            {
                if( getLogger().isWarnEnabled() )
                {
                    final String error = "interupted";
                    getLogger().warn( error, e );
                }
            }
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "termination completed" );
            }
        }
    }
    
    private Station getStation( int port ) throws Exception
    {
        Registry registry = LocateRegistry.getRegistry( port );
        try
        {
            return (Station) registry.lookup( STATION_CONNECTOR_VALUE );
        }
        catch( NotBoundException nbe )
        {
            final String error = 
              "Station not bound on ["
              + port
              + "] under ["
              + STATION_CONNECTOR_VALUE
              + "] on registry ["
              + registry 
              + "]";
            throw new StationNotBoundException( error );
        }
    }

    private Registry resolveRegistry( int port )throws ApplianceException
    {
        try
        {
            Registry registry = LocateRegistry.createRegistry( port );
            getLogger().info( "created local registry on port " + port );
            return registry;
        }
        catch( RemoteException re )
        {
            try
            {
                Registry registry = LocateRegistry.getRegistry( port );
                getLogger().info( "using local registry on port " + port );
                return registry;
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unable to locate or create RMI registry to due a remote exeception.";
                throw new ApplianceException( error, e );
            }
        }
    }
    
    private URI getURI( CommandLine line )
    {
        URI uri = (URI) line.getValue( CODEBASE, null );
        if( null == uri )
        {
            return URI.create( DEFAULT_PLAN_URI_VALUE );
        }
        else
        {
            return uri;
        }
    }
    
    private int getPortValue( CommandLine line, int defaultPort )
    {
        if( line.hasOption( PORT_OPTION ) )
        {
            Number number = (Number) line.getValue( PORT_OPTION, null );
            if( null != number )
            {
                return number.intValue();
            }
        }
        return defaultPort;
    }
    
    private CommandLine getCommandLine( String[] args ) throws Exception
    {
        try
        {
            Parser parser = new Parser();
            parser.setGroup( COMMAND_GROUP );
            return parser.parse( args );
        }
        catch( TooManyArgumentsException e )
        {
            String message = e.getMessage();
            m_logger.error( message );
            return null;
        }
        catch( OptionException oe )
        {
            String message = oe.getMessage();
            m_logger.error( message );
            return null;
        }
    }
    
    private void printVersionMessage()
    {
        StringBuffer buffer = new StringBuffer( "version" );
        buffer.append( "\n" );
        buffer.append( "\n  DPML Station" );
        buffer.append( "\n  Version @PROJECT-VERSION@" );
        buffer.append( "\n  Copyright 2005-2007 Stephen J. McConnell" );
        buffer.append( "\n  Digital Product Management Laboratory" );
        String message = buffer.toString();
        m_logger.info( message );
    }
    
    private void printHelpMessage()
    {
        StringBuffer buffer = new StringBuffer( "help" );
        buffer.append( "\n" );
        buffer.append( "\n  Description:" );
        buffer.append( "\n" );
        buffer.append( "\n    Process management controller." );
        buffer.append( "\n" );
        buffer.append( "\n  Usage: $ station startup [<uri>] | info | shutdown | -help | -version" );
        buffer.append( "\n" );
        buffer.append( "\n  Common Options:" );
        buffer.append( "\n" );
        buffer.append( "\n    -help              list command help and exit" );
        buffer.append( "\n    -version           list version info and exit" );
        buffer.append( "\n    -port              override the RMI Registry port value" );
        buffer.append( "\n" );
        buffer.append( "\n  Commands:" );
        buffer.append( "\n" );
        buffer.append( "\n    startup [<uri>]    startup the station" );
        buffer.append( "\n" );
        buffer.append( "\n        <uri>          uri referencing a plan datatype" );
        buffer.append( "\n                       if not supplied the default station deployment" );
        buffer.append( "\n                       plan (local:plan:dpml/station/default) will" );
        buffer.append( "\n                       be selected" );
        buffer.append( "\n" );
        buffer.append( "\n        -server        enables server execution mode" );
        buffer.append( "\n" );
        buffer.append( "\n    info               list station operational status and exit" );
        buffer.append( "\n" );
        buffer.append( "\n    shutdown           shutdown the station" );
        buffer.append( "\n" );
        buffer.append( "\n" );
        String message = buffer.toString();
        m_logger.info( message );
    }
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }

    //-----------------------------------------------------------------------------
    // CLI
    //-----------------------------------------------------------------------------

    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();

    private static final NumberValidator PORT_VALIDATOR = NumberValidator.getIntegerInstance();
    private static final URIValidator URI_VALIDATOR = new URIValidator();
    
    private static final Option PORT_OPTION = 
      OPTION_BUILDER
        .withShortName( "port" )
        .withDescription( "RMI Registry port." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Registry port." )
            .withName( "port" )
            .withMinimum( 0 )
            .withMaximum( 1 )
            .withValidator( PORT_VALIDATOR )
            .create() )
        .create();
    
    private static final Option SERVER_OPTION =
      OPTION_BUILDER
        .withShortName( "server" )
        .withDescription( "Enables server execution mode." )
        .create();
    
    private static final Option SHUTDOWN_OPTION =
      OPTION_BUILDER
        .withShortName( "shutdown" )
        .withShortName( "s" )
        .withDescription( "Shutdown the station." )
        .create();
    
    private static final Option CODEBASE = 
        ARGUMENT_BUILDER
          .withName( "codebase" )
          .withDescription( "URI referencing a plan datastructure." )
          .withValidator( new URIValidator() )
          .create();

    private static final Group SERVER_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 2 )
        .withOption( PORT_OPTION )
        .withOption( CODEBASE )
        .create();
    
    private static final Option SERVER_COMMAND =
      COMMAND_BUILDER
        .withName( "server" )
        .withDescription( "Server mode." )
        .withChildren( SERVER_GROUP )
        .create();
    
    private static final Group INFO_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( PORT_OPTION )
        .create();
    
    private static final Option INFO_COMMAND =
      COMMAND_BUILDER
        .withName( "info" )
        .withDescription( "Station status report." )
        .withChildren( INFO_GROUP )
        .create();
    
    private static final Group STARTUP_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 2 )
        .withOption( PORT_OPTION )
        //.withOption( SERVER_OPTION )
        .withOption( CODEBASE )
        .create();
    
    private static final Option STARTUP_COMMAND =
      COMMAND_BUILDER
        .withName( "startup" )
        .withDescription( "Startup the station." )
        .withChildren( STARTUP_GROUP )
        .create();
    
    private static final Group SHUTDOWN_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( PORT_OPTION )
        .create();

    private static final Option SHUTDOWN_COMMAND =
      COMMAND_BUILDER
        .withName( "shutdown" )
        .withDescription( "Shutdown the station." )
        .withChildren( SHUTDOWN_GROUP )
        .create();
    
    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( INFO_COMMAND )
        .withOption( STARTUP_COMMAND )
        .withOption( SHUTDOWN_COMMAND )
        .withOption( SERVER_COMMAND )
        .create();

    private static void setShutdownHook( final Process process )
    {
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
            public void run()
            {
                try
                {
                    process.destroy();
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
            }
          }
        );
    }
}
