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


package org.apache.avalon.composition.model.test;

import java.io.File;
import java.net.URL;

import org.apache.avalon.composition.data.SecurityProfile;
import org.apache.avalon.composition.data.builder.XMLSecurityProfileBuilder;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.impl.DefaultSystemContextFactory;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.ModelFactory;
import org.apache.avalon.composition.provider.SystemContextFactory;

import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.RepositoryCriteria;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.avalon.util.env.Env;

import junit.framework.TestCase;

public abstract class AbstractTestCase extends TestCase
{
    //-------------------------------------------------------
    // state
    //-------------------------------------------------------

    public static int PRIORITY;

    private static Logger LOGGER;

    public static final File BASEDIR;

    public static final File SYS_CONF;

    private static final XMLSecurityProfileBuilder SECURITY_BUILDER;

    static
    {
        PRIORITY = ConsoleLogger.LEVEL_INFO;
        LOGGER = new ConsoleLogger( PRIORITY );
        BASEDIR = getWorkDir();
        SYS_CONF = new File( BASEDIR, "system/kernel.xml" ).getAbsoluteFile();
        SECURITY_BUILDER = new XMLSecurityProfileBuilder();
    }
    
    private static File getWorkDir()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        path = System.getProperty( "basedir" );
        if( path != null )
        {
            File root = new File( path );
            return new File( root, "target/test-classes" );
        }
        
        //still no success resort to user.dir
        if( !(path != null) )
        {
        	path = System.getProperty( "user.dir" );
        }
        if( null != path )
        {
            return new File( path );
        }	        return null;
    }

    //-------------------------------------------------------
    // state
    //-------------------------------------------------------

    public ContainmentModel m_model;

   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

   /**
    * Setup the system context and create m_model using a path
    * relative to the ${basedir}/conf directory.
    */
    public ContainmentModel setUp( String path ) throws Exception
    {
        //
        // grab the system configuration
        //

        Configuration config = getConfiguration( SYS_CONF );
        System.out.println( "Loading " + SYS_CONF );
        //
        // create the initial context using the maven repository as the 
        // system repository
        //

        Configuration sysConfig = config.getChild( "system" );
        InitialContext context = setUpInitialContext( BASEDIR, sysConfig );

        //
        // create a system context factory and start profiling the 
        // system we want to run
        //

        SystemContextFactory factory = 
          new DefaultSystemContextFactory( context );

        //
        // setup the logging manager
        //

        String logConfigPath = config.getChild( "logging" ).getAttribute( "path" );
        File file = new File( BASEDIR, logConfigPath );
        URL url = file.toURL();
        LoggingManager logging = 
          DefaultSystemContextFactory.createLoggingManager( 
            context, null, null, url, false );
        factory.setLoggingManager( logging );

        //
        // setup the security profiles
        //

        Configuration secConfig = config.getChild( "security" );
        SecurityProfile[] profiles = 
          SECURITY_BUILDER.createSecurityProfiles( secConfig );
        factory.setSecurityProfiles( profiles );
        factory.setSecurityEnabled( true );

        //
        // create a local application repository to use during test
        // execution
        //

        //Repository repository = 
        //  createTestRepository( context, new File( BASEDIR, "repository" ) );
        //factory.setRepository( repository );

        //
        // and create the system context and grab the model factory
        // so we can construct a model of the test deployment scenario
        //

        SystemContext system = factory.createSystemContext();
        ModelFactory modelFactory = system.getModelFactory();

        //
        // create a containment model using the supplied path
        //

        File source = new File( BASEDIR, path );
        System.out.println("loading " + source.toURL());
        return modelFactory.createRootContainmentModel( source.toURL() );
    }

    private Repository createTestRepository( InitialContext context, File cache ) 
      throws Exception
    {
        Factory factory = context.getInitialFactory();
        RepositoryCriteria criteria = 
          (RepositoryCriteria) factory.createDefaultCriteria();
        criteria.setCacheDirectory( cache );
        criteria.setHosts( new String[0] );
        return (Repository) factory.create( criteria );
    }

    InitialContext setUpInitialContext( File base, Configuration config ) 
      throws Exception
    {
        InitialContextFactory initial = 
          new DefaultInitialContextFactory( "test", base );
        initial.setCacheDirectory( getCacheDirectory() );
        registerSystemArtifacts( initial, config );
        return initial.createInitialContext();
    }

    private void registerSystemArtifacts( 
      InitialContextFactory factory, Configuration config )
      throws Exception
    {
        Artifact[] artifacts = getArtifactsToRegister( config );
        factory.setFactoryArtifacts( artifacts );
    }

    private static Artifact[] getArtifactsToRegister( 
      Configuration config ) throws Exception
    {
        Configuration[] children = 
          config.getChildren( "artifact" );
        Artifact[] artifacts = new Artifact[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String spec = child.getAttribute( "spec" );
            Artifact artifact = Artifact.createArtifact( getURI( spec ) );
            artifacts[i] = artifact;
        }
        return artifacts;
    }

    private static String getURI( String spec )
    {
        if( spec.startsWith( "artifact:" ) ) return spec;
        return "artifact:" + spec;
    }

    public void printModel( String lead, DeploymentModel model )
    {
        if( model instanceof ContainmentModel )
        {
            printContainmentModel( lead, (ContainmentModel) model );
        }
        else if( model instanceof ComponentModel ) 
        {
            printComponentModel( lead, (ComponentModel) model );
        }
    }

    public void printContainmentModel( String lead, ContainmentModel model )
    {
        printDeploymentModel( lead, model );
        DeploymentModel[] models = model.getModels();
        if( models.length > 0 )
        {
            System.out.println( lead + "  children:" );
            for( int i=0; i<models.length; i++ )
            {
                DeploymentModel m = models[i];
                printModel( "    " + lead, m );
            }
        }
        models = model.getStartupGraph();
        if( models.length > 0 )
        {
            System.out.println( lead + "  startup:" );
            for( int i=0; i<models.length; i++ )
            {
                DeploymentModel m = models[i];
                System.out.println( "    " + lead + (i+1) + ": " + m );
            }
        }
        models = ((ContainmentModel)model).getShutdownGraph();
        if( models.length > 0 )
        {
            System.out.println( lead + "  shutdown:" );
            for( int i=0; i<models.length; i++ )
            {
                DeploymentModel m = models[i];
                System.out.println( "    " + lead + (i+1) + ": " + m );
            }
        }
    }

    public void printComponentModel( String lead, ComponentModel model )
    {
        printDeploymentModel( lead, model );
    }

    public void printDeploymentModel( String lead, DeploymentModel model )
    {
        System.out.println( 
          lead 
          + "model:" 
          + model + "(" 
          + model.getDeploymentTimeout() 
          + ")" );

        DeploymentModel[] providers = model.getProviderGraph();
        DeploymentModel[] consumers = model.getConsumerGraph();

        if(( providers.length == 0 ) && ( consumers.length == 0 ))
        {
            return;
        }

        if( providers.length > 0 ) for( int i=0; i<providers.length; i++ )
        {
            DeploymentModel m = providers[i];
            System.out.println( lead + "  <-- " + m );
        }

        if( consumers.length > 0 ) for( int i=0; i<consumers.length; i++ )
        {
            DeploymentModel m = consumers[i];
            System.out.println( lead + "  --> " + m );
        }
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
            return getMavenRepositoryDirectory();
        }
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
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to access environment.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }

    private static Configuration getConfiguration( File file ) throws Exception
    {
        DefaultConfigurationBuilder builder = 
          new DefaultConfigurationBuilder();
        return builder.buildFromFile( file );  
    }

    protected Logger getLogger()
    {
        return LOGGER;
    }

    protected File getBaseDir()
    {
        return BASEDIR;
    }
}
