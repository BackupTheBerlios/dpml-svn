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

package net.dpml.depot;

import java.io.File;
import java.net.URL;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.Random;

import net.dpml.transit.Transit;
import net.dpml.transit.Logger;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;
import net.dpml.transit.monitor.Adapter;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.monitor.RepositoryMonitorAdapter;
import net.dpml.transit.monitor.CacheMonitorAdapter;
import net.dpml.transit.monitor.NetworkMonitorAdapter;
import net.dpml.transit.Repository;
import net.dpml.transit.RepositoryException;
import net.dpml.transit.PID;
import net.dpml.transit.Plugin;
import net.dpml.transit.Environment;
import net.dpml.transit.util.CLIHelper;

/**
 * CLI hander for the depot package.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Main.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public final class Main implements ShutdownHandler
{
    private static Main m_MAIN;
    private static final PID PROCESS_ID = new PID();

    private Object m_plugin;

   /**
    * Static start method used by NT service.
    * @param args command link arguments
    * @exception Exception if a error occurs
    */
    public static void start( final String[] args )
        throws Exception
    {
        System.out.println( "starting station" );
        Main.main( new String[]{"-station"} );
    }

   /**
    * Static stop method used by NT service.
    * @param args command link arguments
    * @exception Exception if a error occurs
    */
    public static void stop( final String[] args )
        throws Exception
    {
        if( null != m_MAIN )
        {
            m_MAIN.exit();
        }
        else
        {
            System.exit( 0 );
        }
    }

   /**
    * Create a shutdown hook that will trigger shutdown of the supplied plugin.
    * @param thread the application thread
    */
    public static void setShutdownHook( final Thread thread )
    {
        //
        // Create a shutdown hook to trigger clean disposal of the
        // controller
        //
        
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
              public void run()
              {
                  try
                  {
                      thread.interrupt();
                  }
                  catch( Throwable e )
                  {
                      // ignore it
                      boolean ignorable = true;
                  }
                  System.runFinalization();
              }
          }
        );
    }

   /**
    * Create a shutdown hook that will trigger shutdown of the supplied plugin.
    * @param thread the application thread
    */
    private void setInternalShutdownHook( final Logger logger, final Handler handler )
    {
        //
        // Create a shutdown hook to trigger clean disposal of the
        // handler
        //
        
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
              public void run()
              {
                  try
                  {
                      logger.info( "shutdown" ); 
                      handler.destroy();
                  }
                  catch( Throwable e )
                  {
                      // ignore it
                      boolean ignorable = true;
                  }
                  System.runFinalization();
              }
          }
        );
    }

   /**
    * Processes command line options to establish the command handler plugin to deploy.
    * Command parameters recognixed by the console include the following:
    * <ul>
    *   <li>-help</li>
    *   <li>-version</li>
    *   <li>-reset</li>
    *   <li>-debug</li>
    *   <li>-setup</li>
    *   <li>-prefs</li>
    *   <li>-station</li>
    *   <li>-exec</li>
    * </ul>
    * @param args the command line argument array
    * @exception Exception if a error occurs
    */
    public static void main( String[] args )
        throws Exception
    {
        if( null != m_MAIN )
        {
            final String error = 
              "Console already established.";
            throw new IllegalArgumentException( error );
        }
        else
        {
            m_MAIN = new Main( args );
        }
    }

    private Main( String[] arguments )
    {
        String[] args = processSystemProperties( arguments );

        boolean debug = false;
        boolean tools = false;
        
        if( CLIHelper.isOptionPresent( args, "-debug" ) )
        {
            args = CLIHelper.consolidate( args, "-debug" );
            System.setProperty( "dpml.logging.level", 
              System.getProperty( "dpml.logging.level", "FINE" ) );
            debug = true;
            for( int i=0; i<arguments.length; i++ )
            {
                System.out.println( " arg[" + i + "] " + arguments[i] );
            }
            System.getProperties().list( System.out );
        }

        if( CLIHelper.isOptionPresent( args, "-tools" ) )
        {
            args = CLIHelper.consolidate( args, "-tools" );
            tools = true;
        }
        
        String option = getSwitch( args );

        if( "-reset".equals( option ) )
        {
            int result = handleReset();
            exit( result );
        }
        else if( "-version".equals( option ) )
        {
            handleVersion( debug );
            exit();
        }
        else if( "-get".equals( option ) )    
        {
            for( int i=0; i < args.length; i++ )
            {
                String arg = args[i];
                if( arg.equals( "-get" ) )
                {
                    if( i + 1 < args.length )
                    {
                        String path = args[ i + 1 ];
                        try
                        {
                            handleGet( getLogger(), args, path );
                            exit();
                        }
                        catch( Throwable e )
                        {
                            String message = e.getMessage();
                            Throwable cause = e.getCause();
                            getLogger().error( message, e );
                            exit( -1 );
                        }
                    }
                    else
                    {
                        final String error = 
                          "Missing get parameter value.";
                        getLogger().error( error );
                        exit( -1 );
                    }
                }
            }
        }
        else if( "-setup".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-setup" );
            handleSetup( args );
        }
        else if( "-prefs".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-prefs" );
            handlePrefs( args );
        }
        else if( "-desktop".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-desktop" );
            handleDesktop( args );
        }
        else if( "-exec".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-exec" );
            handleExec( args );
        }
        else if( "-run".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-run" );
            handleRun( args, tools );
        }
        else if( "-build".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-build" );
            handleBuild( args );
        }
        else if( "-station".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-station" );
            handleStation( args );
        }
        else if( "-audit".equals( option ) )
        {
            args = CLIHelper.consolidate( args, "-audit" );
            handleAudit( args );
        }
        else
        {
            handleHelp();
            exit();
        }
    }

    private int handleReset()
    {
        try
        {
            Preferences netDpml = Preferences.userRoot().node( "/net/dpml" );
            clearPreferences( netDpml );
            Preferences rootDpml = Preferences.userRoot().node( "/dpml" );
            clearPreferences( rootDpml );
            return 0;
        }
        catch( Throwable e )
        {
            final String message =
              "Preferences reset error occured.";
            getLogger().warn( message, e );
            return -1;
        }
    }

    public void exit()
    {
        exit( 0 );
    }

    public void exit( int flag )
    {
        System.exit( flag );
    }

    private void handleSetup( String[] args )
    {
        String name = "setup";
        String spec = "@DEPOT-INSTALL-URI@";
        handlePlugin( name, spec, args );
    }

    private void handlePrefs( String[] args )
    {
        String name = "prefs";
        String spec = "@DEPOT-PREFS-URI@";
        handlePlugin( name, spec, args );
    }

    private void handleDesktop( String[] args )
    {
        String name = "desktop";
        String spec = "@DEPOT-DESKTOP-URI@";
        handlePlugin( name, spec, args );
    }

    private void handleExec( String[] args )
    {
        String name = "exec";
        String spec = "@DEPOT-EXEC-URI@";
        handlePlugin( name, spec, args );
    }

    private void handleBuild( String[] args )
    {
        String name = "build";
        String spec = "@DEPOT-BUILD-URI@";
        handlePlugin( name, spec, args, false, true );
    }

    private void handleRun( String[] args, boolean tools )
    {
        if( args.length < 1 )
        {
            System.out.println( "Target URI required." );
            System.exit( -1 );
        }
        String name = "run";
        String spec = args[0];
        args = CLIHelper.consolidate( args, spec );
        handlePlugin( name, spec, args, false, tools );
    }

    private void handleStation( String[] args )
    {
        String name = "station";
        String spec = "@DEPOT-STATION-URI@";
        handlePlugin( name, spec, args );
    }

    private void handleAudit( String[] args )
    {
        String name = "audit";
        String spec = "@DEPOT-AUDIT-URI@";
        handlePlugin( name, spec, args, false );
    }

    private void handlePlugin( String name, String spec, String[] args )
    {
        handlePlugin( name, spec, args, true );
    }

    private void handlePlugin( String name, String spec, String[] args, boolean wait )
    {
        handlePlugin( name, spec, args, true, false );
    }
    
    private void handlePlugin( String name, String spec, String[] args, boolean wait, boolean tools )
    {
        System.setSecurityManager( new RMISecurityManager() );

        // get setup uri

        Preferences prefs = getRootPreferences();
        Preferences handlers = prefs.node( "handlers" );
        Preferences setup = handlers.node( name );
        String path = setup.get( "uri", spec );
        
        // deploy plugin

        boolean waitForCompletion = deployHandler( name, path, args, this, wait, tools );
        if( !waitForCompletion )
        {
            exit();
        }
    }

    private boolean deployHandler( 
      String command, String path, String[] args, ShutdownHandler shutdown, boolean waitFor, boolean tools )
    {
        Logger logger = getLogger().getChildLogger( command );
        try
        {
            Preferences prefs = getRootPreferences();
            URI uri = new URI( path );
            TransitModel model = getTransitModel( args );
            Transit transit = Transit.getInstance( model );
            setupMonitors( transit, (Adapter) getLogger() );
            Repository repository = transit.getRepository();
            DepotClassLoader classloader = getSystemClassLoader();

            //
            // get the plugin descriptor and check for system classloader entries
            //

            Plugin descriptor = repository.getPluginDescriptor( uri );
            URI[] bootstrap = descriptor.getDependencies( Plugin.SYSTEM );
            if( bootstrap.length > 0 )
            {
                logger.debug( "supplimenting system classpath" );
                for( int i=0; i < bootstrap.length; i++ )
                {
                    URI entry = bootstrap[i];
                    logger.debug( "system entry: (" + (i+1) + ") " + entry );
                }
            }

            if( tools )
            {
                //String javaHome = Environment.getEnvVariable( "JAVA_HOME" );
                String jrePath = System.getProperty( "java.home" );
                try
                {
                    File jre = new File( jrePath );
                    File jdk = jre.getParentFile();
                    File lib = new File( jdk, "lib" );
                    File jar = new File( lib, "tools.jar" );
                    URL url = jar.toURL();
                    URI[] stack = new URI[ bootstrap.length + 1 ];
                    System.arraycopy( bootstrap, 0, stack, 0, bootstrap.length );
                    stack[bootstrap.length] = new URI( url.toString() );
                    classloader.setClasspath( stack );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Internal error while attempting to establish tools.jar in the system classloader: "
                      + e.toString();
                    System.err.println( error );
                    System.exit( -1 );
                }
            }
            else
            {
                classloader.setClasspath( bootstrap );
            }

            m_plugin = 
              repository.getPlugin( 
                classloader, uri, new Object[]{model, args, logger, shutdown, prefs} );
        }
        catch( RepositoryException e )
        {
            Throwable cause = e.getCause();
            if( ( null != cause ) && ( cause instanceof GeneralException ) )
            {
                getLogger().error( cause.getMessage() );
                return false;
            }
            else
            {
                getLogger().error( e.getMessage(), e.getCause() );
                return false;
            }
        }
        catch( GeneralException e )
        {
            getLogger().error( e.getMessage() );
            return false;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to deploy the [" 
              + command 
              + "] handler due to deployment failure.";
            getLogger().error( error, e );
            return false;
        }

        if( m_plugin instanceof Handler )
        {
            setInternalShutdownHook( logger, (Handler) m_plugin );
        }
        if( m_plugin instanceof Runnable )
        {
            getLogger().debug( "starting " + m_plugin.getClass().getName() );
            Thread thread = new Thread( (Runnable) m_plugin );
            thread.start();
            setShutdownHook( thread );
            return true;
        }
        else
        {
            getLogger().debug( "deployed " + m_plugin.getClass().getName() );
            return waitFor;
        }
    }

    private TransitModel getTransitModel( String[] args ) throws RemoteException
    {
        //
        // TODO: improve this so that we can direct the selection of the transit model
        // in a way that would allow resolution of the model from the rmi registry
        // e.g. -transit registry://some/name
        //

        Logger logger = getLogger();
        return new DefaultTransitModel( logger );
    }


   /*
        boolean install = CLIHelper.isOptionPresent( args, "-install" );
        boolean remove = CLIHelper.isOptionPresent( args, "-remove" );
        if( install || remove )
        {
            System.setProperty( "java.util.logging.ConsoleHandler.level", "SEVERE" );
        }

        try
        {
            //
            // setup the depot managment model, get the name of the requested 
            // application profile and load the profile from the depot manager
            //
    
            Logger logger = getLogger();
            Preferences prefs = getRootPreferences();
            DepotHome store = new DepotStorageUnit( prefs );
            DepotProfile manager = new DefaultDepotProfile( logger, store );

            //
            // check for the prefs or station option
            //

            TransitModel model = null;
            ApplicationProfile profile = null;
            for( int i=0; i < args.length; i++ )
            {
                String arg = args[i];
                if( arg.equals( "-prefs" ) )
                {
                    args = CLIHelper.consolidate( args, "-prefs" );
                    model = loadTransitModel( args, logger, true );
                    profile = createPrefsProfile( logger );
                    break;
                }
                else if( arg.equals( "-station" ) )
                {
                    args = CLIHelper.consolidate( args, "-station" );
                    model = loadTransitModel( args, logger, false );
                    profile = createStationProfile( logger );
                    break;
                }
                else if( arg.equals( "-install" ) )
                {
                    args = CLIHelper.consolidate( args, "-install" );
                    model = loadTransitModel( args, logger, false );
                    profile = createInstallProfile( logger, true );
                    break;
                }
                else if( arg.equals( "-remove" ) )
                {
                    args = CLIHelper.consolidate( args, "-remove" );
                    model = loadTransitModel( args, logger, false );
                    profile = createInstallProfile( logger, false );
                    break;
                }
                else if( arg.equals( "-profile" ) )
                {
                    //args = CLIHelper.consolidate( args, "-profile" );
                    model = loadTransitModel( args, logger, true );
                    String target = getTargetProfile( args );
                    if( null != target )
                    {
                        profile = getApplicationProfile( manager, target );
                        break;
                    }
                }
            }

            if( null == profile )
            {
                handleHelp();
                exit();
            }

            termination = profile.getCommandPolicy();
            for( int i=0; i < args.length; i++ )
            {
                String arg = args[i];
                if( arg.equals( "-execute" ) )
                {
                    termination = true;
                    break;
                }
            }

            Properties properties = profile.getSystemProperties();
            applySystemProperties( properties );
            ClassLoader system = getSystemClassLoader();
            URI uri = profile.getCodeBaseURI();
            String id = profile.getID();
            Logger log = logger.getChildLogger( id );

            OBJECT = resolveTargetObject( system, uri, args, manager, model, log, profile );

            Connection connection = profile.getConnection();
            if( null != connection )
            {
                logger.info( "service is requesting a connection" );
                logger.info( "host: " + connection.getHost() );
                logger.info( "port: " + connection.getPort() );

                Registry registry = getRegistry( connection, true );

                registry.bind( id, (Remote) OBJECT );
                if( null == connection.getHost() )
                {
                    logger.info( 
                      "service bound to "
                      + connection.getPort() 
                      + "/" 
                      + id );
                }
                else
                {
                    logger.info( 
                       "service bound to " 
                       + connection.getHost() 
                       + ":" 
                       + connection.getPort() 
                       + "/" + id );
                }
            }
            else if( OBJECT instanceof Runnable )
            {
                //
                // run the plugin
                //
    
                logger.info( "starting " + OBJECT.getClass().getName() );
                Thread thread = new Thread( (Runnable) OBJECT );
                thread.start();
                setShutdownHook( thread );
            }
            else
            {
                logger.info( "loaded " + OBJECT.getClass().getName() );
            }
        }
        catch( ArtifactNotFoundException e )
        {
            getLogger().error( e.getMessage() );
            System.exit( -1 );
        }
        catch( Throwable e )
        {
            final String error = 
              "Deployment failure.";
            getLogger().error( error, e );
            System.exit( -1 );
        }

        long now = new Date().getTime();
        long diff = ( now - start );
        java.math.BigInteger r = new java.math.BigInteger ( "" + diff );
        java.math.BigInteger d = new java.math.BigInteger ( "" + 1000 );
        java.math.BigInteger[] result = r.divideAndRemainder( d );
        getLogger().info( "startup completed in " + result[0] + "." + result[1] + " seconds" );
        if( termination )
        {
            exit();
        }
    }
    */

    private void handleGet( Logger logger, String[] args, String path ) throws Exception
    {
        try
        {
            Transit transit = Transit.getInstance();
            setupMonitors( transit, (Adapter) getLogger() );
            URI uri = new URI( path );
            URL url = new URL( uri.toASCIIString() );
            File file = (File) url.getContent( new Class[]{File.class} );
            getLogger().info( "Cached as: " + file );
        }
        catch( Throwable e )
        {
            final String error = "ERROR: Could not complete get request.";
            getLogger().error( error, e );
        }
    }

    /*
    public static Registry getRegistry( Connection connection, boolean create ) 
      throws RemoteException, ConnectException
    {
        if( null == connection )
        {
            return null;
        }
        else
        {
            String host = connection.getHost();
            int port = connection.getPort();

            if( ( null == host ) || ( "localhost".equals( host ) ) )
            {
                if( false == create )
                {
                    return getLocalRegistry( port );
                }
                try
                {
                    Registry registry = LocateRegistry.createRegistry( port );
                    getLogger().info( "created local registry on port " + port );
                    return registry;
                }
                catch( RemoteException e )
                {
                    return getLocalRegistry( port );
                }
            }
            else
            {
                return LocateRegistry.getRegistry( host, port );
            }
        }
    }
    */

    /*
    private static Registry getLocalRegistry( int port ) throws RemoteException 
    {
        Registry registry = LocateRegistry.getRegistry( port );
        getLogger().info( "using local registry on port " + port );
        return registry;
    }
    */

    private static DepotClassLoader getSystemClassLoader()
    {
        return (DepotClassLoader) ClassLoader.getSystemClassLoader();
    }

    private static void clearPreferences( Preferences prefs ) throws Exception
    {
        getLogger().info( "Resetting: " + prefs );
        prefs.removeNode();
    }

    private static URL getCodeSourceLocation()
    {
        return Main.class.getProtectionDomain().getCodeSource().getLocation();
    }

    private static Preferences getRootPreferences()
    {
        return ROOT_PREFS;
    }

    private static Logger getLogger()
    {
        if( null == m_LOGGER )
        {
            String category = System.getProperty( "dpml.logging.category", "depot" );
            m_LOGGER = new LoggingAdapter( java.util.logging.Logger.getLogger( category ) );
        }
        return m_LOGGER;
    }

    private static void handleHelp()
    {
        final String message = 
          "Help"
          + "\n\nUsage: depot [-help] | [-version] | [-setup] | [-prefs] | [-get [artifact]] | [-exec [spec]]"
          + "\n\nAvailable command line options:"
          + "\n"
          + "\n -help             List command line help."
          + "\n -version          List version information."
          + "\n -run [plugin]     Load and run the plugin."
          + "\n -prefs            Start the preferences editor."
          + "\n -build            Launch the project builder."
          + "\n -exec [spec]      Launch an application using a supplied specification."
          + "\n -reset            Clear Depot and Transit preferences."
          + "\n -setup            DPML system setup (use -setup -help for additional info)"
          + "\n -station          Start the DPML Station process"
          + "\n -debug            Enable debug level logging."
          + "\n -D[name]=[value]  Set one or more system properties."
          + "\n";
        getLogger().info( message );
    }

    private static void handleVersion( boolean debug )
    {
        final String message = 
          "Version\n"
          + "\n"
          + "\n  Process ID:      \t" + PROCESS_ID
          + "\n  Java Runtime:    \t" 
          + System.getProperty( "java.runtime.name" ) + " ("
          + System.getProperty( "java.version" ).replace( '_', ' ' ) + ")"
          + "\n  Depot Console:   \t@DEPOT-CONSOLE-URI@"
          + "\n  Exec Handler:    \t@DEPOT-EXEC-URI@"
          + "\n  Build Handler:   \t@DEPOT-BUILD-URI@"
          + "\n  Install Handler: \t@DEPOT-INSTALL-URI@"
          + "\n  Prefs Handler:   \t@DEPOT-PREFS-URI@"
          + "\n  Station Handler: \t@DEPOT-STATION-URI@"
          + "\n  Transit Library: \t@TRANSIT-CORE-URI@"
          + "\n  Profile Library: \t@PROFILE-PLUGIN-URI@"
          + "\n";
        getLogger().info( message );

        if( debug )
        {
            System.getProperties().list( System.out );
        }
    }

    // TODO: add support for transit configuration profile selection

    /*
    private static TransitModel loadTransitModel( String[] args, Logger logger, boolean resolve )
      throws Exception
    {
        if( false == resolve )
        {
            return new DefaultTransitModel( logger );
        }

        int port = Registry.REGISTRY_PORT;
        Connection connection = new Connection( null, port, true, true );

        logger.info( "resolving transit model" );

        try
        {
            Registry registry = getRegistry( connection, false );
            return (TransitModel) registry.lookup( "//dpml/transit/default" );
        }
        catch( ConnectException e )
        {
            return new DefaultTransitModel( logger );
        }
        catch( NotBoundException e )
        {
            return new DefaultTransitModel( logger );
        }
    }
    */

    //--------------------------------------------------------------------------
    // static utilities for setup of logging manager and root prefs
    //--------------------------------------------------------------------------

   /**
    * DPML build key.
    */
    private static final String BUILD_KEY = "dpml.build";

   /**
    * The Depot system version.
    */
    private static final String BUILD_ID = "@BUILD-ID@";

    static
    {
        //setSystemProperty( "java.util.logging.manager", "net.dpml.depot.logging.LoggingManager" );
        //setSystemProperty( "java.util.logging.manager.altclassloader", "net.dpml.depot.logging.LoggingManager" );

        setSystemProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        setSystemProperty( "java.util.logging.config.class", "net.dpml.transit.util.ConfigurationHandler" );
        setSystemProperty( "java.rmi.server.RMIClassLoaderSpi", "net.dpml.depot.DepotRMIClassLoaderSpi" );
        setSystemProperty( Transit.SYSTEM_KEY, Transit.DPML_SYSTEM.getAbsolutePath() );
        setSystemProperty( Transit.HOME_KEY, Transit.DPML_HOME.getAbsolutePath() );
        setSystemProperty( Transit.DATA_KEY, Transit.DPML_DATA.getAbsolutePath() );
        setSystemProperty( Transit.PREFS_KEY, Transit.DPML_PREFS.getAbsolutePath() );
        setSystemProperty( BUILD_KEY, BUILD_ID );
    }

    private static void setSystemProperty( String key, String value )
    {
        if( null == System.getProperty( key ) )
        {
            System.setProperty( key, value );
        }
    } 

    private static Logger m_LOGGER = null;
    private static final Preferences ROOT_PREFS = 
      Preferences.userNodeForPackage( Main.class );

    private String getSwitch( String[] args )
    {
        if( args.length == 0 ) 
        {
            return "-help";
        }
        else
        {
            for( int i=0; i<args.length; i++ )
            {
                String arg = args[i];
                if( arg.startsWith( "-" ) && ( arg.length() > 1 ) )
                {
                    if( "-run".equals( arg ) || "-build".equals( arg )
                      || "-exec".equals( arg ) || "-station".equals( arg ) || "-setup".equals( arg )
                      || "-prefs".equals( arg ) || "-help".equals( arg ) )
                    {
                        return arg;
                    }
                }
            }
            return args[0];
        }
    }

    private String[] processSystemProperties( String[] args )
    {
        ArrayList result = new ArrayList();
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            int index = arg.indexOf( "=" );
            if( index > -1 && arg.startsWith( "-D" ) )
            {
                String name = arg.substring( 2, index );
                String value = arg.substring( index + 1 );
                System.setProperty( name, value );
            }
            else
            {
                result.add( arg );
            }
        }
        return (String[]) result.toArray( new String[0] );
    }

   /**
    * Setup the monitors.
    */
    private static void setupMonitors( Transit instance, Adapter adapter ) throws Exception
    {
        instance.getRepositoryMonitorRouter().addMonitor(
          new RepositoryMonitorAdapter( adapter ) );
        instance.getCacheMonitorRouter().addMonitor(
          new CacheMonitorAdapter( adapter ) );
        instance.getNetworkMonitorRouter().addMonitor(
          new NetworkMonitorAdapter( adapter ) );
    }
}

