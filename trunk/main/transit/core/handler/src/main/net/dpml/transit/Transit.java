/*
 * Copyright 2004-2005 Stephen McConnell.
 * Copyright 2004-2005 Niclas Hedhman.
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

package net.dpml.transit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import net.dpml.transit.link.LinkManager;
import net.dpml.transit.monitor.RepositoryMonitorRouter;
import net.dpml.transit.monitor.CacheMonitorRouter;
import net.dpml.transit.monitor.NetworkMonitorRouter;
import net.dpml.transit.model.DefaultTransitModel;
import net.dpml.transit.model.TransitModel;

/**
 * The Transit class manages the establishment of a singleton transit instance
 * together with a service supporting the deployment of a application plugin and
 * access to transit monitor routers.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class Transit
{
    //------------------------------------------------------------------
    // static 
    //------------------------------------------------------------------

   /**
    * DPML home key.
    */
    public static final String HOME_KEY =            "dpml.home";

   /**
    * DPML home key.
    */
    public static final String DATA_KEY =            "dpml.data";

   /**
    * DPML home key.
    */
    public static final String PREFS_KEY =           "dpml.prefs";

   /**
    * Transit system key.
    */
    public static final String SYSTEM_KEY =          "dpml.system";

   /**
    * DPML environment variable string.
    */
    public static final String HOME_SYMBOL =         "DPML_HOME";

   /**
    * DPML environment variable string.
    */
    public static final String SYSTEM_SYMBOL =       "DPML_SYSTEM";

   /**
    * The DPML home directory established via assesment of the the ${dpml.home}
    * system property and the DPML_HOME environment variable.  If neither are
    * defined the behaviour is platform specific.  If the os is Windows,
    * the value returned is equivalent to $APPDATA\DPML whereas Unix the
    * environment will return ${user.home}/.dpml.
    */
    public static final File DPML_HOME;

   /**
    * If a system property named "dpml.system" is defined then the value
    * is assigned otherwise the implementation will look for an environment
    * variable named "DPML_SYSTEM". If neither case holds, the value assigned
    * if platform dependent.  If the os is Windows, the equivalent of
    * $PROGRAMFILES\DPML is assigned otherwise the value defaults to
    * "/usr/share/dpml".
    */
    public static final File DPML_SYSTEM;

   /**
    * The Transit personal data directory. The location of this diectory is system
    * dependent.
    */
    public static final File DPML_DATA;

   /**
    * The Transit personal preferences directory. The location of this diectory is system
    * dependent.
    */
    public static final File DPML_PREFS;

   /**
    * The Transit system version.
    */
    public static final String VERSION = "@PROJECT-VERSION@";

    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        System.setProperty( "dpml.transit.version", VERSION );

        DPML_HOME = resolveHomeDirectory();
        DPML_SYSTEM = resolveSystemDirectory( DPML_HOME );
        DPML_DATA = resolveDataDirectory( DPML_HOME );
        DPML_PREFS = resolvePreferencesDirectory( DPML_HOME );

        System.setProperty( SYSTEM_KEY, DPML_SYSTEM.getAbsolutePath() );
        System.setProperty( HOME_KEY, DPML_HOME.getAbsolutePath() );
        System.setProperty( DATA_KEY, DPML_DATA.getAbsolutePath() );
        System.setProperty( PREFS_KEY, DPML_PREFS.getAbsolutePath() );
    }

   /**
    * Returns the singleton instance of the transit system.  If this method
    * has already been invoked the server and monitor argument will be ignored.
    * @return the singleton transit instance
    * @exception TransitError if an error occurs during establishment
    */
    public static Transit getInstance() throws TransitError
    {
        synchronized( Transit.class )
        {
            if( m_INSTANCE == null )
            {
                try
                {
                    TransitModel model = new DefaultTransitModel();
                    return getInstance( model );
                }
                catch( Throwable e )
                {
                    String message = e.getMessage();
                    Throwable cause = e.getCause();
                    throw new TransitError( message, cause );
                }
            }
            else
            {
                return m_INSTANCE;
            }
        }
    }

   /**
    * Returns the singleton instance of the transit system.  If this method
    * has already been invoked the server and monitor argument will be ignored.
    *
    * @param model the activate transit model
    * @return the singleton transit instance
    * @exception IOException if an error occurs during establishment
    */
    public static Transit getInstance( TransitModel model )
        throws IOException
    {
        synchronized( Transit.class )
        {
            if( m_INSTANCE == null )
            {
                m_INSTANCE = new Transit( model );

                // before returning from this method we need to give the transit
                // subsystems a chance to complete initialization actions that 
                // are themselves dependent on an establish Transit instance

                m_INSTANCE.getTransitContext().initialize();
                return m_INSTANCE;
            }
            else
            {
                final String error = 
                  "Transit has already been initialized.";
                throw new TransitAlreadyInitializedException( error );
            }
        }
    }

    //------------------------------------------------------------------
    // state 
    //------------------------------------------------------------------

   /**
    * Singleton repository monitor router.
    */
    private RepositoryMonitorRouter m_repositoryMonitor;

   /**
    * Singleton cache monitor router.
    */
    private CacheMonitorRouter m_cacheMonitor;

   /**
    * Singleton network monitor router.
    */
    private NetworkMonitorRouter m_networkMonitor;

   /**
    * PrintWriter where operations troubleshooting messages
    * can be written to.
    */
    private PrintWriter m_logWriter;

    private SecuredTransitContext m_context;

   /**
    * Return the singleton transit content.
    * @return the context instance
    * @exception IllegalStateException if transit has not been initialized
    */
    SecuredTransitContext getTransitContext() throws IllegalStateException
    {
        if( null == m_context )
        {
            final String error = 
              "Transit context has not been initialized.";
            throw new IllegalStateException( error );
        }
        else
        {
            return m_context;
        }
    }

   /**
    * Private constructor of a transit instance.
    * @param model the active transit model
    * @exception TransitException if an establishment error occurs
    */
    private Transit( TransitModel model ) throws TransitException
    {
        //
        // create the transit context
        //

        try
        {
            m_context = SecuredTransitContext.create( model );
        }
        catch( TransitException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = "Unable to construct transit context.";
            throw new TransitException( error, e );
        }

        //
        // setup the monitors
        //

        m_repositoryMonitor = new RepositoryMonitorRouter();
        m_cacheMonitor = new CacheMonitorRouter();
        m_networkMonitor = new NetworkMonitorRouter();

        try
        {
            // Setting up a temporary directory for Transit.

            File temp = new File( DPML_DATA, "temp" );
            temp.mkdirs();

            // Setting up a permanent output troubleshooting resource
            // for Transit.
            File logs = new File( DPML_DATA, "logs" );
            File logDir = new File( logs, "transit" );
            logDir.mkdirs();
            File logFile = new File( logDir, "transit.log" );
            FileOutputStream fos = new FileOutputStream( logFile );
            OutputStreamWriter osw = new OutputStreamWriter( fos, "UTF-8" );
            m_logWriter = new PrintWriter( osw, true );
        }
        catch( Throwable e )
        {
            final String error = "Unable to construct transit instance.";
            throw new TransitException( error, e );
        }
    }

   /**
    * Return the link manager.
    * @return the link manager
    */
    public LinkManager getLinkManager()
    {
        return getTransitContext().getLinkManager();
    }

   /**
    * Return the Transit repository service.
    * @return the repository service
    * @exception IllegalStateException if Transit has not been initialized
    */
    public Repository getRepository() throws IllegalStateException 
    {
        return getTransitContext().getRepository();
    }

   /**
    * Returns a reference to the repository monitor router.  Client application
    * may use the router to add, remove or replace existing monitors.
    * @return the repository monitor router
    */
    public RepositoryMonitorRouter getRepositoryMonitorRouter()
    {
        return m_repositoryMonitor;
    }

   /**
    * Returns a reference to the cache monitor router.  Client application
    * may use the router to add, remove or replace existing monitors.
    * @return the cache monitor router
    */
    public CacheMonitorRouter getCacheMonitorRouter()
    {
        return m_cacheMonitor;
    }

   /**
    * Returns a reference to the netowork monitor router.  Client application
    * may use the router to add, remove or replace existing monitors.
    * @return the network monitor router
    */
    public NetworkMonitorRouter getNetworkMonitorRouter()
    {
        return m_networkMonitor;
    }

   /** Returns the LogWriter for the Transit system.
    * This writer should only be used to report information that
    * should not be output to the user in the course of normal
    * execution but can aid to determine what has gone wrong in
    * Transit, such as configuration problems, network problems
    * and security issues.
    * @return a PrintWriter where troubleshooting information can
    * be written to.
    */
    public PrintWriter getLogWriter()
    {
        return m_logWriter;
    }

   /**
    * Resolve the DPML home directory using assesment of the the ${dpml.home}
    * system property, the DPML_HOME environment variable.  If DPML_HOME is
    * not declared, the behaviour is platform specific.  If the os is Windows,
    * the value returned is equivalent to $APPDATA\DPML whereas Unix environment
    * will return ${user.home}/.dpml. The value returned may be overriden by 
    * setting a 'dpml.home' system property.
    *
    * @return the DPML home directory
    */
    private static File resolveHomeDirectory()
    {
        String home = System.getProperty( HOME_KEY );
        if( null != home )
        {
            return new File( home );
        }
        home = Environment.getEnvVariable( HOME_SYMBOL );
        if( null != home )
        {
            return new File( home );
        }
        String os = System.getProperty( "os.name" ).toLowerCase();
        if( os.indexOf( "win" ) >= 0 )
        {
            home = Environment.getEnvVariable( "APPDATA" );
            File data = new File( home );
            return new File( data, "DPML" );
        }
        else
        {
            File user = new File( System.getProperty( "user.home" ) );
            return new File( user, ".dpml" );
        }
    }

   /**
    * Resolve the DPML system home directory.  If a system property
    * named "dpml.system" is defined then the value as a file is
    * returned otherwise the implementation will look for an environment
    * variable named "DPML_SYSTEM" which if defined will be
    * returned as a file.  If neither case holds, the value assigned
    * if platform dependent.  If the os is Windows, DPML_HOME\Shared 
    * is returned otherwise $DPML_HOME/share" is returned for a nix 
    * platforms.
    *
    * @param dpmlHomeDir the default DPML_HOME value
    * @return the transit system directory
    */
    private static File resolveSystemDirectory( File dpmlHomeDir )
    {
        String home = System.getProperty( SYSTEM_KEY );
        if( null != home )
        {
            return new File( home );
        }
        home = Environment.getEnvVariable( SYSTEM_SYMBOL );
        if( null != home )
        {
            return new File( home );
        }

        String os = System.getProperty( "os.name" ).toLowerCase();
        if( os.indexOf( "win" ) >= 0 )
        {
            return new File( dpmlHomeDir, "Shared" );
        }
        else
        {
            return new File( dpmlHomeDir, "share" );
        }
    }

   /**
    * Resolve the DPML data directory. If the platform is Windows
    * the returned value is equivalent to ${dpml.home}/Data
    * whereas the Unix variant returns ${dpml.home}/data. The value
    * returned may be overriden by setting a 'dpml.data' system property.
    *
    * @param dir the default DPML_HOME value
    * @return the transit personal data directory
    */
    private static File resolveDataDirectory( File dir )
    {
        String path = System.getProperty( DATA_KEY );
        if( null != path )
        {
            return new File( path );
        }
        String os = System.getProperty( "os.name" ).toLowerCase();
        if( os.indexOf( "win" ) >= 0 )
        {
            return new File( dir, "Data" );
        }
        else
        {
            return new File( dir, "data" );
        }
    }

   /**
    * Resolve the DPML prefs directory. If the platform is Windows
    * the returned value is equivalent to ${dpml.home}/Preferences
    * whereas the Unix variant returns ${dpml.home}/prefs. The value
    * returned may be overriden by setting a 'dpml.prefs' system property.
    *
    * @param dir the default DPML_HOME value
    * @return the transit personal data directory
    */
    private static File resolvePreferencesDirectory( File dir )
    {
        String path = System.getProperty( PREFS_KEY );
        if( null != path )
        {
            return new File( path );
        }
        String os = System.getProperty( "os.name" ).toLowerCase();
        if( os.indexOf( "win" ) >= 0 )
        {
            return new File( dir, "Preferences" );
        }
        else
        {
            return new File( dir, "prefs" );
        }
    }

    //------------------------------------------------------------------
    // static internal 
    //------------------------------------------------------------------

   /**
    * Singleton transit instance.
    */
    private static Transit m_INSTANCE;

}
