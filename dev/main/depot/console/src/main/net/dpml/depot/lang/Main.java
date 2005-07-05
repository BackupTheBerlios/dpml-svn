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

package net.dpml.depot.lang;

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
import net.dpml.transit.adapter.LoggingAdapter;
import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;
import net.dpml.transit.repository.Repository;

import net.dpml.depot.lang.DepotClassLoader;
import net.dpml.depot.profile.ApplicationProfile;
import net.dpml.depot.profile.DefaultApplicationProfile;
import net.dpml.depot.profile.DepotProfile;
import net.dpml.depot.profile.DefaultDepotProfile;
import net.dpml.depot.profile.Profile;
import net.dpml.depot.store.DepotHome;
import net.dpml.depot.unit.DepotStorageUnit;

/**
 * CLI hander for the depot package.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Main.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public final class Main
{
    private static Object OBJECT;

    public static void start( final String[] args )
        throws Exception
    {
        System.out.println( "starting station" );
        Main.main( new String[]{ "-station" } );
    }

    public static void stop( final String[] args )
        throws Exception
    {
        System.exit( 0 );
    }

    public static void main( String[] args )
        throws Exception
    {
        long start = new Date().getTime();
        boolean termination = false;

        System.setProperty( "dpml.logging.category", "depot" );
	  System.setSecurityManager(new RMISecurityManager());

        boolean help = isFlagPresent( args, "-help" );
        if( help )
        {
            handleHelp();
            exit();
        }

        boolean reset = isFlagPresent( args, "-reset" );
        if( reset )
        {
            clearPreferences( Main.class );
            clearPreferences( Transit.class );
            exit();
        }

        boolean version = isFlagPresent( args, "-version" );
        if( version )
        {
            handleVersion();
            exit();
        }

        boolean debug = isFlagPresent( args, "-debug" );
        if( debug )
        {
            args = consolidate( args, "-debug" );
            System.setProperty( "dpml.logging.level", "FINE" );
        }

        boolean metal = isFlagPresent( args, "-metal" );
        if( metal )
        {
            args = consolidate( args, "-metal" );
            System.setProperty( "dpml.depot.metal", "true" );
        }

        //
        // check if -get has been requested
        //
   
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( arg.equals( "-get" ) )
            {
                System.setProperty( "dpml.logging.category", "depot.get" );
                if( i+1 < args.length )
                {
                    String path = args[i+1];
                    handleGet( getLogger(), args, path );
                }
                else
                {
                    final String error = 
                      "Missing get parameter value.";
                    getLogger().error( error );
                }
                exit();
            }
        }

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

    private static void exit()
    {
        System.exit( 0 );
    }

    private static Object resolveTargetObject( 
      ClassLoader parent, URI uri, String[] args, DepotProfile manager, TransitModel model, 
      Logger logger, ApplicationProfile profile ) 
      throws Exception
    {
        Transit transit = Transit.getInstance( model );
        Repository loader = transit.getRepository();

        Artifact artifact = Artifact.createArtifact( uri );
        String type = artifact.getType();
        Properties params = profile.getProperties();
        if( "plugin".equals( type ) )
        {
            return loader.getPlugin( 
              parent, 
              uri, 
              new Object[]{ args, manager, model, logger, profile, params } );
        }
        else if( "part".equals( type ) )
        {
            String path = uri.toASCIIString();
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

    private static void handleGet( Logger logger, String[] args, String path ) throws Exception
    {
        TransitModel model = loadTransitModel( args, logger, false );
        Transit transit = Transit.getInstance( model );
        URI uri = new URI( path );
        URL url = new URL( uri.toASCIIString() );
        File file = (File) url.getContent( new Class[]{ File.class } );
        getLogger().info( "Cached as: " + file );
    }

    private static ApplicationProfile createPrefsProfile( Logger logger ) throws Exception
    {
        String id = "prefs";
        Logger log = logger.getChildLogger( "prefs" );
        boolean policy = true;
        URI uri = new URI( "@DEPOT-PREFS-URI@" );
        String title = "Depot Preferences Management";
        boolean command = false;
        Properties args = new Properties();
        return new DefaultApplicationProfile( 
          log, id, title, null, command, null, uri, true, args );
    }

    private static ApplicationProfile createInstallProfile( Logger logger, boolean install ) throws Exception
    {
        String id = "install";
        Logger log = logger.getChildLogger( "install" );
        boolean policy = true;
        URI uri = new URI( "link:plugin:dpml/depot/dpml-depot-install#LATEST" );
        String title = "Depot Installation Manager";
        boolean command = false;
        Properties args = new Properties();
        args.setProperty( "java.util.logging.ConsoleHandler.level", "SEVERE" );
        args.setProperty( "dpml.depot.install", "" + install );
        return new DefaultApplicationProfile( 
          log, id, title, null, command, null, uri, true, args );
    }

    private static ApplicationProfile createStationProfile( Logger logger ) throws Exception
    {
        String id = "station";
        Logger log = logger.getChildLogger( "station" );
        boolean policy = true;
        URI uri = new URI( "@STATION-PLUGIN-URI@" );
        String title = "DepotProfile Station";
        boolean command = false;
        Connection connection = new Connection();
        Properties args = new Properties();
        return new DefaultApplicationProfile( 
          log, id, title, null, command, connection, uri, true, args );
    }

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

    private static Registry getLocalRegistry( int port ) throws RemoteException 
    {
        Registry registry = LocateRegistry.getRegistry( port );
        getLogger().info( "using local registry on port " + port );
        return registry;
    }

    private static ApplicationProfile getApplicationProfile( DepotProfile manager, String target )
      throws Exception
    {
        return manager.getApplicationProfile( target );
    }

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

   /**
    * Setup the system properties for the target.
    * @param prefs the profile's system properties preferences node
    */
    private static void applySystemProperties( Properties properties ) throws BackingStoreException
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
                System.setProperty( key, value );
            }
        }
    }

    private static void deploy( Class c, String[] args ) throws Exception
    {
        try
        {
            Method method = c.getDeclaredMethod( "main", new Class[]{ String[].class } );
            Object[] params = new Object[]{ args };
            method.invoke( null, params );
            setShutdownHook( null );
        }
        catch( NoSuchMethodException e )
        {
            Thread thread = null;
            Object object = c.newInstance();
            if( object instanceof Runnable )
            {
                thread = new Thread( (Runnable) object );
                thread.start();
            }
            setShutdownHook( thread );
        }
    }

    private static String getTargetProfile( String[] args ) throws BackingStoreException
    {
        for( int i=0; i<args.length; i++ )
        {
            String arg = args[i];
            if( arg.equals( "-profile" ) )
            {
                if( i+1 < args.length )
                {
                    String profile = args[ i+1 ];
                    return profile;
                }
                else
                {
                    final String error = 
                      "Missing profile name.";
                    getLogger().error( error );
                    System.exit( -1 );
                }
            }
        }
        return null;
    }

   /**
    * Constructor (disabled).
    */
    private Main()
    {
        // disabled
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
          "DepotProfile application deployment."
          + "\n\nUsage: depot [-help] | [-version] | [-prefs] | [-get [artifact]] | [-profile [name]]"
          + "\n\nAvailable command line options:"
          + "\n\n -debug            Enable debug level logging."
          + "\n -get [artifact]   Load the supplied artifact to the cache."
          + "\n -help             List command line help."
          + "\n -install          Launch the package installation manager."
          + "\n -prefs            Start the DepotProfile preferences editor."
          + "\n -profile [name]   Launch a named application."
          + "\n -reset            Clear DepotProfile and Transit prefences."
          + "\n -version          List DepotProfile version information."
          + "\n"
          + "\nNB: The -install option may be invoked with the argument 'magic' "
          + "and 'metro'.  This is equivalent to entering the installation manager "
          + "and requesting installation of the respective packages. The -remove option "
          + "is also recognized as an alias to the installion manager."
          + "\n";
        getLogger().info( message );
    }

    private static void handleVersion()
    {
        final String message = 
          "DepotProfile Version.\n"
          + "\nConsole: \t@DEPOT-CONSOLE-URI@"
          + "\nPreferences: \t@DEPOT-PREFS-URI@"
          + "\nStation: \t@STATION-PLUGIN-URI@";
        getLogger().info( message );
    }

    // TODO: add support for transit configuration profile selection

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

}

