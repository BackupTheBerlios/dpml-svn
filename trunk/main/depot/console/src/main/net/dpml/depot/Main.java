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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.Remote;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.rmi.activation.ActivationSystem;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitError;
import net.dpml.transit.TransitException;
import net.dpml.transit.Artifact;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;
import net.dpml.transit.monitor.Adapter;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.monitor.RepositoryMonitorAdapter;
import net.dpml.transit.monitor.CacheMonitorAdapter;
import net.dpml.transit.monitor.NetworkMonitorAdapter;
import net.dpml.transit.Repository;

/**
 * CLI hander for the depot package.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Main.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public final class Main implements ShutdownHandler
{
    private static Main MAIN;

    private Object m_plugin;

    //private static Object OBJECT;

    public static void start( final String[] args )
        throws Exception
    {
        System.out.println( "starting station" );
        Main.main( new String[]{ "-station" } );
    }

    public static void stop( final String[] args )
        throws Exception
    {
        if( null != MAIN )
        {
            MAIN.exit();
        }
        else
        {
            System.exit( 0 );
        }
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
    * </ul>
    */
    public static void main( String[] args )
        throws Exception
    {
        if( null != MAIN )
        {
            final String error = 
              "Console already established.";
            throw new IllegalArgumentException( error );
        }
        else
        {
            MAIN = new Main( args );
        }
    }

    private String getSwitch( String[] args )
    {
        if( args.length == 0 ) 
        {
            return "-help";
        }
        else
        {
            return args[0];
        }
    }

    private Main( String[] arguments )
    {
        String[] args = arguments;
        if( isFlagPresent( args, "-debug" ) )
        {
            args = consolidate( args, "-debug" );
            System.setProperty( "dpml.logging.level", "FINE" );
        }

        String option = getSwitch( args );

        if( "-help".equals( option ) )
        {
            handleHelp();
            exit();
        }
        else if( "-reset".equals( option ) )
        {
            int result = handleReset();
            exit( result );
        }
        else if( "-version".equals( option ) )
        {
            handleVersion();
            exit();
        }
        else if( "-get".equals( option ) )
        {
            for( int i=0; i < args.length; i++ )
            {
                String arg = args[i];
                if( arg.equals( "-get" ) )
                {
                    if( i+1 < args.length )
                    {
                        String path = args[i+1];
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
            args = consolidate( args, "-setup" );
            handleSetup( args );
        }
        else if( "-prefs".equals( option ) )
        {
            args = consolidate( args, "-prefs" );
            handlePrefs( args );
        }
        else if( "-exec".equals( option ) )
        {
            args = consolidate( args, "-exec" );
            handleExec( args );
        }
        else if( "-station".equals( option ) )
        {
            args = consolidate( args, "-station" );
            handleStation( args );
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
            clearPreferences( Main.class );
            clearPreferences( Transit.class );
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

    private void handleExec( String[] args )
    {
        String name = "exec";
        String spec = "@DEPOT-EXEC-URI@";
        handlePlugin( name, spec, args );
    }

    private void handleStation( String[] args )
    {
        String name = "station";
        String spec = "@DEPOT-STATION-URI@";
        handlePlugin( name, spec, args );
    }

    private void handlePlugin( String name, String spec, String[] args )
    {
	  System.setSecurityManager(new RMISecurityManager());

        // get setup uri

        Preferences prefs = getRootPreferences();
        Preferences handlers = prefs.node( "handlers" );
        Preferences setup = handlers.node( name );
        String path = setup.get( "uri", spec );
        
        // deploy plugin

        boolean waitForCompletion = deployHandler( name, path, args, this, true );
        if( false == waitForCompletion )
        {
            exit();
        }
    }

    private boolean deployHandler( String command, String path, String[] args, ShutdownHandler shutdown, boolean waitFor )
    {
        Logger logger = getLogger().getChildLogger( command );
        try
        {
            Preferences prefs = getRootPreferences();
            URI uri = new URI( path );
            TransitModel model = getTransitModel( args );
            Repository repository = Transit.getInstance( model ).getRepository();
            ClassLoader classloader = getSystemClassLoader();
            m_plugin = 
              repository.getPlugin( 
                classloader, uri, new Object[]{ model, args, logger, shutdown, prefs } );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to deploy the [" + command + "] handler due to deployment failure.";
            getLogger().error( error, e );
        }

        if( null == m_plugin )
        {
            return false;
        }
        else if( m_plugin instanceof Runnable )
        {
            Thread thread = null;
            getLogger().info( "starting " + m_plugin.getClass().getName() );
            thread = new Thread( (Runnable) m_plugin );
            thread.start();
            setShutdownHook( thread );
            return true;
        }
        else
        {
            getLogger().info( "deployed " + m_plugin.getClass().getName() );
            return waitFor;
        }
    }

    private TransitModel getTransitModel( String[] args ) throws RemoteException
    {
        //
        // TODO: impriove this so that we can direct the selction of the transit model
        // in a way that would allow resolution of the model from the rmi registry
        // e.g. -model rmi://some/name
        //

        Logger logger = getLogger();
        return new DefaultTransitModel( logger );
    }


   /*
        boolean install = isFlagPresent( args, "-install" );
        boolean remove = isFlagPresent( args, "-remove" );
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
                    args = consolidate( args, "-prefs" );
                    model = loadTransitModel( args, logger, true );
                    profile = createPrefsProfile( logger );
                    break;
                }
                else if( arg.equals( "-station" ) )
                {
                    args = consolidate( args, "-station" );
                    model = loadTransitModel( args, logger, false );
                    profile = createStationProfile( logger );
                    break;
                }
                else if( arg.equals( "-install" ) )
                {
                    args = consolidate( args, "-install" );
                    model = loadTransitModel( args, logger, false );
                    profile = createInstallProfile( logger, true );
                    break;
                }
                else if( arg.equals( "-remove" ) )
                {
                    args = consolidate( args, "-remove" );
                    model = loadTransitModel( args, logger, false );
                    profile = createInstallProfile( logger, false );
                    break;
                }
                else if( arg.equals( "-profile" ) )
                {
                    //args = consolidate( args, "-profile" );
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
                      + connection.getPort() + "/" + id );
                }
                else
                {
                    logger.info( 
                       "service bound to " + connection.getHost() 
                       + ":" + connection.getPort() + "/" + id );
                }
            }
            else if( OBJECT instanceof Runnable )
            {
                //
                // run the plugin
                //
    
                Thread thread = null;
                logger.info( "starting " + OBJECT.getClass().getName() );
                thread = new Thread( (Runnable) OBJECT );
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
            //TransitModel model = loadTransitModel( args, logger, false );
            //Transit transit = Transit.getInstance( model );
            Transit transit = Transit.getInstance();
            setupMonitors( transit, (Adapter) getLogger() );
            URI uri = new URI( path );
            URL url = new URL( uri.toASCIIString() );
            File file = (File) url.getContent( new Class[]{ File.class } );
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

    private static ClassLoader getSystemClassLoader()
    {
        return ClassLoader.getSystemClassLoader();
    }

    private static void clearPreferences( Class c ) throws Exception
    {
        Preferences prefs = Preferences.userNodeForPackage( c );
        getLogger().info( "Resetting: " + prefs );
        prefs.removeNode();
    }

    private static URL getCodeSourceLocation()
    {
        return Main.class.getProtectionDomain().getCodeSource().getLocation();
    }

   /**
    * Create a shutdown hook that will trigger shutdown of the supplied plugin.
    * @param thread the application thread
    */
    private static void setShutdownHook( final Object object )
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
                  if( ( null != object ) && ( object instanceof Thread ) )
                  {
                      try
                      {
                          ((Thread)object).interrupt();
                      }
                      catch( Throwable e )
                      {
                          // ignore it
                      }
                  }
                  System.runFinalization();
              }
          }
        );
    }

    private static Preferences getRootPreferences()
    {
        return ROOT_PREFS;
    }

    private static Logger getLogger()
    {
        if( null == LOGGER )
        {
            String category = System.getProperty( "dpml.logging.category", "depot" );
            LOGGER = new LoggingAdapter( java.util.logging.Logger.getLogger( category ) );
        }
        return LOGGER;
    }

    private static void handleHelp()
    {
        final String message = 
          "Help"
          + "\n\nUsage: depot [-help] | [-version] | [-setup] | [-prefs] | [-get [artifact]] | [-exec [key]]"
          + "\n\nAvailable command line options:"
          + "\n"
          + "\n -help             List command line help."
          + "\n -version          List version information."
          + "\n -get [artifact]   Load the supplied artifact to the cache."
          + "\n -prefs            Start the preferences editor."
          + "\n -exec [key]       Launch an application using a supplied profile key."
          + "\n -reset            Clear Depot and Transit preferences."
          + "\n -setup            DPML system setup (use -setup -help for additional info)"
          + "\n -debug            Enable debug level logging."
          + "\n";
        getLogger().info( message );
    }

    private static void handleVersion()
    {
        final String message = 
          "Version\n"
          + "\n"
          + "\n  Depot Console: \t@DEPOT-CONSOLE-URI@"
          + "\n  Exec Handler: \t@DEPOT-EXEC-URI@"
          + "\n  Install Handler: \t@DEPOT-INSTALL-URI@"
          + "\n  Prefs Handler: \t@DEPOT-PREFS-URI@"
          + "\n  Station Handler: \t@DEPOT-STATION-URI@"
          + "\n  Transit Library: \t@TRANSIT-CORE-URI@"
          + "\n  Profile Library: \t@PROFILE-PLUGIN-URI@"
          + "\n";
        getLogger().info( message );
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
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        System.setProperty( "java.util.logging.manager", "net.dpml.depot.logging.LoggingManager" );
        System.setProperty( "java.util.logging.manager.altclassloader", "net.dpml.depot.logging.LoggingManager" );
        System.setProperty( "java.util.logging.config.class", "net.dpml.transit.util.ConfigurationHandler" );
        System.setProperty( "java.rmi.server.RMIClassLoaderSpi", "net.dpml.depot.lang.DepotRMIClassLoaderSpi" );
        System.setProperty( Transit.SYSTEM_KEY, Transit.DPML_SYSTEM.getAbsolutePath() );
        System.setProperty( Transit.HOME_KEY, Transit.DPML_HOME.getAbsolutePath() );
        System.setProperty( Transit.DATA_KEY, Transit.DPML_DATA.getAbsolutePath() );
        System.setProperty( Transit.PREFS_KEY, Transit.DPML_PREFS.getAbsolutePath() );
        System.setProperty( BUILD_KEY, BUILD_ID );
    }

    private static Logger LOGGER = null;
    private static final Preferences ROOT_PREFS = 
      Preferences.userNodeForPackage( Main.class );

    private static boolean isFlagPresent( String[] args, String flag )
    {
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( arg.equals( flag ) )
            {
                return true;
            }
        }
        return false;
    }

    private static String[] consolidate( String [] args, String argument )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( false == arg.equals( argument ) )
            {
                list.add( arg );
            }
        }
        return (String[]) list.toArray( new String[0] );
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

