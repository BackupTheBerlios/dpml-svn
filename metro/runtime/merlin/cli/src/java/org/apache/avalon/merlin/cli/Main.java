/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.merlin.cli;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Locale;

import javax.naming.directory.Attributes;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;
import org.apache.avalon.repository.main.DefaultBuilder;
import org.apache.avalon.repository.meta.ArtifactDescriptor;
import org.apache.avalon.repository.util.RepositoryUtils;
import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


/**
 * Merlin command line handler.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Main.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Main 
{
    //----------------------------------------------------------
    // static
    //----------------------------------------------------------

    private static Resources REZ =
        ResourceManager.getPackageResources( Main.class );

    private static final File USER_HOME = 
      new File( System.getProperty( "user.home" ) );

    private static final String MERLIN_PROPERTIES = "merlin.properties";

    private static final String IMPLEMENTATION_KEY = "merlin.implementation";

    private static Options CL_OPTIONS = buildCommandLineOptions();

    private static Options buildCommandLineOptions()
    {
        Options options = new Options();

        Option help = new Option(
           "help",
           REZ.getString( "cli-help-description" ) );

        Option offline = new Option(
           "offline",
           REZ.getString( "cli-offline-description" ) );

        Option version = new Option(
           "version",
           REZ.getString( "cli-version-description" ) );

        Option execute = new Option(
           "execute",
           REZ.getString( "cli-execute-description" ) );

        Option debug = new Option(
           "debug",
           REZ.getString( "cli-debug-description" ) );

        Option noproxy = new Option(
           "noproxy",
           REZ.getString( "cli-noproxy-description" ) );

        Option audit = new Option(
           "audit",
           REZ.getString( "cli-audit-description" ) );

        Option info = new Option(
           "info",
           REZ.getString( "cli-info-description" ) );

        Option secure = new Option(
           "secure",
           REZ.getString( "cli-secure-description" ) );

        Option locale = OptionBuilder
           .hasArg()
           .withArgName( "code" )
           .withDescription( REZ.getString( "cli-language-description" )  )
           .create( "lang" );

        Option implementation = OptionBuilder
           .hasArg()
           .withArgName( "artifact" )
           .withDescription( REZ.getString( "cli-implementation-description" )  )
           .create( "impl" );

        Option home = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "directory" ) )
           .withDescription( REZ.getString( "cli-home-description" ) )
           .create( "home" );

        Option context = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "directory" ) )
           .withDescription( REZ.getString( "cli-context-description" ) )
           .create( "context" );

        Option system = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "directory" ) )
           .withDescription( REZ.getString( "cli-system-description" ) )
           .create( "system" );

        Option repository = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "directory" ) )
           .withDescription( REZ.getString( "cli-repository-description" ) )
           .create( "repository" );

        Option anchor = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "directory" ) )
           .withDescription( REZ.getString( "cli-anchor-description" ) )
           .create( "anchor" );

        Option config = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "url" ) )
           .withDescription( REZ.getString( "cli-config-description" ) )
           .create( "config" );

        Option kernel = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "url" ) )
           .withDescription( REZ.getString( "cli-kernel-description" ) )
           .create( "kernel" );

        Option install = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "url" ) )
           .withDescription( REZ.getString( "cli-install-description" ) )
           .create( "install" );

        options.addOption( help );
        options.addOption( locale );
        options.addOption( offline );
        options.addOption( execute );
        options.addOption( version );
        options.addOption( info );
        options.addOption( debug );
        options.addOption( audit );
        options.addOption( noproxy );
        options.addOption( secure );
        options.addOption( install );
        options.addOption( home );
        options.addOption( context );
        options.addOption( system );
        options.addOption( repository );
        options.addOption( anchor );
        options.addOption( config );
        options.addOption( kernel );
        options.addOption( implementation );
        return options;
    }

    private static Main MAIN = null;

   /**
    * Main command line enty point.
    * @param args the command line arguments
    */
    public static void main( String[] args )
    {
        boolean debug = false;
        try
        {
            //
            // parse the commandline
            //

            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse( CL_OPTIONS, args );

            File dir = getWorkingDirectory( line );
            File cache = getMerlinSystemRepository( line );
            Artifact artifact = getDefaultImplementation( dir, line );

            debug = line.hasOption( "debug" );

            if( line.hasOption( "version" ) )
            {
                Main.printVersionInfo( cache, artifact );
                return;     
            }
            else if( line.hasOption( "help" ) )
            {
                if( line.hasOption( "lang" ) )
                {
                    ResourceManager.clearResourceCache();
                    String language = line.getOptionValue( "lang" );
                    Locale locale = new Locale( language, "" );
                    Locale.setDefault( locale );
                    REZ = ResourceManager.getPackageResources( Main.class );
                }

                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( 
                  "merlin [block]", 
                  " ", 
                  buildCommandLineOptions(), 
                  "", 
                  true );
                return;
            }
            else
            {
                //
                // setup the initial context
                //

                InitialContextFactory factory = 
                  new DefaultInitialContextFactory( "merlin", dir );
                factory.setCacheDirectory( cache );
                factory.setOnlineMode( !line.hasOption( "offline" ) );
                InitialContext context = factory.createInitialContext();

                //
                // process the commandline and do the real work
                //

                MAIN = new Main( context, artifact, line );
            }
        }
        catch( Exception exception )
        {
            String msg = 
              ExceptionHelper.packException( exception, debug );
            System.err.println( msg );
            System.exit( -1 );
        }
        catch( Throwable throwable )
        {
            String msg = 
              ExceptionHelper.packException( throwable, true );
            System.err.println( msg );
            System.exit( -1 );
        }
    }

    //----------------------------------------------------------
    // immutable state
    //----------------------------------------------------------

    private final Object m_kernel;

    private boolean m_debug;

    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

   /**
    * Creation of a new kernel cli handler.
    * @param context the repository inital context
    * @param artifact the merlin implementation artifact
    * @param line the command line construct
    * @exception Exception if an error occurs
    */
    public Main( 
      InitialContext context, Artifact artifact, CommandLine line ) throws Exception
    {
        Builder builder = context.newBuilder( artifact );
        Factory factory = builder.getFactory();
        Map criteria = factory.createDefaultCriteria();

        //
        // update the criteria using the command line information
        //

        handleCommandLine( criteria, line );

        //
        // instantiate the kernel
        //

        m_kernel = factory.create( criteria );
    }

    //----------------------------------------------------------
    // implementation
    //----------------------------------------------------------

    private void handleCommandLine( Map criteria, CommandLine line )
    {
        setLanguage( criteria, line );
        setInfoPolicy( criteria, line );
        setDebugPolicy( criteria, line );
        setAuditPolicy( criteria, line );
        setProxyPolicy( criteria, line );
        setServerPolicy( criteria, line );
        setSecurityPolicy( criteria, line );
        setAnchorDirectory( criteria, line );
        setContextDirectory( criteria, line );
        setRepositoryDirectory( criteria, line );
        setKernelURL( criteria, line );
        setOverridePath( criteria, line );
        setDeploymentPath( criteria, line );
    }

    private void setLanguage( Map criteria, CommandLine line )
    {
        if( line.hasOption( "lang" ) )
        {
            String language = line.getOptionValue( "lang" );
            criteria.put( "merlin.lang", language );
        }
    }

    private void setKernelURL( Map criteria, CommandLine line )
    {
        if( line.hasOption( "kernel" ) )
        {
            String kernel = line.getOptionValue( "kernel" );
            criteria.put( "merlin.kernel", kernel );
        }
    }

    private void setOverridePath( Map criteria, CommandLine line )
    {
        if( line.hasOption( "config" ) )
        {
            String config = line.getOptionValue( "config" );
            criteria.put( "merlin.override", config );
        }
    }

    private void setWorkingDirectory( Map criteria, CommandLine line )
    {
        if( line.hasOption( "home" ) )
        {
            String home = line.getOptionValue( "home" );
            criteria.put( "merlin.dir", home );
        }
    }

    private void setAnchorDirectory( Map criteria, CommandLine line )
    {
        if( line.hasOption( "anchor" ) )
        {
            String anchor = line.getOptionValue( "anchor" );
            criteria.put( "merlin.anchor", anchor );
        }
    }

    private void setContextDirectory( Map criteria, CommandLine line )
    {
        if( line.hasOption( "context" ) )
        {
            String context = line.getOptionValue( "context" );
            criteria.put( "merlin.context", context );
        }
    }

    private void setRepositoryDirectory( Map criteria, CommandLine line )
    {
        if( line.hasOption( "repository" ) )
        {
            String repository = line.getOptionValue( "repository" );
            criteria.put( "merlin.repository", repository );
        }
    }

    private void setDebugPolicy( Map criteria, CommandLine line )
    {
        if( line.hasOption( "debug" ) )
        {
            criteria.put( "merlin.debug", new Boolean( true ) );
        }
    }

    private void setAuditPolicy( Map criteria, CommandLine line )
    {
        if( line.hasOption( "audit" ) )
        {
            criteria.put( "merlin.audit", new Boolean( true ) );
        }
    }

    private void setProxyPolicy( Map criteria, CommandLine line )
    {
        if( line.hasOption( "noproxy" ) )
        {
            criteria.put( "merlin.proxy", new Boolean( false ) );
        }
    }

    private void setInfoPolicy( Map criteria, CommandLine line )
    {
        if( line.hasOption( "info" ) )
        {
            criteria.put( "merlin.info", new Boolean( true ) );
        }
    }

    private void setServerPolicy( Map criteria, CommandLine line )
    {
        if( line.hasOption( "execute" ) )
        {
            criteria.put( "merlin.server", new Boolean( false ) );
        }
    }

    private void setSecurityPolicy( Map criteria, CommandLine line )
    {
        if( line.hasOption( "secure" ) )
        {
            criteria.put( "merlin.code.security.enabled", new Boolean( true ) );
        }
    }

    private void setDeploymentPath( Map criteria, CommandLine line )
    {
        String[] arguments = line.getArgs();
        if( arguments.length > 0 )
        {
            criteria.put( "merlin.deployment", arguments );
        }
    }

   /**
    * Resolve the merlin.dir value.
    * @param line the command line construct
    * @return the working directory
    */
    private static File getWorkingDirectory( CommandLine line ) throws Exception
    {
        if( line.hasOption( "home" ) )
        {
            String dir = line.getOptionValue( "home" );
            return new File( dir ).getCanonicalFile();
        }
        else
        {
            return getBaseDirectory();
        }
    }

   /**
    * Resolve the default implementation taking into account 
    * command line arguments, local and hom properties, and 
    * application defaults.
    * @param line the command line construct
    * @return the artifact reference
    */
    private static Artifact getDefaultImplementation( 
      File base, CommandLine line ) throws Exception
    {
        if( line.hasOption( "impl" ) )
        {
            String spec = line.getOptionValue( "impl" );
            return Artifact.createArtifact( spec );
        }
        else
        {
            return DefaultBuilder.createImplementationArtifact( 
                Main.class.getClassLoader(), 
                getMerlinHome(),
                getBaseDirectory(), 
                MERLIN_PROPERTIES, 
                IMPLEMENTATION_KEY );
        }
    }

   /**
    * Print out version information to System.out.  This function is 
    * invoked in response to the inclusion of the -version switch on
    * the command line.
    *
    * @param cache the local system cache
    * @param artifact the merlin implementation artifact descriptor
    */
    private static void printVersionInfo( File cache, Artifact artifact )
    {
        try
        {
            Attributes attr = RepositoryUtils.getAttributes( cache, artifact );
            ArtifactDescriptor desc = new ArtifactDescriptor( attr );
            System.out.println( "\n  Implementation: " 
              + artifact.getGroup() 
              + ":" + artifact.getName() 
              + ";" + artifact.getVersion() 
              + " (" + desc.getBuild() + ")"
            );
        }
        catch( Throwable e )
        {
            System.out.println( "\nImplementation: " 
              + artifact.getGroup() 
              + ":" + artifact.getName() 
              + ";" + artifact.getVersion() );
        }
    }

   /**
    * Return the merlin system repository root directory taking into 
    * account the supplied command-line, and merlin.properties files in 
    * the current and home directories.
    *
    * @param line the command line construct
    * @return the merlin system root repository directory
    */
    private static File getMerlinSystemRepository( CommandLine line )
    {
        if( line.hasOption( "system" ) )
        {
            String system = line.getOptionValue( "system" );
            return new File( system );
        }
        else
        {
            return new File( getMerlinHome( ), "system" );
        }
    }

   /**
    * Return the merlin home directory.
    * @return the merlin installation directory
    */
    private static File getMerlinHome()
    {
        return new File( getMerlinHomePath() );
    }

   /**
    * Return the merlin home directory path.
    * @return the merlin installation directory path
    */
    private static String getMerlinHomePath()
    {
        try
        {
            String merlin = 
              System.getProperty( 
                "merlin.home", 
                Env.getEnvVariable( "MERLIN_HOME" ) );
            if( null != merlin ) return merlin;
            return System.getProperty( "user.home" ) 
              + File.separator + ".merlin";
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to access MERLIN_HOME environment.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }

   /**
    * Return the functional base directory.  The implementation looks
    * for the ${merlin.dir} system property and if not found, looks for 
    * the ${basedir} system property, and as a last resort, returns the 
    * JVM ${user.dir} value.
    *
    * @return the base directory
    */
    private static File getBaseDirectory()
    {
        final String merlin = System.getProperty( "merlin.dir" );
        if( null != merlin )
        {
            return new File( merlin );
        }
        final String base = System.getProperty( "basedir" );
        if( null != base )
        {
            return new File( base );
        }
        return new File( System.getProperty( "user.dir" ) );
    }

   /**
    * Return a property file from a fir with a supplied filename.
    * @param dir the directory
    * @param filename the filename
    * @return a possibly empty properties instance
    */
    private static Properties getLocalProperties( 
      File dir, String filename ) 
    {
        Properties properties = new Properties();
        if( null == dir ) return properties;
        File file = new File( dir, filename );
        if( !file.exists() ) return properties;
        try
        {
            properties.load( new FileInputStream( file ) );
            return properties;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected exception while attempting to read properties from: " 
              + file + ". Cause: " + e.toString();
            throw new IllegalStateException( error );
        }
    }
}
