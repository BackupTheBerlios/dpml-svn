/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.repository.main;

import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase ;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.avalon.util.env.Env;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

/**
 * DefaultInitialContextFactoryTestCase
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultInitialContextFactoryTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultInitialContextFactoryTestCase extends TestCase
{
    private static final String KEY = "merlin";

    private static final File BASEDIR = getWorkDir();

    private static File getWorkDir()
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


    public void testInitialContextCreation() throws Exception
    {
        DefaultInitialContextFactory factory = 
          new DefaultInitialContextFactory( KEY, BASEDIR );

        //
        // Before creating the intial context we can set the 
        // hosts, cache, parent classloader (and the repoitory
        // implementation artifact but that would be unusual).
        //
        // Before modifying anything lets just check what the 
        // default values are.
        //

        System.out.println( "" );
        System.out.println( "InitialContextFactory" );
        System.out.println( "---------------------" );
        System.out.println( "  key: " + factory.getApplicationKey() );
        System.out.println( "  home: " + factory.getHomeDirectory() );
        System.out.println( "  cache: " + factory.getCacheDirectory() );
        System.out.println( "  work: " + factory.getWorkingDirectory() );
        System.out.println( "  impl: " + factory.getImplementation() );
        String[] hosts = factory.getHosts();
        for( int i=0; i<hosts.length; i++ )
        {
            System.out.println( 
              "  host (" + (i+1) + "): " 
              + hosts[i] );
        }
        System.out.println( "" );

        //
        // Normally the default values should be ok and you should 
        // not need to modify anything, however, you have available
        // serveral set methods on the InitialContextFactory interface
        // that allow customization of the factory. 

        // In this example
        // we will override the system cach with the local maven 
        // repository.
        //

        File repo = getRepositoryCache();
        factory.setCacheDirectory( repo );

        //
        // These actions declare factory artifacts to the initial context
        // so that other applications don't have to go hunting.
        //

        Artifact[] artifacts = 
          getArtifactsToRegister( "system.xml" );
        factory.setFactoryArtifacts( artifacts );

        //
        // Once customized we can proceed with the instantiation
        // of the initial context. The following method invocation
        // will trigger the population of the system cache with the  
        // resources necesary to load the repository implementation.
        //

        InitialContext context = factory.createInitialContext();

        //
        // The following code simply prints out the working and cache
        // directory, and the set of initial hosts assigned to the 
        // initial context.
        //

        System.out.println( "InitialContext" );
        System.out.println( "--------------" );
        System.out.println( "  work: " + context.getInitialWorkingDirectory() );
        System.out.println( "  cache: " + context.getInitialCacheDirectory() );
        hosts = context.getInitialHosts();
        for( int i=0; i<hosts.length; i++ )
        {
            System.out.println( 
              "  host (" + (i+1) + "): " 
              + hosts[i] );
        }
        System.out.println( "" );

        //
        // With the establishment of an initial context we can load any 
        // factory based system (e.g. logging, merlin, etc.).  The following code
        // requests an artifact for the repository implementation:
        //

        System.out.println( "Usage Example" );
        System.out.println( "-------------" );
        String key = Repository.class.getName();
        System.out.println( "  key: " + key );
        Artifact[] candidates = 
          context.getRepository().getCandidates( Repository.class );
        for( int i=0; i<candidates.length; i++ )
        {
            System.out.println( "  " + candidates[i] );
        }

        //
        // We can use the initial context to construct a plugin using a 
        // builder.  The following example demonstrates the creation of a 
        // a repository plugin (the same process applies for merlin, logging
        // plugins, runtime plugins, etc.).
        //

        if( candidates.length > 0 )
        {
            Builder builder = context.newBuilder( candidates[0] );
            Factory exampleFactory = builder.getFactory();
            Map criteria = exampleFactory.createDefaultCriteria();
            Repository exampleRepository = (Repository) exampleFactory.create( criteria );
            System.out.println( "  instance: " + exampleRepository );
            System.out.println( "" );
        }
    }

    private File getRepositoryCache()
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

    private Artifact[] getArtifactsToRegister( String path ) throws Exception
    {
        Configuration config = 
          getConfiguration( new File( BASEDIR, path ) );
        Configuration[] children = 
          config.getChildren( "artifact" );
        Artifact[] artifacts = new Artifact[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String spec = getSpec( child.getAttribute( "spec" ) );
            Artifact artifact = Artifact.createArtifact( spec );
            artifacts[i] = artifact;
        }
        return artifacts;
    }

    private String getSpec( String spec )
    {
        if( spec.startsWith( "artifact:" ) ) return spec;
        return "artifact:" + spec;
    }

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
        catch( IOException e )
        {
            final String error = 
              "Internal error while attempting to access environment.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }

    Configuration getConfiguration( File file ) throws Exception
    {
        DefaultConfigurationBuilder builder = 
          new DefaultConfigurationBuilder();
        return builder.buildFromFile( file );  
    }
}
