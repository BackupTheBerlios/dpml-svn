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

package org.apache.avalon.repository.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Locale;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.net.URL;

import javax.naming.directory.Attributes;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.RepositoryException;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;
import org.apache.avalon.repository.main.DefaultBuilder;
import org.apache.avalon.repository.meta.ArtifactDescriptor;
import org.apache.avalon.repository.util.RepositoryUtils;

import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

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

    private static final String AVALON_PROPERTIES = "avalon.properties";

    private static final String IMPLEMENTATION_KEY = "avalon.repository.implementation";

    private static Options CL_OPTIONS = buildCommandLineOptions();

    private static Options buildCommandLineOptions()
    {
        Options options = new Options();

        Option help = new Option(
           "help",
           REZ.getString( "cli-help-description" ) );

        Option verify = new Option(
           "verify",
           REZ.getString( "cli-verify-description" ) );

        Option version = new Option(
           "version",
           REZ.getString( "cli-version-description" ) );

        Option locale = OptionBuilder
           .hasArg()
           .withArgName( "code" )
           .withDescription( REZ.getString( "cli-language-description" )  )
           .create( "lang" );

        Option info = new Option(
           "info",
           REZ.getString( "cli-info-description" ) );

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

        Option cache = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "directory" ) )
           .withDescription( REZ.getString( "cli-cache-description" ) )
           .create( "cache" );

        Option hosts = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "urls" ) )
           .withDescription( REZ.getString( "cli-hosts-description" ) )
           .create( "hosts" );

        Option install = OptionBuilder
           .hasArg()
           .withArgName( REZ.getString( "url" ) )
           .withDescription( REZ.getString( "cli-install-description" ) )
           .create( "install" );

        options.addOption( help );
        options.addOption( locale );
        options.addOption( version );
        options.addOption( info );
        options.addOption( implementation );
        options.addOption( install );
        options.addOption( home );
        options.addOption( cache );
        options.addOption( hosts );
        options.addOption( verify );
        return options;
    }

    private static Main MAIN = null;

   /**
    * Main command line enty point.
    * @param args the command line arguments
    */
    public static void main( String[] args )
    {
        try
        {
            //
            // parse the commandline
            //

            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse( CL_OPTIONS, args );

            File dir = getWorkingDirectory( line );
            File cache = getCacheDirectory( line );
            Artifact artifact = getDefaultImplementation( dir, line );

            if( line.hasOption( "version" ) )
            {
                Main.printVersionInfo( cache, artifact );
                return;     
            }
            else if( line.hasOption( "help" ) )
            {
                doHelp( line );
                return;
            }
            else
            {
                //
                // setup the initial context
                //

                ClassLoader parent = Main.class.getClassLoader();
                String[] hosts = getHostsPath( line );
                
                DefaultInitialContextFactory factory = 
                  new DefaultInitialContextFactory( "avalon", dir );
                factory.setCacheDirectory( cache );
                factory.setHosts( hosts );
                
                InitialContext context = factory.createInitialContext();

                //
                // process the commandline and do the real work
                //

                MAIN = new Main( context, line );
            }
        }
        catch( Throwable e )
        {
            String msg = 
              ExceptionHelper.packException( e, true );
            System.err.println( msg );
            System.exit( -1 );
        }
    }

    private static void doHelp( CommandLine line )
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
          "repository [artifact]", 
          " ", 
          buildCommandLineOptions(), 
          "", 
          true );
    }

    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

   /**
    * Creation of a new kernel cli handler.
    * @param context the repository inital context
    * @param line the command line construct
    * @exception Exception if an error occurs
    */
    public Main( 
      InitialContext context, CommandLine line ) throws Exception
    {
        if( line.hasOption( "info" ) )
        {
            StringBuffer buffer = 
              new StringBuffer( InitialContext.LINE );
            buffer.append( "\nAvalon Repository" );
            buffer.append( InitialContext.LINE );
            prepareInfoListing( buffer, context );
            buffer.append( InitialContext.LINE );
            System.out.println( buffer.toString() );
        }

        if( line.hasOption( "install" ) )
        {
            doInstall( context, line );
        }
        else if( line.hasOption( "verify" ) )
        {
            doVerify( context );
        }
        else
        {
            doHelp( line );
        }
    }

    private void prepareInfoListing( StringBuffer buffer, InitialContext context )
    {
        buffer.append( "\n${avalon.repository.cache} = " );
        buffer.append( context.getInitialCacheDirectory() );
        buffer.append( "\n${avalon.dir} = " );
        buffer.append( context.getInitialWorkingDirectory() );
        String[] hosts = context.getInitialHosts();
        buffer.append( "\n${avalon.repository.hosts} = (" );
        buffer.append( hosts.length );
        buffer.append( ")" );
        for( int i=0; i<hosts.length; i++ )
        {
            buffer.append( "\n  " + hosts[i] );
        }
    }

    private void doInstall( InitialContext context, CommandLine line ) 
      throws Exception
    {
        final URL url = getInstallTarget( line );
        try
        {
            context.install( url );
        }
        catch( Throwable e )
        {
            final String error = 
              "Install failure: " 
              + url;
            throw new RepositoryException( error, e );
        }
    }

    private void doVerify( InitialContext context ) 
      throws Exception
    {
        RepositoryVerifier verifier = 
          new RepositoryVerifier( context );
        verifier.verify();
    }


    //----------------------------------------------------------
    // implementation
    //----------------------------------------------------------

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

    private static String[] getHostsPath( CommandLine line )
    {
        if( line.hasOption( "hosts" ) )
        {
            String hosts = line.getOptionValue( "hosts" );
            return expandHosts( hosts );
        }
        else
        {
            return null;
        }
    }

    private static String[] expandHosts( String arg )
    {
        ArrayList list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer( arg, "," );
        while( tokenizer.hasMoreTokens() )
        {
            String next = tokenizer.nextToken();
            list.add( next );
        }
        return (String[]) list.toArray( new String[0] );
    }

   /**
    * Return the url from the install parameter.
    * @param line the command line construct
    * @return the install target url
    */
    private URL getInstallTarget( CommandLine line ) throws IOException
    {
        String spec = line.getOptionValue( "install" );
        return new URL( spec );
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
                getAvalonHome(),
                getBaseDirectory(), 
                AVALON_PROPERTIES, 
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
    * Return the avalon repository cache directory taking into 
    * account the supplied command-line, and avalon.properties files in 
    * the current and home directories.
    *
    * @param line the command line construct
    * @return the merlin system root repository directory
    */
    private static File getCacheDirectory( CommandLine line )
    {
        if( line.hasOption( "cache" ) )
        {
            String system = line.getOptionValue( "cache" );
            return new File( system );
        }
        else
        {
            return new File( getAvalonHome( ), "repository" );
        }
    }

   /**
    * Return the avalon home directory.
    * @return the avalon home directory
    */
    private static File getAvalonHome()
    {
        return new File( getAvalonHomePath() );
    }

   /**
    * Return the installation directory path.
    * @return the merlin install directory path
    */
    private static String getAvalonHomePath()
    {
        return getHomePath( "AVALON_HOME", ".avalon" ); 
    }

   /**
    * Return the merlin home directory path.
    * @return the merlin install directory path
    */
    private static String getHomePath( final String var, final String dir )
    {
        try
        {
            String avalon = 
              System.getProperty( 
                "avalon.home", 
                Env.getEnvVariable( var ) );
            if( null != avalon ) return avalon;
            return System.getProperty( "user.home" ) 
              + File.separator + dir;
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to access AVALON_HOME environment.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }

   /**
    * Return the functional base directory.  The implementation looks
    * for the ${avalon.dir} system property and if not found, looks for 
    * the ${basedir} system property, and as a last resort, returns the 
    * JVM ${user.dir} value.
    *
    * @return the merlin install directory
    */
    private static File getBaseDirectory()
    {
        final String path = System.getProperty( "avalon.dir" );
        if( null != path )
        {
            return new File( path );
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
