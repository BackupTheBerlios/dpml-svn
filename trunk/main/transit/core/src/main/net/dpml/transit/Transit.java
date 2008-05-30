/*
 * Copyright 2004-2007 Stephen McConnell.
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

import dpml.util.DefaultLogger;

import dpml.transit.TransitContext;
import dpml.transit.info.TransitDirective;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;

import net.dpml.util.Logger;

/**
 * The Transit class manages the establishment of a singleton transit instance.
 * The implementation establishes an internal cache management system, a suite
 * of protocol handlers, and a dynamic content handler service.
 * 
 * During initialization Transit will load an XML configuration descibing the 
 * available remote hosts.  The XML file will be resolved using the following 
 * strategy:
 *
 * <ul>
 *  <li>if the system property <tt>dpml.transit.profile</tt> is defined then
 *     <ul>
 *       <li>if the value contains the ':' character the value will be 
 *           treated as a URL referencing a remote configuration</li>
 *       <li>otherwise the value will be treated as a relative file path
 *           that is first evaluated relative to the current working directory 
 *           and a file exists at that location it will be loaded, otherwise, 
 *           the path will be evaluated relative to the DPML Preferences root
 *           directory</li>
 *     </ul>
 *   </li>
 *   <li>otherwise, the default configuration path 
 *     <tt>dpml/transit/xmls/standard.xml</tt> will be resolved relative to the 
 *     Preferences root directory</li>
 *   <li>if no default configuration is found, Transit will assign a standard
 *     profile</li>
 * </ul>
 * 
 * During initialization Transit will create the following system properties:
 *
 * <ul>
 *  <li><tt>dpml.home</tt> home directory</li>
 *  <li><tt>dpml.data</tt> data directory</li>
 *  <li><tt>dpml.prefs</tt> preferences repository root directory</li>
 *  <li><tt>dpml.share</tt> shared system root directory</li>
 *  <li><tt>dpml.transit.version</tt> Transit system version</li>
 * </ul>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Transit
{
    //------------------------------------------------------------------
    // static 
    //------------------------------------------------------------------

   /**
    * DPML home key.
    */
    private static final String HOME_KEY = "dpml.home";

   /**
    * DPML data key.
    */
    private static final String DATA_KEY = "dpml.data";

   /**
    * DPML prefs key.
    */
    private static final String PREFS_KEY = "dpml.prefs";

   /**
    * Transit system key.
    */
    private static final String SYSTEM_KEY = "dpml.system";

   /**
    * Transit share key (alias to dpml.system).
    */
    private static final String SHARE_KEY = "dpml.share";

   /**
    * Transit config key.
    */
    private static final String CONFIG_KEY = "dpml.config";

   /**
    * DPML environment variable string.
    */
    private static final String HOME_SYMBOL = "DPML_HOME";

   /**
    * DPML environment variable string.
    */
    private static final String SYSTEM_SYMBOL = "DPML_SYSTEM";

   /**
    * The DPML home directory established via assesment of the the ${dpml.home}
    * system property and the <tt>DPML_HOME</tt> environment variable.
    */
    public static final File HOME;

   /**
    * If a system property named "dpml.system" is defined then the value
    * is assigned otherwise the implementation will look for an environment
    * variable <tt>DPML_SYSTEM</tt>.
    */
    public static final File SYSTEM;

   /**
    * The Transit personal data directory. The location of this diectory is system
    * dependent.
    */
    public static final File DATA;

   /**
    * The Transit personal preferences directory. The location of this diectory is system
    * dependent.
    */
    public static final File PREFS;

   /**
    * The Transit shared config directory. The location of this diectory is system
    * dependent.
    */
    public static final File CONFIG;

   /**
    * The Transit system version.
    */
    public static final String VERSION = "@PROJECT-VERSION@";

   /**
    * Default configuration path.
    */
    private static final String STANDARD_PATH = "dpml/transit/xmls/standard.xml";

   /**
    * System property key used to hold an overriding configuration url.
    */
    private static final String PROFILE_KEY = "dpml.transit.profile";

    private static final Logger LOGGER = new DefaultLogger( "dpml.transit" );

    static
    {
        String pkgs = System.getProperty( "java.protocol.handler.pkgs" );
        if( null == pkgs )
        {
            System.setProperty( "java.protocol.handler.pkgs", "dpml.transit|net.dpml.transit" );
        }
        else
        {
            System.setProperty( "java.protocol.handler.pkgs", pkgs + "|dpml.transit|net.dpml.transit" );
        }
        
        System.setProperty( "dpml.transit.version", VERSION );
        
        HOME = resolveHomeDirectory();
        SYSTEM = resolveSystemDirectory( HOME );
        DATA = resolveDataDirectory( HOME );
        PREFS = resolvePreferencesDirectory( HOME );
        CONFIG = resolveConfigDirectory( SYSTEM );

        System.setProperty( SYSTEM_KEY, SYSTEM.getAbsolutePath() );
        System.setProperty( SHARE_KEY, SYSTEM.getAbsolutePath() );
        System.setProperty( HOME_KEY, HOME.getAbsolutePath() );
        System.setProperty( DATA_KEY, DATA.getAbsolutePath() );
        System.setProperty( PREFS_KEY, PREFS.getAbsolutePath() );
        System.setProperty( CONFIG_KEY, CONFIG.getAbsolutePath() );
    }
    
   /**
    * Returns the singleton instance of the transit system. If Transit
    * has not been initialized the transit configuration will be resolved 
    * using the System property <tt>dpml.transit.profile</tt>.
    * @return the singleton transit instance
    * @exception TransitError if an error occurs during establishment
    */
    public static synchronized Transit getInstance() throws TransitError
    {
        if( null == m_INSTANCE )
        {
            if( LOGGER.isTraceEnabled() )
            {
                LOGGER.trace( "version " + VERSION );
                LOGGER.trace( "codebase: " 
                  + Transit.class.getProtectionDomain().getCodeSource().getLocation()
                );
            }
            try
            {
                TransitDirective directive = loadTransitDirective();
                m_INSTANCE = new Transit( directive );
                return m_INSTANCE;
            }
            catch( Throwable e )
            {
                final String error = 
                  "Transit initialization failure.";
                throw new TransitError( error, e );
            }
        }
        else
        {
            return m_INSTANCE;
        }
    }
    
    //------------------------------------------------------------------
    // state 
    //------------------------------------------------------------------

   /**
    * Internal transit context.
    */
    private TransitContext m_context;

    //------------------------------------------------------------------
    // constructor 
    //------------------------------------------------------------------

   /**
    * Private constructor of a transit instance.
    *
    * @param directive the transit configuration
    * @exception TransitException if an establishment error occurs
    */
    private Transit( TransitDirective directive ) throws TransitException
    {
        try
        {
            m_context = TransitContext.create( directive );
        }
        //catch( TransitException e )
        //{
        //    throw e;
        //}
        catch( Throwable e )
        {
            final String error = "Internal error while attempting to create the Transit context.";
            throw new TransitException( error, e );
        }
    }
    
    //------------------------------------------------------------------
    // implementation 
    //------------------------------------------------------------------

   /**
    * Return the singleton transit content.
    *
    * @return the context instance
    * @exception IllegalStateException if transit has not been initialized
    */
    TransitContext getTransitContext() throws IllegalStateException
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
    * Return the current cache directory.
    * @return the cache directory.
    */
    public File getCacheDirectory()
    {
        return getTransitContext().getCacheHandler().getCacheDirectory();
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
    * Return the cache layout.
    * @return the layout
    */
    public Layout getCacheLayout()
    {
        return getTransitContext().getCacheLayout();
    }
    
   /**
    * Add a monitor to Transit.
    * @param monitor the monitor to add
    */
    public void addMonitor( Monitor monitor )
    {
        getTransitContext().addMonitor( monitor );
    }
    
   /**
    * Return the content handler fo the supplied content type.
    * @param type the content handler type
    * @return the content handler or null if no content handler found
    */
    public ContentHandler getContentHandler( String type )
    {
        return getTransitContext().getContentHandler( type );
    }
    
   /**
    * Resolve the DPML home directory using assesment of the the ${dpml.home}
    * system property, the HOME environment variable.  If HOME is
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
        home = System.getenv( HOME_SYMBOL );
        if( null != home )
        {
            return new File( home );
        }
        String os = System.getProperty( "os.name" ).toLowerCase();
        if( os.indexOf( "win" ) >= 0 )
        {
            home = System.getenv( "APPDATA" );
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
    * variable named "SYSTEM" which if defined will be
    * returned as a file otherwise a value equivalent to 
    * <tt>${dpml.home}/share</tt> will be returned.
    *
    * @param dpmlHomeDir the default HOME value
    * @return the transit system directory
    */
    private static File resolveSystemDirectory( File dpmlHomeDir )
    {
        String home = System.getProperty( SYSTEM_KEY );
        if( null != home )
        {
            return new File( home );
        }
        home = System.getenv( SYSTEM_SYMBOL );
        if( null != home )
        {
            return new File( home );
        }
        String os = System.getProperty( "os.name" ).toLowerCase();
        if( os.indexOf( "win" ) >= 0 )
        {
            return new File( dpmlHomeDir, "share" );
        }
        else
        {
            // return new File( dpmlHomeDir, "share" ); /* bug in 2.1.1 */
            return new File( "/opt/dpml/share" );
        }
    }

   /**
    * Resolve the DPML data directory. The value
    * returned may be overriden by setting a 'dpml.data' 
    * system property otherwise the default value returned
    * will be equivalent to <tt>${dpml.home}/data</tt>.
    *
    * @param dir the default HOME value
    * @return the transit personal data directory
    */
    private static File resolveDataDirectory( File dir )
    {
        String path = System.getProperty( DATA_KEY );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            return new File( dir, "data" );
        }
    }

   /**
    * Resolve the DPML prefs directory. The value
    * returned may be overriden by setting a 'dpml.prefs' 
    * system property otherwise the default value returned
    * will be equivalent to <tt>${dpml.home}/prefs</tt>.
    *
    * @param dir the default HOME value
    * @return the transit personal data directory
    */
    private static File resolvePreferencesDirectory( File dir )
    {
        String path = System.getProperty( PREFS_KEY );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            return new File( dir, "prefs" );
        }
    }

   /**
    * Resolve the DPML config directory. The value
    * returned may be overriden by setting a 'dpml.config' 
    * system property otherwise the default value returned
    * will be equivalent to <tt>${dpml.system}/config</tt>.
    *
    * @param dir the default system directory
    * @return the transit shared configuration directory
    */
    private static File resolveConfigDirectory( File dir )
    {
        String path = System.getProperty( CONFIG_KEY );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            return new File( dir, "config" );
        }
    }

    //------------------------------------------------------------------
    // static internal 
    //------------------------------------------------------------------

   /**
    * Singleton transit instance.
    */
    private static Transit m_INSTANCE;

   /**
    * Resolve the transit configuration using the default resource path 
    * <tt>configuration:xml:dpml/transit/config</tt>. If the resource does not exist a classic 
    * default scenario will be returned.
    *
    * @return the transit configuration directive
    * @exception Exception if an error occurs during model construction
    */
    private static TransitDirective loadTransitDirective() throws Exception
    {
        String path = System.getProperty( PROFILE_KEY );
        if( null != path )
        {
            URL url = resolveURL( path );
            return loadTransitDirective( url );
        }
        else
        {
            File config = Transit.CONFIG;
            File configuration = new File( config, STANDARD_PATH );
            if( configuration.exists() )
            {
                URI uri = configuration.toURI();
                URL url = uri.toURL();
                return loadTransitDirective( url );
            }
            else
            {
                return TransitDirective.CLASSIC_PROFILE;
            }
        }
    }
    
    private static TransitDirective loadTransitDirective( URL url ) throws Exception
    {
        if( LOGGER.isTraceEnabled() )
        {
            LOGGER.trace( 
              "configuration [" 
              + url
              + "]" );
        }
        return TransitDirective.decode( url );
    }
    
    private static URL resolveURL( String path ) throws Exception
    {
        if( path.indexOf( ":" ) > -1 )
        {
            // its a url
            URI uri = new URI( path );
            return Artifact.toURL( uri );
        }
        else
        {
            // its a file path
            File file = new File( path );
            if( file.exists() )
            {
                return file.toURI().toURL();
            }
            else
            {
                File config = Transit.CONFIG;
                File alt = new File( config, path );
                if( alt.exists() )
                {
                    return alt.toURI().toURL();
                }
                else
                {
                    throw new FileNotFoundException( path ); 
                }
            }
        }
    }
}
