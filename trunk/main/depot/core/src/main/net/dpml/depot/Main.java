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

import java.net.URL;
import java.net.URI;
import java.rmi.RMISecurityManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import net.dpml.transit.Disposable;
import net.dpml.transit.Transit;
import net.dpml.transit.TransitError;
import net.dpml.transit.Repository;
import net.dpml.transit.RepositoryException;
import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.monitor.Adapter;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.monitor.RepositoryMonitorAdapter;
import net.dpml.transit.monitor.CacheMonitorAdapter;
import net.dpml.transit.monitor.NetworkMonitorAdapter;

import net.dpml.lang.Enum;
import net.dpml.lang.PID;
import net.dpml.lang.Logger;

/**
 * CLI hander for the depot package.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Main //implements ShutdownHandler
{
    private static Main m_MAIN;
    private static final PID PROCESS_ID = new PID();

    private Object m_plugin;
    private boolean m_debug = false;
    
   /**
    * Processes command line options to establish the command handler plugin to deploy.
    * Command parameters recognixed by the console include the following:
    * <ul>
    *   <li>-Ddpml.depot.addplication=transit|station|metro|build</li>
    *   <li>-debug</li>
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

        //
        // check for debug mode
        //
        
        if( CLIHelper.isOptionPresent( args, "-debug" ) )
        {
            args = CLIHelper.consolidate( args, "-debug" );
            System.setProperty( "dpml.debug", "true" );
        }
        
        if( "true".equals( System.getProperty( "dpml.debug" ) ) )
        {
            m_debug = true;
        }
        
        if( null == System.getProperty( "dpml.logging.config" ) )
        {
            if( m_debug )
            {
                System.setProperty( "dpml.logging.config", "local:properties:dpml/transit/debug" );
            }
            else
            {
                System.setProperty( "dpml.logging.config", "local:properties:dpml/transit/default" );
            }
        }
        
        if( m_debug )
        {
            for( int i=0; i<arguments.length; i++ )
            {
                getLogger().debug( "arg[" + i + "]: " + arguments[i] );
            }
        }
        
        //
        // handle cli sub-system establishment
        //
        
        Command command = getCommand( args );
        if( Command.BUILD.equals( command ) )
        {
            handleBuild( args );
        }
        else if( Command.TRANSIT.equals( command ) )
        {
            handleTransit( args );
        }
        else if( Command.METRO.equals( command ) )
        {
            handleMetro( args );
        }
        else if( Command.STATION.equals( command ) )
        {
            handleStation( args );
        }
        else
        {
            final String error = 
              "Missing application key '" + APPLICATION_KEY + "'.";
            System.err.println( error );
            System.exit( 1 );
        }
    }
    
    private void handleBuild( String[] args )
    {
        String name = "build";
        String spec = "@DEPOT-BUILDER-URI@";
        handlePlugin( name, spec, args, false );
    }

    private void handleMetro( String[] args )
    {
        String name = "exec";
        String spec = "@DEPOT-EXEC-URI@";
        handlePlugin( name, spec, args, true );
    }

    private void handleTransit( String[] args )
    {
        String name = "transit";
        String spec = "@TRANSIT-CONSOLE-URI@";
        handlePlugin( name, spec, args, false );
    }

    private void handleStation( String[] args )
    {
        new File( Transit.DPML_DATA, "logs/station" ).mkdirs();
        if( CLIHelper.isOptionPresent( args, "-server" ) )
        {
            String name = "station";
            args = CLIHelper.consolidate( args, "-server" );
            String spec = "@DEPOT-STATION-SERVER-URI@";
            handlePlugin( name, spec, args, true );
        }
        else
        {
            String name = "station";
            String spec = "@DEPOT-STATION-URI@";
            handlePlugin( name, spec, args, false );
        }
    }

    private void handlePlugin( String name, String spec, String[] args, boolean wait )
    {
        System.setSecurityManager( new RMISecurityManager() );
        TransitModel model = getTransitModel( args );
        boolean waitForCompletion = deployHandler( model, name, spec, args, wait );
        if( !waitForCompletion )
        {
            if( m_plugin instanceof Disposable )
            {
                Disposable disposable = (Disposable) m_plugin;
                disposable.dispose();
            }
            if( model instanceof DefaultTransitModel )
            {
                DefaultTransitModel disposable = (DefaultTransitModel) model;
                disposable.dispose();
            }
            System.exit( 0 );
        }
    }
    
    private boolean deployHandler( 
      TransitModel model, String command, String path, String[] args, boolean waitFor )
    {
        Logger logger = getLogger();
        if( logger.isDebugEnabled() )
        {
            logger.debug( "date: " + new Date() );
            logger.debug( "system: " + command );
            logger.debug( "uri: " + path );
            logger.debug( "args: [" + toString( args ) + "]" );
        }
        Logger log = resolveLogger( logger, command );
        try
        {
            URI uri = new URI( path );
            Transit transit = Transit.getInstance( model );
            setupMonitors( transit, (Adapter) logger );
            Repository repository = transit.getRepository();
            m_plugin = 
              repository.getPlugin( 
                ClassLoader.getSystemClassLoader(), 
                uri, 
                new Object[]
                {
                    model, 
                    args, 
                    log
                }
              );
        }
        catch( RepositoryException e )
        {
            Throwable cause = e.getCause();
            if( ( null != cause ) && ( cause instanceof GeneralException ) )
            {
                getLogger().error( cause.getMessage() );
                System.exit( 1 );
            }
            else
            {
                getLogger().error( e.getMessage(), e.getCause() );
                System.exit( 1 );
            }
        }
        catch( GeneralException e )
        {
            getLogger().error( e.getMessage() );
            System.exit( 1 );
        }
        catch( Throwable e )
        {
            final String error = 
              "Deloyment failure." 
              + "\nTarget: " + command 
              + "\n   URI: " + path;
            getLogger().error( error, e );
            System.exit( 1 );
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
    
    private Logger resolveLogger( Logger logger, String command )
    {
        String partition = System.getProperty( "dpml.station.partition", null );
        if( null != partition )
        {
            return new LoggingAdapter( partition );
        }
        else
        {
            return logger.getChildLogger( command );
        }
    }
    
    private TransitModel getTransitModel( String[] args )
    {
        final String key = "dpml.transit.model";
        String property = null;
        for( int i=0; i<args.length; i++ )
        {
            String arg = args[i];
            if( arg.startsWith( "-D" + key + "=" ) )
            {
                property = arg.substring( 21 );
                break;
            }
        }
        
        if( null != property )
        {
            if( property.startsWith( "registry:" ) )
            {
                try
                {
                    return (TransitModel) new URL( property ).getContent( 
                      new Class[]{TransitModel.class} );
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unable to resolve registry reference: " + property;
                    throw new TransitError( error, e );
                }
            }
            else
            {
                final String error = 
                  "System property value for the key ': "
                  + key 
                  + "' contains an unrecognized value: "
                  + property;
                throw new TransitError( error );
            }
        }
        
        //
        // otherwise let Transit handle model creation
        //
        
        try
        {
            Logger logger = getLogger().getChildLogger( "transit" );
            return DefaultTransitModel.getDefaultModel( logger );
        }
        catch( Exception e )
        {
            final String error = 
              "Transit model establishment failure.";
            throw new TransitError( error, e );
        }
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

    private String toString( String[] args )
    {
        StringBuffer buffer = new StringBuffer();
        for( int i=0; i<args.length; i++ )
        {
            if( i > 0 )
            {
                buffer.append( ", " );
            }
            buffer.append( args[i] );
        }
        return buffer.toString();
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

    //--------------------------------------------------------------------------
    // static utilities for setup of logging manager and root prefs
    //--------------------------------------------------------------------------

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
                      boolean ignorable = true;
                  }
                  System.runFinalization();
              }
          }
        );
    }

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
    
    private Command getCommand( String[] args )
    {
        String ref = getApplicationReference( args );
        String app = System.getProperty( APPLICATION_KEY, ref );
        return Command.parse( app );
    }

    private String getApplicationReference( String[] args )
    {
        String key = "-D" + APPLICATION_KEY + "=";
        for( int i=0; i<args.length; i++ )
        {
            String arg = args[i];
            if( arg.startsWith( key ) )
            {
                return arg.substring( 25 );
            }
        }
        return null;
    }

   /**
    * Application selection key.
    */
    public static final String APPLICATION_KEY = "dpml.depot.application";
    
   /**
    * Application identifier enumeration.
    */
    private static final class Command extends Enum
    {
        static final long serialVersionUID = 1L;

       /**
        * Transit command id.
        */
        public static final Command TRANSIT = new Command( "dpml.transit" );

       /**
        * Metro command id.
        */
        public static final Command METRO = new Command( "dpml.metro" );
    
       /**
        * Station command id.
        */
        public static final Command STATION = new Command( "dpml.station" );
    
       /**
        * Builder command id.
        */
        public static final Command BUILD = new Command( "dpml.builder" );
    
       /**
        * Internal constructor.
        * @param label the enumeration label.
        * @param index the enumeration index.
        * @param map the set of constructed enumerations.
        */
        private Command( String label )
        {
            super( label );
        }
        
       /**
        * Create a now mode using a supplied mode name.
        * @param value the mode name
        * @return the mode
        * @exception NullPointerException if the supplied value is null
        * @exception IllegalArgumentException if the supplied value is not recognized
        */
        public static Command parse( String value ) throws NullPointerException, IllegalArgumentException
        {
            if( null == value )
            {
                final String error = 
                  "Undefined sub-system identifier."
                  + "\nThe depot cli handler must be supplied with an -D"
                  + APPLICATION_KEY + "=[id] where id is one of the value 'dpml.metro', "
                  + "'dpml.transit', 'dpml.station' or 'dpml.build'.";
                throw new NullPointerException( error ); 
            }
            if( value.equalsIgnoreCase( "dpml.metro" ) )
            {
                return METRO;
            }
            else if( value.equalsIgnoreCase( "dpml.transit" ) )
            {
                return TRANSIT;
            }
            else if( value.equalsIgnoreCase( "dpml.station" ) )
            {
                return STATION;
            }
            else if( value.equalsIgnoreCase( "dpml.builder" ) )
            {
                return BUILD;
            }
            else
            {
                final String error =
                  "Unrecognized application id [" + value + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }
}

