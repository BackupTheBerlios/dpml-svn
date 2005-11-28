/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.build.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.net.URI;
import java.util.Properties;
import java.util.Date;

import junit.framework.TestCase;

import net.dpml.build.model.Module;
import net.dpml.build.model.Library;
import net.dpml.build.model.Processor;
import net.dpml.build.model.Resource;
import net.dpml.build.model.Type;
import net.dpml.build.info.LibraryDirective;
import net.dpml.build.info.ResourceDirective;
import net.dpml.build.info.Scope;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;
import net.dpml.transit.Category;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Testing the DefaultResource implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultResourceTestCase extends AbstractTestCase
{   
   /**
    * Test resource name.
    */
    public void testName() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/metro/dpml-composition-runtime" );
        String name = resource.getName();
        assertEquals( "name", "dpml-composition-runtime", name );
    }
    
   /**
    * Test assigned build signature version.
    */
    public void testExplicitVersion() throws Exception
    {
        Resource resource = m_library.getResource( "junit/junit" );
        String version = resource.getVersion();
        assertEquals( "version", "@JUNIT_VERSION@", version );
        //System.out.println( "# version: " + version );
    }
    
   /**
    * Test assigned build signature version.
    */
    public void testBuildSignatureVersion() throws Exception
    {
        System.setProperty( "build.signature", "project.timestamp" );
        Resource resource = m_library.getResource( "dpml/metro/dpml-composition-runtime" );
        String version = resource.getVersion();
        assertEquals( "version", "YYYYMMDD.HHMMSS".length(), version.length() );
        System.getProperties().remove( "build.signature" );
        //System.out.println( "# version: " + version );
    }
    
   /**
    * Test resource version.
    */
    public void testSnapshotVersion() throws Exception
    {
        System.getProperties().remove( "build.signature" );
        Resource resource = m_library.getResource( "dpml/metro/dpml-composition-runtime" );
        String version = resource.getVersion();
        assertEquals( "version", "SNAPSHOT", version );
        //System.out.println( "# version: " + version );
    }
    
   /**
    * Test resource path.
    */
    public void testResourcePath() throws Exception
    {
        String path = "dpml/metro/dpml-composition-runtime";
        Resource resource = m_library.getResource( path );
        assertEquals( "resource-path", path, resource.getResourcePath() );
    }
    
   /**
    * Test basedir feature.
    */
    public void testBaseDir() throws Exception
    {
        String path = "dpml";
        Resource resource = m_library.getResource( path );
        File basedir = resource.getBaseDir();
        assertNotNull( "basedir", basedir );
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File base = new File( test, ".." ).getCanonicalFile();
        assertEquals( "basedir", base, basedir );
    }

   /**
    * Test basedir feature.
    */
    public void testNullBaseDir() throws Exception
    {
        String path = "ant/ant";
        Resource resource = m_library.getResource( path );
        File basedir = resource.getBaseDir();
        assertNull( "basedir", basedir );
    }
    
   /**
    * Test resource types.
    */
    public void testTypeNames() throws Exception
    {
        String path = "dpml/metro/dpml-composition-runtime";
        Resource resource = m_library.getResource( path );
        Type[] types = resource.getTypes();
        //for( int i=0; i<types.length; i++ )
        //{
        //    System.out.println( "# type: " + types[i] );
        //}
        Type jar = types[0];
        Type plugin = types[1];
        assertEquals( "types-length", 2, types.length );
        assertEquals( "jar-type", "jar", jar.getName() );
        assertEquals( "plugin-type", "plugin", plugin.getName() );
    }
    
   /**
    * Test isa function on an implied type.
    */
    public void testIsa() throws Exception
    {
        String path = "dpml/tools/dpml-tools-ant";
        Resource resource = m_library.getResource( path );
        assertTrue( "isa-jar", resource.isa( "jar" ) );
        assertTrue( "isa-plugin", resource.isa( "plugin" ) );
        assertFalse( "isa-rabbit", resource.isa( "rabbit" ) );
    }
    
   /**
    * Test artifact uri function.
    */
    public void testArtifact() throws Exception
    {
        String path = "dpml/tools/dpml-tools-ant";
        Resource resource = m_library.getResource( path );
        Type[] types = resource.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            Type type = types[i];
            Artifact artifact = resource.getArtifact( type.getName() );
            String urn = 
              "artifact:" 
              + type.getName() 
              + ":" 
              + resource.getResourcePath() 
              + "#" 
              + resource.getVersion();
            assertEquals( "uri", urn, artifact.toURI().toString() );
        }
    }
    
   /**
    * Test enclosing parent module.
    */
    public void testGetParent() throws Exception
    {
        String path = "dpml/tools/dpml-tools-ant";
        Resource dpml = m_library.getResource( "dpml" );
        Resource tools = m_library.getResource( "dpml/tools" );
        Resource ant = m_library.getResource( "dpml/tools/dpml-tools-ant" );
        assertEquals( "parent", null, dpml.getParent() );
        assertEquals( "parent", dpml, tools.getParent() );
        assertEquals( "parent", tools, ant.getParent() );
    }

   /**
    * Test scoped non-transitive providers.
    */
    public void testProviders() throws Exception
    {
        try
        {
            String path = "dpml/metro/dpml-composition-runtime";
            doProviderTest( path, false, 0, 6, 0 );
            doProviderTest( path, true, 0, 12, 0 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "dpml/transit/dpml-transit-tools";
            doProviderTest( path, false, 0, 2, 1 );
            doProviderTest( path, true, 0, 3, 4 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "dpml/metro/dpml-component-model";
            doProviderTest( path, false, 0, 4, 4 );
            doProviderTest( path, true, 0, 5, 11 );
        }
        catch( Exception e )
        {
            throw e;
        }
    }

   /**
    * Checking
    */
    public void testAggregatedProvidersCase1() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/metro/dpml-composition-runtime" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, false, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, false, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, false, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 6, runtime.length );
        assertEquals( "test", 6, test.length );
    }    
    
    public void testAggregatedExpandedProvidersCase1() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/metro/dpml-composition-runtime" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, true, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, true, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, true, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 12, runtime.length );
        assertEquals( "test", 12, test.length );
    }

    public void testAggregatedProvidersCase2() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/transit/dpml-transit-tools" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, false, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, false, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, false, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 2, runtime.length );
        assertEquals( "test", 3, test.length );
    }    
    
    public void testAggregatedExpandedProvidersCase2() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/transit/dpml-transit-tools" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, true, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, true, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, true, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 3, runtime.length );
        assertEquals( "test", 5, test.length );
    }
    
    public void testAggregatedProvidersCase3() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/metro/dpml-component-model" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, false, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, false, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, false, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 4, runtime.length );
        assertEquals( "test", 8, test.length );
    }    
    
    public void testAggregatedExpandedProvidersCase3() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/metro/dpml-component-model" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, true, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, true, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, true, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 5, runtime.length );
        assertEquals( "test", 12, test.length );
    }
    
    public void testRuntimeClasspathProviders() throws Exception
    {
        String path = "dpml/transit/dpml-transit-tools";
        Resource resource = m_library.getResource( path );
        Resource[] chain = resource.getClasspathProviders( Scope.RUNTIME );
        assertEquals( "chain-length", 3, chain.length );
    }
    
    public void testTestClasspathProviders() throws Exception
    {
        String path = "dpml/transit/dpml-transit-tools";
        Resource resource = m_library.getResource( path );
        Resource[] chain = resource.getClasspathProviders( Scope.TEST );
        assertEquals( "chain-length", 5, chain.length );
    }
    
    public void testClassloaderChainConstruction() throws Exception
    {
        doClasspathChainTest( "dpml/metro/dpml-composition-runtime", 0, 3, 6, 3 );
        doClasspathChainTest( "dpml/tools/dpml-tools-ant", 4, 0, 0, 5 );
        doClasspathChainTest( "dpml/transit/dpml-transit-tools", 0, 0, 0, 3 );
    }
    
    public void testDirectConsumers() throws Exception
    {
        String path = "dpml/transit/dpml-transit-main";
        Resource resource = m_library.getResource( path );
        Resource[] consumers = resource.getConsumers( false, false );
        assertEquals( "consumer-count", 6, consumers.length );
    }
    
    public void testTransitiveConsumers() throws Exception
    {
        String path = "dpml/transit/dpml-transit-main";
        Resource resource = m_library.getResource( path );
        Resource[] consumers = resource.getConsumers( true, true );
        assertEquals( "consumer-count", 20, consumers.length );
    }
    
    //-------------------------------------------------------------------------------
    // helper code
    //-------------------------------------------------------------------------------
    
    private void doClasspathChainTest( String path, int a, int b, int c, int d ) throws Exception
    {
        doCategoryTest( path, Category.SYSTEM, a );
        doCategoryTest( path, Category.PUBLIC, b );
        doCategoryTest( path, Category.PROTECTED, c );
        doCategoryTest( path, Category.PRIVATE, d );
    }
    
    private void doClasspathChainTest( String path ) throws Exception
    {
        int sys = doCategoryTest( path, Category.SYSTEM, -1 );
        int pub = doCategoryTest( path, Category.PUBLIC, -1 );
        int pro = doCategoryTest( path, Category.PROTECTED, -1 );
        int pri = doCategoryTest( path, Category.PRIVATE, -1 );
        System.out.println( 
          "# classpath: " 
          + path 
          + " ("
          + sys + ", "
          + pub + ", "
          + pro + ", "
          + pri + ")" 
        );
    }
    
    private int doCategoryTest( String path, Category category, int n ) throws Exception
    {
        Resource resource = m_library.getResource( path );
        Resource[] chain = resource.getClasspathProviders( category );
        int j = chain.length;
        if( n > -1 )
        {
            assertEquals( "chain-length", n, j );
        }
        else
        {
            System.out.println( "# " + path + ", " + category.getName().toUpperCase() );
            for( int i=0; i<chain.length; i++ )
            {
                System.out.println( "# (" + (i+1) + ") " + chain[i] );
            }
        }
        return chain.length;
    }
}
