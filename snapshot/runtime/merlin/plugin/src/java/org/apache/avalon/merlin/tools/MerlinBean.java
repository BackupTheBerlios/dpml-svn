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

package org.apache.avalon.merlin.tools;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.ArrayList;

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
 * @version $Id: MerlinBean.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class MerlinBean
{
    //----------------------------------------------------------
    // static
    //----------------------------------------------------------

    private static final String MERLIN_PROPERTIES = "merlin.properties";

    private static final String IMPLEMENTATION_KEY = "merlin.implementation";

    //----------------------------------------------------------
    // immutable state
    //----------------------------------------------------------

    private String m_deployment;

    private String[] m_hosts;

    private String m_debug;

    private String m_info;

    //----------------------------------------------------------
    // setters
    //----------------------------------------------------------

   /**
    * Set the deployment path.
    */
    public void setDeployment( String path )
    {
        m_deployment = path;
    }

   /**
    * Set the deployment path.
    */
    public void setHosts( String path )
    {
        if( null != path )
        {
            m_hosts = expandHosts( path );
        }
    }

   /**
    * Set the debug policy.
    */
    public void setDebug( String policy )
    {
        m_debug = policy;
    }

   /**
    * Set the info policy.
    */
    public void setInfo( String policy )
    {
        m_info = policy;
    }

   /**
    * Establish the merlin kernel.
    */
    public void doExecute() throws Exception
    {
        ClassLoader classloader = MerlinBean.class.getClassLoader();

        try
        {
            File basedir = getBaseDirectory();

            InitialContextFactory initial = 
              new DefaultInitialContextFactory( "merlin", basedir );
            initial.setCacheDirectory( getMavenRepositoryDirectory() );
            initial.setHosts( m_hosts );
            InitialContext context = initial.createInitialContext();

            Artifact artifact = 
              DefaultBuilder.createImplementationArtifact( 
                classloader, 
                getMerlinHome(),
                getBaseDirectory(), 
                MERLIN_PROPERTIES, 
                IMPLEMENTATION_KEY );

            Builder builder = context.newBuilder( artifact );
            Factory factory = builder.getFactory();
            Map criteria = factory.createDefaultCriteria();

            applyLocalProperties( criteria );

            factory.create( criteria );
        }
        catch( Throwable e )
        {
            final String error = ExceptionHelper.packException( e, true );
            System.out.println( error );
            throw new Exception( error );
        }
    }

    //----------------------------------------------------------------------
    // utilities
    //----------------------------------------------------------------------

    private void applyLocalProperties( Map criteria )
    {
        File repository = getMavenRepositoryDirectory();

        criteria.put( "merlin.repository", repository );
        criteria.put( "merlin.server", "false" );
        criteria.put( "merlin.code.security.enabled", "false" );

        if( m_deployment != null )
        {
            criteria.put( "merlin.deployment", m_deployment );
        }

        if( null != m_debug ) criteria.put( "merlin.debug", m_debug );
        if( null != m_info ) criteria.put( "merlin.info", m_info ); 
    }

    private static File getMavenRepositoryDirectory()
    {
        return new File( getMavenHome(), "repository" );
    }

    private static File getMavenHome()
    {
        try
        {
            String local = 
              System.getProperty( 
                "maven.home.local", 
                Env.getEnvVariable( "MAVEN_HOME_LOCAL" ) );
            if( null != local ) 
              return new File( local ).getCanonicalFile();

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

    private File getBaseDirectory()
    {
        final String base = System.getProperty( "basedir" );
        if( null != base )
        {
            return new File( base );
        }
        return new File( System.getProperty( "user.dir" ) );
    }

    private static String[] expandHosts( String arg )
    {
        ArrayList list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer( arg, "," );
        while( tokenizer.hasMoreTokens() )
        {
            list.add( tokenizer.nextToken() );
        }
        return (String[]) list.toArray( new String[0] );
    }

   /**
    * Return the merlin home directory.
    * @return the merlin install directory
    */
    private static File getMerlinHome()
    {
        return new File( getMerlinHomePath() );
    }

   /**
    * Return the merlin home directory path.
    * @return the merlin install directory path
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

}

