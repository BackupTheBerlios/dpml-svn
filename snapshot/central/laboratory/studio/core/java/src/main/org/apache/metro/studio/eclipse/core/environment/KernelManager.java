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

package org.apache.metro.studio.eclipse.core.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Map;
import java.util.Properties;

import org.apache.avalon.repository.Artifact;

import org.apache.avalon.repository.main.DefaultBuilder;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;

import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;

import org.apache.avalon.util.env.Env;

import org.apache.avalon.util.exception.ExceptionHelper;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Merlin command line handler.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team </a>
 * @version $Id: Main.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class KernelManager
{
    //----------------------------------------------------------
    // static
    //----------------------------------------------------------

    private static Resources REZ;

    private static final File USER_HOME;

    private static final String MERLIN_PROPERTIES;

    private static final String IMPLEMENTATION_KEY;

    private static KernelManager MAIN;

    static
    {
        REZ = ResourceManager.getPackageResources( KernelManager.class );
        USER_HOME = new File( System.getProperty("user.home") );
        MERLIN_PROPERTIES = "merlin.properties";
        IMPLEMENTATION_KEY = "merlin.implementation";
    }
    
    /**
     * Main command line enty point.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main( String[] args )
    {
        boolean debug = false;
        try
        {
            ModelObject line = new ModelObject();
            line.put( "impl", "merlin-cli-3.3.0.jar" );
            
            File dir = getWorkingDirectory( line );
            File cache = getMerlinSystemRepository( line );
            Artifact artifact = getDefaultImplementation( dir, line );

            InitialContextFactory factory = 
                new DefaultInitialContextFactory( "merlin", dir );
            factory.setCacheDirectory(cache);
            factory.setOnlineMode(!line.hasOption("offline"));
            InitialContext context = factory.createInitialContext();
            
            KernelManager kernel = new KernelManager( context, artifact, line );
            
        } catch( Exception exception )
        {
            String msg = ExceptionHelper.packException( exception, debug );
            System.err.println(msg);
            System.exit(-1);
        } catch( Throwable throwable )
        {
            String msg = ExceptionHelper.packException( throwable, true );
            System.err.println( msg );
            System.exit( -1 );
        }
    }

    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

    /**
     * Creation of a new kernel cli handler.
     * 
     * @param context
     *            the repository inital context
     * @param artifact
     *            the merlin implementation artifact
     * @param line
     *            the command line construct
     * @exception Exception
     *                if an error occurs
     */
    public KernelManager( InitialContext context, Artifact artifact, ModelObject line) 
        throws Exception
    {
        Builder builder = context.newBuilder( artifact );
        Factory factory = builder.getFactory();
        Map criteria = factory.createDefaultCriteria();

        //
        // update the criteria using the command line information
        //

        handleCommandLine( criteria, line );

    }

    //----------------------------------------------------------
    // implementation
    //----------------------------------------------------------

    private void handleCommandLine( Map criteria, ModelObject line )
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

    private void setLanguage( Map criteria, ModelObject line )
    {
        if( line.hasOption( "lang" ) )
        {
            String language = line.getOptionValue( "lang" );
            criteria.put( "merlin.lang", language );
        }
    }

    private void setKernelURL( Map criteria, ModelObject line )
    {
        if( line.hasOption( "kernel" ) )
        {
            String kernel = line.getOptionValue( "kernel" );
            criteria.put( "merlin.kernel", kernel );
        }
    }

    private void setOverridePath( Map criteria, ModelObject line )
    {
        if( line.hasOption( "config" ) )
        {
            String config = line.getOptionValue( "config" );
            criteria.put( "merlin.override", config );
        }
    }

    private void setWorkingDirectory( Map criteria, ModelObject line )
    {
        if( line.hasOption( "home" ) )
        {
            String home = line.getOptionValue( "home" );
            criteria.put( "merlin.dir", home );
        }
    }

    private void setAnchorDirectory( Map criteria, ModelObject line )
    {
        if( line.hasOption( "anchor" ) )
        {
            String anchor = line.getOptionValue( "anchor" );
            criteria.put( "merlin.anchor", anchor );
        }
    }

    private void setContextDirectory( Map criteria, ModelObject line )
    {
        if( line.hasOption( "context" ) )
        {
            String context = line.getOptionValue( "context" );
            criteria.put( "merlin.context", context );
        }
    }

    private void setRepositoryDirectory( Map criteria, ModelObject line )
    {
        if( line.hasOption( "repository" ) )
        {
            String repository = line.getOptionValue( "repository" );
            criteria.put( "merlin.repository", repository );
        }
    }

    private void setDebugPolicy( Map criteria, ModelObject line )
    {
        if( line.hasOption( "debug" ) )
        {
            criteria.put( "merlin.debug", Boolean.TRUE );
        }
    }

    private void setAuditPolicy( Map criteria, ModelObject line )
    {
        if( line.hasOption( "audit" ) )
        {
            criteria.put( "merlin.audit", Boolean.TRUE );
        }
    }

    private void setProxyPolicy( Map criteria, ModelObject line )
    {
        if( line.hasOption( "noproxy" ) )
        {
            criteria.put( "merlin.proxy", Boolean.FALSE );
        }
    }

    private void setInfoPolicy( Map criteria, ModelObject line )
    {
        if( line.hasOption( "info" ) )
        {
            criteria.put( "merlin.info", Boolean.TRUE );
        }
    }

    private void setServerPolicy( Map criteria, ModelObject line )
    {
        if( line.hasOption( "execute" ) )
        {
            criteria.put( "merlin.server", Boolean.FALSE );
        }
    }

    private void setSecurityPolicy( Map criteria, ModelObject line )
    {
        if( line.hasOption( "secure" ) )
        {
            criteria.put( "merlin.code.security.enabled", Boolean.TRUE );
        }
    }

    private void setDeploymentPath( Map criteria, ModelObject line )
    {
        String[] arguments = line.getArgs();
        if( arguments.length > 0 )
        {
            criteria.put( "merlin.deployment", arguments );
        }
    }

    /**
     * Resolve the merlin.dir value.
     * 
     * @param line
     *            the command line construct
     * @return the working directory
     */
    private static File getWorkingDirectory( ModelObject line ) 
        throws Exception
    {
        if( line.hasOption( "home" ) )
        {
            String dirname = line.getOptionValue( "home" );
            File dir = new File( dirname );
            return dir.getCanonicalFile();
        } else
        {
            return getBaseDirectory();
        }
    }

    /**
     * Resolve the default implementation taking into account command line
     * arguments, local and hom properties, and application defaults.
     * 
     * @param line
     *            the command line construct
     * @return the artifact reference
     */
    private static Artifact getDefaultImplementation( File base, ModelObject line )
        throws Exception
    {
        if( line.hasOption( "impl" ) )
        {
            String spec = line.getOptionValue( "impl" );
            return Artifact.createArtifact( spec );
        } else
        {
            return DefaultBuilder.createImplementationArtifact(
                KernelManager.class.getClassLoader(), 
                getMerlinHome(),
                getBaseDirectory(), 
                MERLIN_PROPERTIES, 
                IMPLEMENTATION_KEY
            );
        }
    }

    /**
     * Return the merlin system repository root directory taking into account
     * the supplied command-line, and merlin.properties files in the current and
     * home directories.
     * 
     * @param line
     *            the command line construct
     * @return the merlin system root repository directory
     */
    private static File getMerlinSystemRepository( ModelObject line )
    {
        if( line.hasOption( "system" ) )
        {
            String system = line.getOptionValue( "system" );
            return new File( system );
        } else
        {
            return new File( getMerlinHome(), "system" );
        }
    }

    /**
     * Return the merlin home directory.
     * 
     * @return the merlin installation directory
     */
    private static File getMerlinHome()
    {
        return new File( getMerlinHomePath() );
    }

    /**
     * Return the merlin home directory path.
     * 
     * @return the merlin installation directory path
     */
    private static String getMerlinHomePath()
    {
        try
        {
            String merlinHome = Env.getEnvVariable( "MERLIN_HOME" );
            String merlin = System.getProperty( "merlin.home", merlinHome );
            if( null != merlin )
                return merlin;
            return System.getProperty( "user.home" ) + File.separator + ".merlin";
        } catch (Throwable e)
        {
            final String error = "Internal error while attempting to access MERLIN_HOME environment.";
            final String message = ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }

    /**
     * Return the functional base directory. The implementation looks for the
     * ${merlin.dir} system property and if not found, looks for the ${basedir}
     * system property, and as a last resort, returns the JVM ${user.dir} value.
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
     * 
     * @param dir
     *            the directory
     * @param filename
     *            the filename
     * @return a possibly empty properties instance
     */
    private static Properties getLocalProperties( File dir, String filename )
    {
        Properties properties = new Properties();
        if( null == dir || null == filename)
            return properties;
        File file = new File( dir, filename );
        if( ! file.exists() )
            return properties;
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );
            properties.load( fis );
            return properties;
        } catch (Throwable e)
        {
            final String error = 
                "Unexpected exception while attempting to read properties from: "
                + file + ". Cause: " + e.toString();
            throw new IllegalStateException( error );
        } finally
        {
            if( fis != null )
            {
                try
                {
                    fis.close();
                } catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
