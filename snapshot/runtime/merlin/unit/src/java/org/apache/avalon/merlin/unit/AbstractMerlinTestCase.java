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

package org.apache.avalon.merlin.unit;

import java.io.File;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;
import org.apache.avalon.repository.main.DefaultBuilder;

import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;

/**
 * Test case that usages the repository builder to deploy the 
 * Merlin default application factory.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractMerlinTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class AbstractMerlinTestCase extends TestCase
{
    //----------------------------------------------------------
    // static
    //----------------------------------------------------------

    private static final String MERLIN_PROPERTIES = "merlin.properties";

    private static final String IMPLEMENTATION_KEY = "merlin.implementation";

    private static final String DEPLOYMENT_MODEL_CLASSNAME = 
      "org.apache.avalon.composition.model.DeploymentModel";

    public static File getWorkDirectory()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            return getBaseDirectory();
        }
    }

    public static File getBaseDirectory()
    {
        String path = System.getProperty( "basedir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            return new File( System.getProperty( "user.dir" ) );
        }
    }

    public static File getTargetDirectory()
    {
        return new File( getBaseDirectory(), "target" );
    }

    public static File getClassesDirectory()
    {
        return new File( getTargetDirectory(), "classes" );
    }

    private static File getTestClassesDirectory()
    {
        return new File( getTargetDirectory(), "test-classes" );
    }

    //----------------------------------------------------------
    // immutable state
    //----------------------------------------------------------

    private Object m_kernel;
    private ClassLoader m_classloader;
    private Object m_root;
    private Method m_locate;
    private Method m_runtime;
    private Method m_resolve;
    private Method m_shutdown;

    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

    /**
     * Constructor for MerlinEmbeddedTest.
     */
    public AbstractMerlinTestCase()
    {
        super();
    }

    /**
     * Constructor for MerlinEmbeddedTest.
     * @param name the name of the testcase
     */
    public AbstractMerlinTestCase( String name )
    {
        super( name );
    }

    public void setUp() throws Exception
    {
        ClassLoader classloader = AbstractMerlinTestCase.class.getClassLoader();

        try
        {
            File work = getWorkDirectory();
            File home = getHomeDirectory();

            Artifact artifact = DefaultBuilder.createImplementationArtifact( 
              classloader, home, work, 
              MERLIN_PROPERTIES, IMPLEMENTATION_KEY );

            File cache = getCacheDirectory();
            InitialContextFactory icFactory = 
              new DefaultInitialContextFactory( "merlin", work );
            icFactory.setCacheDirectory( cache );

            InitialContext context = icFactory.createInitialContext();

            Builder builder = new DefaultBuilder( context, artifact );
            m_classloader = builder.getClassLoader();
            Factory factory = builder.getFactory();
            Map criteria = factory.createDefaultCriteria();

            //
            // set the defaults
            //

            criteria.put( "merlin.context", "target" );
            criteria.put( "merlin.server", "true" );
            criteria.put( "merlin.code.security.enabled", "false" );
            criteria.put( "merlin.repository", cache );

            //
            // if the deployment path is undefined then the best we 
            // can do is to assume ${basedir}/target/classes and/or
            // ${basedir}/target/test-classes contains a BLOCK-INF/block.xml
            // and from this, derive a merlin.deployment value
            //

            String[] deployment = (String[]) criteria.get( "merlin.deployment" );
            if( deployment.length == 0 )
            {
                String path = buildDefaultTestPath();
                if( null != path )
                {
                    criteria.put( "merlin.deployment", path );
                }
                else
                {
                    final String error = 
                      "Cannot locate a deployment objective.";
                    throw new IllegalStateException( error );
                }
            }

            //
            // if the ${merlin.override} value is undefined, check for 
            // the existance of an override file in ${basedir}/conf/config.xml
            // and if it exists assign it as the override parameter
            //

            if( null == criteria.get( "merlin.override" ) )
            {  
                String override = buildDefaultOverridePath();
                if( null != override )
                {
                    criteria.put( "merlin.override", override );
                }
            }

            //
            // go ahead with the deployment of the kernel
            //

            m_kernel = factory.create( criteria );
            m_shutdown =
              m_kernel.getClass().getMethod( 
                "shutdown", 
                new Class[0] );
            Method method = 
              m_kernel.getClass().getMethod( 
                "getModel", 
                new Class[0] );
            m_root = method.invoke( m_kernel, new Object[0] );
            m_locate = 
              m_root.getClass().getMethod( 
                "getModel", 
                new Class[]{ String.class } );
            Class modelClass = 
              m_classloader.loadClass( DEPLOYMENT_MODEL_CLASSNAME );
            m_resolve = 
              modelClass.getMethod( "resolve", new Class[0] );
        }
        catch( Throwable e )
        {
            final String error = ExceptionHelper.packException( e, true );
            System.out.println( error );
            throw new Exception( error );
        }
    }

    public void tearDown()
    {
        m_classloader = null;
        m_root = null;
        m_locate = null;
        m_resolve = null;

        try
        {
            m_shutdown.invoke( m_kernel, new Object[0] );
        }
        catch( Throwable e )
        {
            // ignore
        }

        m_shutdown = null;
    }

    //----------------------------------------------------------------------
    // protected
    //----------------------------------------------------------------------

    protected Object resolve( String path ) throws Exception
    {
        if( null == m_kernel ) 
          throw new IllegalStateException( "kernel does not exist" );

        try
        {
            Object model =  m_locate.invoke( m_root, new Object[]{ path } );
            return m_resolve.invoke( model, new Object[0] );
        }
        catch( InvocationTargetException ite )
        {
            Throwable cause = ite.getTargetException();
            final String error = ExceptionHelper.packException( cause, true );
            throw new Exception( error );
        }
        catch( Throwable e )
        {
            final String error = ExceptionHelper.packException( e, true );
            throw new Exception( error );
        }
    }

    //----------------------------------------------------------------------
    // utilities
    //----------------------------------------------------------------------

    private String buildDefaultOverridePath()
    {
        File base = getBaseDirectory();
        File config = new File( base, "config.xml" );
        if( config.exists() ) return "config.xml";
        return null;
    }

    private String buildDefaultTestPath()
    {
        File base = getBaseDirectory();
        File classes = new File( getClassesDirectory(), "BLOCK-INF/block.xml" );
        File tests = new File( getTestClassesDirectory(), "BLOCK-INF/block.xml" );
        if( classes.exists() && tests.exists() )
        {
            return getClassesDirectory().toString() 
               + "," + getTestClassesDirectory().toString();
        }
        else if( classes.exists() )
        {
            return getClassesDirectory().toString();
        }
        else if( tests.exists() )
        {
            return getTestClassesDirectory().toString();
        }
        else
        {
            return null;
        }
    }

    private File getHomeDirectory()
    {
        return getCacheDirectory().getParentFile();
    }

    private File getCacheDirectory()
    {
        String cache = System.getProperty( "project.repository.cache.path" );
        if( null != cache )
        {
            return new File( cache );
        }
        else
        {
            throw new IllegalStateException( "don't know what to do yet" );
        }
    }


    /*
    private static File getMavenHome()
    {
        try
        {
            String local = 
              System.getProperty( 
                "maven.home.local", 
                Env.getEnvVariable( "MAVEN_HOME_LOCAL" ) );

            if( null != local ) return new File( local ).getCanonicalFile();

            return new File(
              System.getProperty( "user.home" ) 
              + File.separator 
              + ".maven" ).getCanonicalFile();

        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to access environment.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }
    */

   /**
    * Return the merlin home directory.
    * @return the merlin install directory
    */
    /*
    private static File getMerlinHome()
    {
        return new File( getMerlinHomePath() );
    }
    */

   /**
    * Return the merlin home directory path.
    * @return the merlin install directory path
    */
    /*
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
    */
}
