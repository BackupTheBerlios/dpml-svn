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

package org.apache.avalon.activation.impl.test;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.avalon.activation.impl.DefaultRuntime;

import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.data.SecurityProfile;
import org.apache.avalon.composition.data.builder.XMLContainmentProfileCreator;
import org.apache.avalon.composition.data.builder.XMLSecurityProfileBuilder;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.impl.DefaultSystemContextFactory;
import org.apache.avalon.composition.model.impl.DefaultSecurityModel;
import org.apache.avalon.composition.provider.ModelFactory;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.SystemContextFactory;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.provider.Factory;
import org.apache.avalon.repository.provider.RepositoryCriteria;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;

import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.avalon.util.env.Env;

public abstract class AbstractTestCase extends TestCase
{
   //-------------------------------------------------------
   // static
   //-------------------------------------------------------

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

    public static final File BASEDIR = getWorkDir();

    public static final File SYS_CONF = 
      new File( BASEDIR, "system/kernel.xml" );

    private static final XMLSecurityProfileBuilder SECURITY_BUILDER = 
      new XMLSecurityProfileBuilder();

   //-------------------------------------------------------
   // state
   //-------------------------------------------------------

    protected Logger m_logger;

    protected ContainmentModel m_model;

    protected SystemContext m_system;
    
    protected boolean m_secured;

   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public AbstractTestCase( )
    {
        this( "data", false );
    }

    public AbstractTestCase( String name )
    {
        this( name, false );
    }
    
    public AbstractTestCase( String name, boolean secured )
    {
        super( name );
        m_secured = secured;
    }

   //-------------------------------------------------------
   // setup
   //-------------------------------------------------------

   /**
    * The setup process covers the establishment of the base
    * directory (from which relative references for extension directories
    * and fileset base directories are resolved), a file repository (not
    * used in this test case at this time), and a class loader model from 
    * which a classpath will be established.
    *
    * @exception Exception if things don't work out
    */
    public void setUp( String filename ) throws Exception
    {
        File block = new File( BASEDIR, filename );
        setUp( BASEDIR, block );
    }

   /**
    * The setup process covers the establishment of the base
    * directory (from which relative references for extension directories
    * and fileset base directories are resolved), a file repository (not
    * used in this test case at this time), and a class loader model from 
    * which a classpath will be established.
    *
    * @exception Exception if things don't work out
    */
    public void setUp( File base, File block ) throws Exception
    {
        //
        // create the initial context using the maven repository as the 
        // system repository
        //

        Configuration config = getConfiguration( SYS_CONF );
        Configuration sysConfig = config.getChild( "system" );
        InitialContext context = setUpInitialContext( base, sysConfig );

        //
        // create a system context and add a test repository to use during 
        // testcase execution
        //

        SystemContextFactory factory = 
          new DefaultSystemContextFactory( context );

        //
        // setup the logging manager
        //

        String logConfigPath = config.getChild( "logging" ).getAttribute( "path" );
        File file = new File( base, logConfigPath );
        URL url = file.toURL();
        LoggingManager logging = 
          DefaultSystemContextFactory.createLoggingManager( 
            context, null, null, url, false );
        factory.setLoggingManager( logging );
        
        //
        // setup security
        //

        Configuration secConfig = config.getChild( "security" );

        if( m_secured )
        {
            SecurityProfile[] profiles = 
              SECURITY_BUILDER.createSecurityProfiles( secConfig );
            factory.setSecurityProfiles( profiles );
            factory.setSecurityEnabled( true );
        }

        //Repository repository = 
        //  createTestRepository( context, new File( base, "repository" ) );
        //factory.setRepository( repository );

        factory.setRuntime( DefaultRuntime.class );
        m_system = factory.createSystemContext();

        m_logger = m_system.getLogger();

        //
        // load the meta data using the profile returned from getPath()
        // and establish a containment model for the unit test
        //

        ContainmentProfile profile = setUpProfile( block );
        m_model = m_system.getModelFactory().createRootContainmentModel( profile );
    }

    InitialContext setUpInitialContext( 
      File base, Configuration config ) throws Exception
    {
        InitialContextFactory initial = 
          new DefaultInitialContextFactory( "test", base );
        initial.setCacheDirectory( getCacheDirectory() );
        registerSystemArtifacts( initial, config );
        return initial.createInitialContext();
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

    private Repository createTestRepository( 
      InitialContext context, File cache ) throws Exception
    {
        Factory factory = context.getInitialFactory();
        RepositoryCriteria criteria = 
          (RepositoryCriteria) factory.createDefaultCriteria();
        criteria.setCacheDirectory( cache );
        criteria.setHosts( new String[0] );
        return (Repository) factory.create( criteria );
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
            String spec = getURI( child.getAttribute( "spec" ) );
            Artifact artifact = Artifact.createArtifact( spec );
            artifacts[i] = artifact;
        }
        return artifacts;
    }

    private static String getURI( String path )
    {
        if( path.startsWith( "artifact:" ) ) return path;
        return "artifact:" + path;
    }

    protected ContainmentProfile setUpProfile( File file )
      throws Exception
    {
        XMLContainmentProfileCreator creator = new XMLContainmentProfileCreator();
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration config = builder.buildFromFile( file );
        return creator.createContainmentProfile( config );
    }

    protected static File getBaseDirectory()
    {
        return BASEDIR;
    }

    protected Logger getLogger()
    {
        return m_logger;
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate the composition model.
    */
    public void executeDeploymentCycle() throws Exception
    {

        //
        // 1. assemble the model during which all dependencies
        //    are resolved (deployment and runtime)
        //

        getLogger().debug( "model assembly" );
        m_model.assemble();

        //
        // 2. create the root block using the service context
        //    and the root containment model
        //

        getLogger().debug( "model commissioning" );
        m_model.commission();

        //
        // 4-5. suspend and resume the root block (not implemented yet)
        //
        // 6. decommission the block during which all managed appliances
        //    are decommissioned resulting in the decommissioning of all
        //    appliance instances
        //

        m_model.decommission();

        //
        // 7. disassemble the block during which reference between 
        //    appliances established at assembly time are discarded
        //

        m_model.disassemble();

        //
        // 8. dispose of the appliance during which all subsidiary 
        //    appliances are disposed of in an orderly fashion
        //

        if( m_model instanceof Disposable )
        {
            ((Disposable)m_model).dispose();
        }

        assertTrue( true );
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

            return System.getProperty( "user.home" ) 
              + File.separator + ".maven";

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

}
