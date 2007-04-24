/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.util;

import dpml.transit.StandardMonitor;

import java.net.URI;
import java.net.URL;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.LogManager;
import java.util.Properties;

import javax.tools.Tool;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;

import net.dpml.util.Logger;

import static net.dpml.transit.Transit.VERSION;

/**
 * CLI hander for the depot package.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Main
{
    static
    {
        final String version = VERSION; // trigger static field initialization
    }
    
    private static Main m_MAIN;
    private static final PID PROCESS_ID = new PID();

    private Object m_plugin;
    private boolean m_debug = false;
    private boolean m_trace = false;
    
   /**
    * Processes command line options to establish the command handler plugin to deploy.
    * Command parameters recognixed by the console include the following:
    * <ul>
    *   <li>-Ddpml.depot.application=[uri]</li>
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
        
        ClassLoader system = ClassLoader.getSystemClassLoader();
        Thread.currentThread().setContextClassLoader( system );
        
        //
        // check for debug and trace cli options
        //
        
        
        if( CLIHelper.isOptionPresent( args, "-trace" ) )
        {
            args = CLIHelper.consolidate( args, "-trace" );
            System.setProperty( "dpml.trace", "true" );
            m_trace = true;
        }
        
        if( CLIHelper.isOptionPresent( args, "-debug" ) )
        {
            args = CLIHelper.consolidate( args, "-debug" );
            System.setProperty( "dpml.debug", "true" );
            m_debug = true;
        }
        
        //
        // handle cli sub-system establishment
        //
        
        String path = System.getProperty( APPLICATION_KEY, null );
        if( null == path )
        {
            System.out.println( "Undefined system property '" + APPLICATION_KEY + "'." );
            System.exit( -1 );
        }
        
        if( null == System.getProperty( "dpml.logging.config" ) )
        {
            if( m_trace )
            {
               setSystemProperty( "dpml.logging.config", "local:properties:dpml/transit/trace" );
            }
            else if( m_debug )
            {
                System.setProperty( "dpml.logging.config", "local:properties:dpml/transit/debug" );
            }
            else
            {
                System.setProperty( "dpml.logging.config", "local:properties:dpml/transit/default" );
            }
        }
    
        if( m_trace )
        {
            for( int i=0; i<arguments.length; i++ )
            {
                getLogger().debug( "arg[" + i + "]: " + arguments[i] );
            }
            
            int id = System.identityHashCode( system );
            
            getLogger().trace( system.toString() );
        }
        final int exit = deployHandler( path, args, false );
        if( exit < 1 )
        {
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "exit " + exit );
            }
            Thread thread = new Thread(
              new Runnable()
              {
                  public void run()
                  {
                    System.exit( exit );
                  }
              }
            );
            thread.start();
        }
        else
        {
            if( getLogger().isTraceEnabled() )
            {
                getLogger().trace( "joining" );
            }
            try
            {
                Thread.currentThread().join();
            }
            catch( InterruptedException e )
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().info( "exit/interrupt" );
                }
            }
            finally
            {
                if( getLogger().isTraceEnabled() )
                {
                    getLogger().info( "exit " + exit );
                }
                Thread thread = new Thread(
                  new Runnable()
                  {
                      public void run()
                      {
                        System.exit( exit );
                      }
                  }
                );
                thread.start();
            }
        }
    }
    
    private int deployHandler( 
      String path, String[] args, boolean waitFor )
    {
        System.setSecurityManager( new RMISecurityManager() );
        Logger logger = getLogger();
        if( logger.isTraceEnabled() )
        {
            logger.trace( "date: " + new Date() );
            logger.trace( "uri: " + path );
            logger.trace( "args: [" + toString( args ) + "]" );
            logger.trace( "system classloader: [" 
              + System.identityHashCode( ClassLoader.getSystemClassLoader() ) 
              + "]" );
        }
        Logger log = resolveLogger( logger );
        try
        {
            URI uri = new URI( path );
            Transit transit = Transit.getInstance();
            transit.addMonitor( new StandardMonitor( logger ) );
            
            //
            // implementation is limited to uris recognized as artifacts that
            // resolve to a javax.tools.Tool implementation
            //
            
            if( Artifact.isRecognized( uri ) )
            {
                URL url = Artifact.toURL( uri );
                Tool tool = (Tool) url.getContent( new Class[]{Tool.class} );
                if( null == tool )
                {
                    final String error = 
                      "The target uri ["
                      + uri 
                      + "] did not return a value for the content class ["
                      + Tool.class.getName()
                      + "].";
                    logger.error( error );
                    return -1;
                }
                try
                {
                    return tool.run( null, null, null, args );
                }
                catch( Throwable ee )
                {
                    final String error = 
                      "Tool execution error."
                      + "\nTool: " + tool.getClass().getName()
                      + "\nURI: " + uri;
                    processError( error, ee );
                    return -1;
                }
            }
            else
            {
                final String error = 
                  "URI scheme not recognized ["
                  + uri
                  + "]";
                logger.error( error );
                return -1;
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Tool creation error."
              + "\nURI: " + path;
            processError( error, e );
            return -1;
        }
    }
    
    private void processError( String message, Throwable cause )
    {
        Logger logger = getLogger();
        if( logger.isTraceEnabled() )
        {
            logger.error( message, cause );
        }
        /*
        try
        {
            logger.info( "# START" );
            NotificationStatus status = NotificationHelper.getNotificationStatus( cause );
            logger.info( "# STATUS: " + status + ", " + cause.getClass() );
            if( NotificationStatus.INFO.equals( status ) )
            {
                logger.info( message + "\n" + cause.getMessage() );
            }
            else if( NotificationStatus.WARN.equals( status ) )
            {
                logger.warn( message + "\n" + cause.getMessage() );
            }
            else
            {
                logger.error( message, cause );
            }
            logger.info( "# END" );
        }
        catch( Throwable e )
        {
            logger.error( message, cause );
        }
        */
    }
    
    private Logger resolveLogger( Logger logger )
    {
        String partition = System.getProperty( "dpml.station.partition", null );
        if( null != partition )
        {
            return new DefaultLogger( partition );
        }
        else
        {
            return logger;
        }
    }
    
    private static Logger getLogger()
    {
        if( null == m_LOGGER )
        {
            try
            {
                LogManager.getLogManager().readConfiguration();
            }
            catch( Throwable e )
            {
                e.printStackTrace();
            }
            String category = System.getProperty( "dpml.logging.category", "dpml.lang" );
            m_LOGGER = new DefaultLogger( java.util.logging.Logger.getLogger( category ) );
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
    
   /**
    * For all of the supplied command line arguments, if the 
    * argument is in the form -Dabc=def then extract the argument from
    * the array and apply it as a system property.  All non-system property
    * arguments are included in the returned argument array.
    *
    * @param args the supplied commandline arguments including 
    *   system property assignments
    * @return the array of pure command line arguments (excluding
    *   and arg values recognized as system property declarations
    */
    private String[] processSystemProperties( String[] args )
    {
        ArrayList<String> result = new ArrayList<String>();
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            int index = arg.indexOf( "=" );
            if( index > -1 && arg.startsWith( "-D" ) )
            {
                String name = arg.substring( 2, index );
                String raw = arg.substring( index + 1 );
                Properties system = System.getProperties();
                String value = PropertyResolver.resolve( system, raw );
                System.setProperty( name, value );
            }
            else
            {
                result.add( arg );
            }
        }
        return result.toArray( new String[0] );
    }

    //--------------------------------------------------------------------------
    // static utilities for setup of logging manager and root prefs
    //--------------------------------------------------------------------------
        
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
        setSystemProperty( "java.util.logging.config.class", "dpml.util.ConfigurationHandler" );
        setSystemProperty( "java.rmi.server.RMIClassLoaderSpi", "dpml.util.FederatingClassLoader" );
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

   /**
    * Application selection key.
    */
    public static final String APPLICATION_KEY = "dpml.depot.application";
}

