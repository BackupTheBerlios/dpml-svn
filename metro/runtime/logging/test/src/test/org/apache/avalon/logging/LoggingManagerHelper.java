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

package org.apache.avalon.logging;

import java.io.File;
import java.util.Map;

import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;

import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;


/**
 * 
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LoggingManagerHelper.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class LoggingManagerHelper
{
    //-------------------------------------------------------------------
    // utilities
    //-------------------------------------------------------------------

    /**
     * Setup the initial context. 
     * @param filename the name of a file in the test/conf directory
     * @param bootstrap the boostrap logger logging level
     * @return the logging manager
     */
    public static LoggingManager setUpLoggingManager( 
      String id, String filename ) throws Exception
    {
        InitialContext context = 
          setUpInitialContext( "system.xml" );
        Artifact[] candidates = 
          context.getRepository().getCandidates( LoggingManager.class );
        Artifact artifact = selectArtifact( candidates, id );
        Builder builder = context.newBuilder( artifact );
        Factory factory = builder.getFactory();
        Map criteria = factory.createDefaultCriteria();

        //
        // customize the criteria
        //

        File basedir = getBaseDir();
        File file = new File( basedir, filename );
        criteria.put( "avalon.logging.configuration", file );
        criteria.put( "avalon.logging.basedir", basedir );

        //
        // create the logging manager
        //

        return (LoggingManager) factory.create( criteria );

    }

    public static InitialContext setUpInitialContext( String path ) throws Exception
    {
        DefaultInitialContextFactory factory = 
           new DefaultInitialContextFactory( "avalon", getBaseDir() );
        factory.setCacheDirectory( getRepositoryCache() );
        Artifact[] artifacts = 
          getArtifactsToRegister( path );
        factory.setFactoryArtifacts( artifacts );
        return factory.createInitialContext();
    }

    private static File getRepositoryCache()
    {
        String cache = System.getProperty( "project.repository.cache.path" );
        if( null != cache )
        {
            return new File( cache );
        }
        else
        {
            return getMavenRepositoryDirectory();
        }
    }

    private static Artifact selectArtifact( Artifact[] artifacts, String id )
    {
        for( int i=0; i<artifacts.length; i++ )
        {
            Artifact artifact = artifacts[i];
            if( artifact.getName().equals( id ) )
            {
                return artifact;
            }
        }
        throw new IllegalStateException( "No matching artifact id: " + id );
    }

    //-------------------------------------------------------------------
    // utilities
    //-------------------------------------------------------------------

    private static File getMavenRepositoryDirectory()
    {
        return new File( getMavenHomeDirectory(), "repository" );
    }

    private static File getMavenHomeDirectory()
    {
        return new File( getMavenHome() );
    }

    private static String getMavenHome()
    {
        try
        {
            String local = 
              System.getProperty( 
                "maven.home.local", 
                Env.getEnvVariable( "MAVEN_HOME_LOCAL" ) );
            if( null != local ) return local;

            return System.getProperty( "user.home" ) + File.separator + ".maven";

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

    private static File getBaseDir()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            path = System.getProperty( "basedir" );
            File root = new File( path );
            return new File( root, "target/test-classes" );
        }
    }

    private static Artifact[] getArtifactsToRegister( String path ) throws Exception
    {
        Configuration config = 
          getConfiguration( new File( getBaseDir(), path ) );
        Configuration[] children = 
          config.getChildren( "artifact" );
        Artifact[] artifacts = new Artifact[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String spec = child.getAttribute( "spec" );
            String uri = getURI( spec );
            Artifact artifact = Artifact.createArtifact( uri );
            artifacts[i] = artifact;
        }
        return artifacts;
    }

    private static String getURI( String spec )
    {
        if( spec.startsWith( "artifact:" ) ) return spec;
        return "artifact:" + spec;
    }

    private static Configuration getConfiguration( File file ) throws Exception
    {
        DefaultConfigurationBuilder builder = 
          new DefaultConfigurationBuilder();
        return builder.buildFromFile( file );  
    }
}
