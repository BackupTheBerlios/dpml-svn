/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.station.console;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import net.dpml.station.Application;
import net.dpml.station.Manager;
import net.dpml.station.Station;
import net.dpml.station.StationException;

import net.dpml.station.info.StartupPolicy;
import net.dpml.station.ApplicationRegistry;
import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.ValueDescriptor;
import net.dpml.station.server.RemoteApplicationRegistry;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;

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
import net.dpml.cli.validation.EnumValidator;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.NumberValidator;


/**
 * Plugin that handles station commands.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StationPlugin 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new station plugin.  The station plugin handles console
    * based commmandline interaction with the application registry and a 
    * server station.
    *
    * @param logger the assigned logging channel
    * @param args command line arguments
    * @exception Exception if an error occurs duirng plugin establishment
    */
    public StationPlugin( Logger logger, String[] args ) throws Exception
    {
        m_logger = logger;
        
        // log the raw arguments
        
        if( m_logger.isDebugEnabled() )
        {
            StringBuffer buffer = 
              new StringBuffer( 
                "Processing [" 
                + args.length 
                + "] args." );
            for( int i=0; i<args.length; i++ )
            {
                buffer.append( 
                  "\n  " 
                  + ( i+1 ) 
                  + " " 
                  + args[i] );
            }
            String message = buffer.toString();
            m_logger.debug( message );
        }
        
        // parse the command group model
        
        Parser parser = new Parser();
        parser.setGroup( COMMAND_GROUP );
        
        // handle command
        
        try
        {
            CommandLine line = parser.parse( args );
            if( line.hasOption( HELP_COMMAND ) )
            {
                processHelp();
            }
            else if( line.hasOption( STARTUP_COMMAND ) )
            {
                processStartup( line );
            }
            else if( line.hasOption( SHUTDOWN_COMMAND ) )
            {
                Manager manager = getManager( line );
                processShutdown( manager );
            }
            else if( line.hasOption( ADD_COMMAND ) )
            {
                ApplicationRegistry registry = getApplicationRegistry( line );
                String key = (String) line.getValue( ADD_COMMAND, null );
                URI uri = (URI) line.getValue( REQUIRED_URI_OPTION, null );
                Properties properties = getCommandLineProperties( line );
                String baseDir = getBasedir( line );
                String title = getTitle( line, key );
                StartupPolicy policy = getStartupPolicy( line, StartupPolicy.MANUAL );
                int startup = getStartupTimeout( line, ApplicationDescriptor.DEFAULT_STARTUP_TIMEOUT );
                int shutdown = getShutdownTimeout( line, ApplicationDescriptor.DEFAULT_SHUTDOWN_TIMEOUT );
                URI config = getConfigurationURI( line );
                processAddCommand( 
                  registry, key, title, uri, startup, shutdown, properties, baseDir, policy, config );
            }
            else if( line.hasOption( SET_COMMAND ) )
            {
                ApplicationRegistry registry = getApplicationRegistry( line );
                String key = (String) line.getValue( SET_COMMAND, null );
                URI uri = (URI) line.getValue( OPTIONAL_URI_OPTION, null );
                Properties properties = getCommandLineProperties( line );
                String baseDir = getBasedir( line );
                String title = getTitle( line, key );
                StartupPolicy policy = getStartupPolicy( line, null );
                int startup = getStartupTimeout( line, -1 );
                int shutdown = getShutdownTimeout( line, -1 );
                URI config = getConfigurationURI( line );
                processSetCommand(
                  registry, key, title, uri, startup, shutdown, properties, baseDir, policy, config );
            }
            else if( line.hasOption( INFO_COMMAND ) )
            {
                processInfoCommand( line );
            }
            else if( line.hasOption( START_COMMAND ) )
            {
                Manager manager = getManager( line );
                String value = (String) line.getValue( START_COMMAND, null );
                processStartCommand( manager, value );
            }
            else if( line.hasOption( STOP_COMMAND ) )
            {
                Manager manager = getManager( line );
                String value = (String) line.getValue( STOP_COMMAND, null );
                processStopCommand( manager, value );
            }
            else if( line.hasOption( RESTART_COMMAND ) )
            {
                Manager manager = getManager( line );
                String value = (String) line.getValue( RESTART_COMMAND, null );
                processRestartCommand( manager, value );
            }
            else if( line.hasOption( REMOVE_COMMAND ) )
            {
                ApplicationRegistry registry = getApplicationRegistry( line );
                String value = (String) line.getValue( REMOVE_COMMAND, null );
                processRemoveCommand( registry, value );
            }
            else
            {
                List options = line.getOptions();
                Iterator iterator = options.iterator();
                while( iterator.hasNext() )
                {
                    Option option = (Option) iterator.next();
                    System.out.println( 
                      "# UNPROCESSED OPTION: " 
                      + option 
                      + " [" 
                      + option.getId() 
                      + "]" );
                }
            }
        }
        catch( OptionException e )
        {
            m_logger.error( e.getMessage() );
        }
    }
    
    // ------------------------------------------------------------------------
    // commandline interegation
    // ------------------------------------------------------------------------
    
    private Manager getManager( CommandLine line ) throws Exception
    {
        int port = getPortValue( line, Registry.REGISTRY_PORT );
        Registry registry = getLocalRegistry( port );
        if( null != registry )
        {
            try
            {
                return (Manager) registry.lookup( Station.STATION_KEY );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unable to locate station.";
                throw new StationException( error, e );
            }
        }
        else
        {
            final String error = 
              "Unable to locate station.";
            throw new StationException( error );
        }
    }
    
    private URI getRegistryURI( CommandLine line )
    {
        if( line.hasOption( REGISTRY_URI_OPTION ) )
        {
            return null;
        }
        else
        {
            return (URI) line.getValue( REGISTRY_URI_OPTION, null );
        }
    }
    
    private URI getConfigurationURI( CommandLine line )
    {
        if( line.hasOption( CONFIGURATION_URI_OPTION ) )
        {
            return null;
        }
        else
        {
            return (URI) line.getValue( CONFIGURATION_URI_OPTION, null );
        }
    }
    
   /**
    * Return a properties instance composed of the <tt>-D&lt;key&gt;=&lt;value&gt;</tt>
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

   /**
    * Return the basedir option value.
    * @param line the commandline
    * @return the base path
    */
    private String getBasedir( CommandLine line )
    {
        return (String) line.getValue( BASEDIR_OPTION, null );
    }
    
   /** 
    * Get the application title.
    * @param line the commandline
    * @param title a default title if no title specified on the commandline
    * @return the application title
    */
    private String getTitle( CommandLine line, String title )
    {
        return (String) line.getValue( TITLE_OPTION, title );
    }
    
   /**
    * Return the startup policy declared on the commandline.
    * @param line the commandline
    * @param policy the default policy to apply
    */
    private StartupPolicy getStartupPolicy( CommandLine line, StartupPolicy policy )
    {
        final String policyValue = (String) line.getValue( STARTUP_POLICY_OPTION, null );
        if( null == policyValue )
        {
            return policy;
        }
        else
        {
            return StartupPolicy.parse( policyValue );
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
    
    private int getStartupTimeout( CommandLine line, int fallback )
    {
        if( line.hasOption( STARTUP_TIMEOUT_OPTION ) )
        {
            Number number = (Number) line.getValue( STARTUP_TIMEOUT_OPTION, null );
            if( null != number )
            {
                return number.intValue();
            }
        }
        return fallback;
    }
    
    private int getShutdownTimeout( CommandLine line, int fallback )
    {
        if( line.hasOption( SHUTDOWN_TIMEOUT_OPTION ) )
        {
            Number number = (Number) line.getValue( SHUTDOWN_TIMEOUT_OPTION, null );
            if( null != number )
            {
                return number.intValue();
            }
        }
        return fallback;
    }
    
    private ApplicationRegistry getApplicationRegistry( CommandLine line ) throws Exception
    {
        URI uri = getRegistryURI( line );
        if( null != uri )
        {
            return getApplicationRegistry( uri );
        }
        else
        {
            try
            {
                Manager manager = getManager( line );
                getLogger().debug( "using remote registry" );
                return manager.getApplicationRegistry();
            }
            catch( Exception e )
            {
                getLogger().debug( "using local registry" );
                return getApplicationRegistry( (URI) null );
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // command handling
    // ------------------------------------------------------------------------
    
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
          100, 50 );
        
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
        formatter.setShellCommand( "station" );
        formatter.print();
    }
    
   /**
    * Startup the station.
    */
    private void processStartup( CommandLine line ) throws Exception
    {
        int port = getPortValue( line, Registry.REGISTRY_PORT );
        if( null != getStation( port ) )
        {
            final String message = 
              "Station already deployed on port: " + port;
            getLogger().warn( message );
            return;
        }
        ArrayList list = new ArrayList();
        list.add( "station" );
        Set propertyValue = line.getProperties();
        Iterator iterator = propertyValue.iterator();
        while( iterator.hasNext() )
        {
            String name = (String) iterator.next();
            String value = line.getProperty( name );
            list.add( "-D" + name + "=" + value );
        }

        list.add( "-server" );
        list.add( "" + port );
        URI uri = getRegistryURI( line );
        if( null != uri )
        {
            list.add( "-registry" );
            list.add( uri.toASCIIString() );
        }
        String[] args = (String[]) list.toArray( new String[0] );
        try
        {
            Runtime.getRuntime().exec( args, null );
        }
        catch( Throwable e )
        {
            final String error = 
              "Station deployment failure.";
            getLogger().error( error, e );
        }
        getLogger().info( "Station started on port: " + port );
    }
    
    private void processShutdown( Manager manager ) throws Exception
    {
        getLogger().info( "initiating station shutdown" );
        try
        {
            manager.shutdown();
        }
        catch( Exception e )
        {
            getLogger().warn( e.getClass().getName() );
        }    
        getLogger().info( "station shutdown complete" );
    }
    
   /**
    * List application registry information.
    * @param line the commandline
    */
    private void processInfoCommand( CommandLine line ) throws Exception
    {
        Manager manager = null;
        try
        {
            manager = getManager( line );
        }
        catch( Exception e )
        {
            // ignore
        }
        ApplicationRegistry registry = getApplicationRegistry( line );
        String key = (String) line.getValue( INFO_COMMAND, null );
        
        if( null == key )
        {
            StringBuffer buffer = new StringBuffer( "\n" );
            try
            {
                String[] keys = registry.getKeys();
                buffer.append( "\nProfile count: " + keys.length + "\n" );
                for( int i=0; i<keys.length; i++ )
                {
                    String k = keys[i];
                    ApplicationDescriptor profile = 
                      registry.getApplicationDescriptor( k );
                    buffer.append( 
                      "\n (" 
                      + ( i+1 ) 
                      + ") " 
                      + k
                    );
                    if( null != manager )
                    {
                        Application application = manager.getApplication( k );
                        buffer.append( "\t" + application.getState() );
                    }
                    else
                    {
                        buffer.append( "\t" );
                    }
                    buffer.append( "\t" + profile.getCodeBaseURI() );
                }
                buffer.append( "\n" );
                System.out.println( buffer.toString() );
            }
            catch( RemoteException e )
            {
                getLogger().error( "Remote exception.", e );
            }
            catch( UnknownKeyException e )
            {
                // ignore
            }
        }
        else
        {
            try
            {
                ApplicationDescriptor profile = registry.getApplicationDescriptor( key );
                listProfile( profile );
            }
            catch( UnknownKeyException e )
            {
                getLogger().warn( "Unknown application key [" + key + "]." );
            }
            catch( RemoteException e )
            {
                getLogger().error( "Remote exception.", e );
            }
        }
    }
    
   /**
    * Handle a request for the startup of an application process.
    * @param key the application key
    */
    private void processStartCommand( Manager manager, String key ) throws Exception
    {
        getLogger().info( "starting application [" + key + "]" );
        Application application = manager.getApplication( key );
        application.start();
    }
    
   /**
    * Handle a request for the shutdown of an application process.
    * @param key the application key
    */
    private void processStopCommand( Manager manager, String key ) throws Exception
    {
        getLogger().info( "stopping application [" + key + "]" );
        Application application = manager.getApplication( key );
        application.stop();
    }
    
   /**
    * Handle a request for the restart of an application process.
    * @param key the application key
    */
    private void processRestartCommand( Manager manager, String key ) throws Exception
    {
        getLogger().info( "restarting application [" + key + "]" );
        Application application = manager.getApplication( key );
        application.restart();
    }
    
   /**
    * Add a profile to the registry.
    * @param key the application key
    * @param title the application title
    * @param uri the codebase uri
    * @param properties system properties
    * @param base process base directory
    * @param policy application startup policy
    */
    private void processAddCommand( 
      ApplicationRegistry registry, String key, String title, URI uri, 
      int startup, int shutdown, Properties properties, String base, StartupPolicy policy, 
      URI config )
    {
        String configPath = null;
        if( null != config )
        {
            configPath = config.toASCIIString();
        }
        try
        {
            ApplicationDescriptor descriptor = 
              new ApplicationDescriptor( 
                uri.toASCIIString(), 
                title,
                new ValueDescriptor[0], 
                base, 
                policy, 
                startup, 
                shutdown, 
                properties,
                configPath );
            registry.addApplicationDescriptor( key, descriptor );
            registry.flush();
            System.out.println( "\nAdded new profile [" + key + "]" );
            listProfile( descriptor );
        }
        catch( DuplicateKeyException e )
        {
            final String error = 
              "Cannot add application profile because the key [" 
              + key
              + "] is already assigned to another profile.";
            getLogger().error( error );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while processing add request.";
            getLogger().error( error, e );
        }
    }
    
   /**
    * Update a profile.
    * @param key the application key
    * @param title the application title
    * @param uri the codebase uri
    * @param properties system properties
    * @param base process base directory
    * @param policy application startup policy
    */
    private void processSetCommand( 
      ApplicationRegistry registry, String key, String title, URI uri, 
      int startup, int shutdown, Properties properties, String base, 
      StartupPolicy policy, URI config )
      throws IOException
    {
        String configPath = null;
        if( null != config )
        {
            configPath = config.toASCIIString();
        }
        try
        {
            ApplicationDescriptor descriptor = 
              new ApplicationDescriptor( 
                uri.toASCIIString(), 
                title,
                new ValueDescriptor[0], 
                base, 
                policy, 
                startup, 
                shutdown, 
                properties, 
                configPath );
            registry.updateApplicationDescriptor( key, descriptor );
            registry.flush();
        }
        catch( UnknownKeyException e )
        {
            final String error = 
              "Cannot update application profile because the key [" 
              + key
              + "] is unknown.";
            getLogger().error( error );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while processing add request.";
            getLogger().error( error, e );
        }
        
        try
        {
            ApplicationDescriptor profile = registry.getApplicationDescriptor( key );
            System.out.println( "\nUpdated profile [" + key + "]" );
            listProfile( profile );
        }
        catch( UnknownKeyException e )
        {
            final String error = 
              "Unexpected error - updated profile not found.";
            getLogger().error( error, e );
        }
    }
    
    private void processRemoveCommand( ApplicationRegistry registry, String key )
    {
        try
        {
            registry.removeApplicationDescriptor( key );
            registry.flush();
            getLogger().info( "removed application [" + key + "]" );
        }
        catch( UnknownKeyException e )
        {
            final String error = 
              "Cannot add application profile because the key [" 
              + key
              + "] is unknown.";
            getLogger().error( error );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while processing remove request.";
            getLogger().error( error, e );
        }
    }
    
    // ------------------------------------------------------------------------
    // internal utilities
    // ------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private Station getStation( int port ) throws Exception
    {
        Registry registry = getLocalRegistry( port );
        if( null != registry )
        {
            try
            {
                return (Station) registry.lookup( Station.STATION_KEY );
            }
            catch( Throwable e )
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    private ApplicationRegistry getApplicationRegistry( int port ) throws Exception
    {
        Station station = getStation( port );
        if( null != station )
        {
            Manager manager = (Manager) station;
            return manager.getApplicationRegistry();
        }
        else
        {
            return getApplicationRegistry( (URI) null );
        }
    }
    
    private Registry getLocalRegistry( int port )
    {
        try
        {
            return LocateRegistry.getRegistry( port );
        }
        catch( RemoteException e )
        {
            return null;
        }
    }
    
    private ApplicationRegistry getApplicationRegistry( URI uri ) throws IOException
    {
        if( null == uri )
        {
            return getLocalApplicationRegistry( ApplicationRegistry.DEFAULT_STORAGE_URI );
        }
        else if( uri.getScheme().startsWith( "registry:" ) )
        {
            return (ApplicationRegistry) Artifact.createArtifact( uri ).toURL().getContent();
        }
        else
        {
            return getLocalApplicationRegistry( uri );
        }
    }
    
    private ApplicationRegistry getLocalApplicationRegistry( URI uri )
    {
        try
        {
            Logger logger = getLogger();
            URL url = getStorageURL( uri );
            return new RemoteApplicationRegistry( logger, url );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while loading application registry from the uri ["
              + uri
              + "].";
            getLogger().error( error, e );
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Return the storage uri as a url.
    * @param uri the uri
    * @return the url
    * @exception Exception if the uri could not be converted to a url
    */
    public URL getStorageURL( URI uri ) throws Exception
    {
        if( Artifact.isRecognized( uri ) )
        {
            return Artifact.createArtifact( uri ).toURL();
        }
        else
        {
            return uri.toURL();
        }
    }
    
   /**
    * List infomation to console about the supplied profile.
    * @param profile the application profile
    */
    private void listProfile( ApplicationDescriptor profile )
    {
        StringBuffer buffer = new StringBuffer( "\n" );
        buffer.append( "\n  Codebase: " + profile.getCodeBaseURI() );
        buffer.append( "\n  Working Directory Path: " + profile.getBasePath() );
        buffer.append( "\n  Startup Timeout: " + profile.getStartupTimeout() );
        buffer.append( "\n  Shutdown Timeout: " + profile.getShutdownTimeout() );
        buffer.append( "\n  Startup Policy: " + profile.getStartupPolicy() );
        Properties properties = profile.getSystemProperties();
        buffer.append( "\n  System Properties: " + properties.size() );
        buffer.append( "\n" );
        System.out.println( buffer.toString() );
    }
    
    // ------------------------------------------------------------------------
    // static utilities
    // ------------------------------------------------------------------------
    
    private static final String DEPOT_STATION_PLUGIN_URI = "@DEPOT-STATION-PLUGIN-URI@";
    private static final Set STARTUP_POLICY_SET = createStartupPolicySet();
    private static final String[] SUPPORTED_URI_SCHEMES = 
      new String[]{"link", "artifact", "local"};
    
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
    
    private static final Option STARTUP_POLICY_OPTION = 
      OPTION_BUILDER
        .withShortName( "policy" )
        .withDescription( "Startup policy." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "disabled|manual|automatic" )
            .withName( "policy" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( new EnumValidator( STARTUP_POLICY_SET ) )
            .create() )
        .create();
        
    private static final Option BASEDIR_OPTION = 
      OPTION_BUILDER
        .withShortName( "dir" )
        .withShortName( "basedir" )
        .withDescription( "Base directory." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Directory path." )
            .withName( "path" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();

    private static final Option TITLE_OPTION = 
      OPTION_BUILDER
        .withShortName( "title" )
        .withDescription( "Application title." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Description." )
            .withName( "title" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final PropertyOption PROPERTY_OPTION = new PropertyOption();
    private static final Option REQUIRED_URI_OPTION = buildURIOption( true );
    private static final Option OPTIONAL_URI_OPTION = buildURIOption( false );
    private static final NumberValidator INTERGER_VALIDATOR = NumberValidator.getIntegerInstance();
    
    private static final Option REGISTRY_URI_OPTION = 
        OPTION_BUILDER
          .withShortName( "registry" )
          .withDescription( "Application registry store." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Local or remote artifact reference." )
              .withName( "artifact" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URIValidator() )
              .create() )
          .create();
    
    private static final Option CONFIGURATION_URI_OPTION = 
        OPTION_BUILDER
          .withShortName( "config" )
          .withDescription( "Application configuration." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Configuration uri." )
              .withName( "uri" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URIValidator() )
              .create() )
          .create();
    
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
            .withValidator( INTERGER_VALIDATOR )
            .create() )
        .create();
    
    private static final Option STARTUP_TIMEOUT_OPTION = 
      OPTION_BUILDER
        .withShortName( "startup" )
        .withDescription( "Startup timeout." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Timeout in seconds." )
            .withName( "seconds" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( INTERGER_VALIDATOR )
            .create() )
        .create();
    
    private static final Option SHUTDOWN_TIMEOUT_OPTION = 
      OPTION_BUILDER
        .withShortName( "shutdown" )
        .withDescription( "Shutdown timeout." )
        .withRequired( false )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Timeout in seconds." )
            .withName( "seconds" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .withValidator( INTERGER_VALIDATOR )
            .create() )
        .create();
    
    private static final Group APPLICATION_REGISTRY_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( PORT_OPTION )
        .withOption( REGISTRY_URI_OPTION )
        .create();
    
    private static final Group STARTUP_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 2 )
        .withOption( PORT_OPTION )
        .withOption( REGISTRY_URI_OPTION )
        .withOption( PROPERTY_OPTION )
        .create();
    
    private static final Option STARTUP_COMMAND =
      COMMAND_BUILDER
        .withName( "startup" )
        .withDescription( "Startup the station." )
        .withChildren( STARTUP_GROUP )
        .create();

    private static final Option SHUTDOWN_COMMAND =
      COMMAND_BUILDER
        .withName( "shutdown" )
        .withDescription( "Shutdown the station." )
        .create();

    private static final Option HELP_COMMAND =
      COMMAND_BUILDER
        .withName( "help" )
        .withDescription( "Print command help." )
        .create();
    
    private static final Group ADD_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 1 )
        .withOption( REQUIRED_URI_OPTION )
        .withOption( STARTUP_POLICY_OPTION )
        .withOption( PROPERTY_OPTION )
        .withOption( BASEDIR_OPTION )
        .withOption( TITLE_OPTION )
        .withOption( REGISTRY_URI_OPTION )
        .withOption( STARTUP_TIMEOUT_OPTION )
        .withOption( SHUTDOWN_TIMEOUT_OPTION )
        .withOption( CONFIGURATION_URI_OPTION )
        .create();
        
    private static final Option ADD_COMMAND =
      COMMAND_BUILDER
        .withName( "add" )
        .withDescription( "Add a profile." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Unique application key." )
            .withName( "key" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .withChildren( ADD_OPTIONS_GROUP )
        .create();
        
    private static final Group SET_OPTIONS_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withOption( OPTIONAL_URI_OPTION )
        .withOption( STARTUP_POLICY_OPTION )
        .withOption( PROPERTY_OPTION )
        .withOption( BASEDIR_OPTION )
        .withOption( TITLE_OPTION )
        .withOption( REGISTRY_URI_OPTION )
        .withOption( STARTUP_TIMEOUT_OPTION )
        .withOption( SHUTDOWN_TIMEOUT_OPTION )
        .withOption( CONFIGURATION_URI_OPTION )
        .create();
    
    private static final Option SET_COMMAND =
      COMMAND_BUILDER
        .withName( "set" )
        .withDescription( "Set an application feature." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "application key" )
            .withName( "key" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .withChildren( SET_OPTIONS_GROUP )
        .create();
        
    private static final Option START_COMMAND =
      COMMAND_BUILDER
        .withName( "start" )
        .withDescription( "Start application." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Application key." )
            .withName( "key" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final Option STOP_COMMAND =
      COMMAND_BUILDER
        .withName( "stop" )
        .withDescription( "Stop application." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Application key." )
            .withName( "key" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final Option RESTART_COMMAND =
      COMMAND_BUILDER
        .withName( "restart" )
        .withDescription( "Restart application." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Application key." )
            .withName( "key" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .create();
    
    private static final Option REMOVE_COMMAND =
      COMMAND_BUILDER
        .withName( "remove" )
        .withDescription( "Remove profile." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "unique application key" )
            .withName( "key" )
            .withMinimum( 1 )
            .withMaximum( 1 )
            .create() )
        .withChildren( APPLICATION_REGISTRY_GROUP )
        .create();
        
    private static final Option INFO_COMMAND =
      COMMAND_BUILDER
        .withName( "info" )
        .withDescription( "Station or profile info." )
        .withArgument(
          ARGUMENT_BUILDER 
            .withDescription( "Application key." )
            .withName( "key" )
            .withMinimum( 0 )
            .withMaximum( 1 )
            .create() )
        .withChildren( APPLICATION_REGISTRY_GROUP )
        .create();

    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withName( "options" )
        .withOption( STARTUP_COMMAND )
        .withOption( ADD_COMMAND )
        .withOption( SET_COMMAND )
        .withOption( START_COMMAND )
        .withOption( STOP_COMMAND )
        .withOption( RESTART_COMMAND )
        .withOption( INFO_COMMAND )
        .withOption( REMOVE_COMMAND )
        .withOption( SHUTDOWN_COMMAND )
        .withOption( HELP_COMMAND )
        .withMinimum( 1 )
        .withMaximum( 1 )
        .create();
    
    private static Option buildURIOption( boolean required )
    {
        return OPTION_BUILDER
          .withShortName( "uri" )
          .withDescription( "Codebase uri." )
          .withRequired( required )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Codebase uri." )
              .withName( "artifact" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URIValidator() )
              .create() )
          .create();
    }
    
    private static Set createStartupPolicySet()
    {
        Set set = new HashSet();
        StartupPolicy[] values = StartupPolicy.values();
        for( int i=0; i<values.length; i++ )
        {
            StartupPolicy policy = values[i];
            set.add( policy.getName() );
        }
        return set;
    }
}
