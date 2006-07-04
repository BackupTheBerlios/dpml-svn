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

package net.dpml.library.impl;

import java.io.File;

import net.dpml.library.Resource;
import net.dpml.library.info.Scope;
import net.dpml.library.Type;

import net.dpml.transit.Artifact;
import net.dpml.lang.Category;

/**
 * Testing the DefaultResource implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultResourceTestCase extends AbstractTestCase
{   
   /**
    * Test resource name.
    * @exception Exception if a test error occurs
    */
    public void testName() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-composition-runtime" );
        String name = resource.getName();
        assertEquals( "name", "dpml-composition-runtime", name );
    }
    
   /**
    * Test assigned build signature version.
    * @exception Exception if a test error occurs
    */
    public void testExplicitVersion() throws Exception
    {
        Resource resource = getLibrary().getResource( "junit/junit" );
        String version = resource.getVersion();
        assertEquals( "version", "@JUNIT_VERSION@", version );
    }
    
   /**
    * Test assigned build signature version.
    * @exception Exception if a test error occurs
    */
    public void testBuildSignatureVersion() throws Exception
    {
        System.setProperty( "build.signature", "project.timestamp" );
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-composition-runtime" );
        String version = resource.getVersion();
        assertEquals( "version", "YYYYMMDD.HHMMSS".length(), version.length() );
        System.getProperties().remove( "build.signature" );
    }
    
   /**
    * Test resource version.
    * @exception Exception if a test error occurs
    */
    public void testSnapshotVersion() throws Exception
    {
        System.getProperties().remove( "build.signature" );
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-composition-runtime" );
        String version = resource.getVersion();
        assertEquals( "version", "SNAPSHOT", version );
    }
    
   /**
    * Test resource path.
    * @exception Exception if a test error occurs
    */
    public void testResourcePath() throws Exception
    {
        String path = "dpml/metro/dpml-composition-runtime";
        Resource resource = getLibrary().getResource( path );
        assertEquals( "resource-path", path, resource.getResourcePath() );
    }
    
   /**
    * Test basedir feature.
    * @exception Exception if a test error occurs
    */
    public void testBaseDir() throws Exception
    {
        String path = "dpml";
        Resource resource = getLibrary().getResource( path );
        File basedir = resource.getBaseDir();
        assertNotNull( "basedir", basedir );
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File dpml = new File( test, "dpml" );
        File base = new File( dpml, ".." ).getCanonicalFile();
        assertEquals( "basedir", base, basedir );
    }

   /**
    * Test basedir feature.
    * @exception Exception if a test error occurs
    */
    public void testNullBaseDir() throws Exception
    {
        String path = "ant/ant";
        Resource resource = getLibrary().getResource( path );
        File basedir = resource.getBaseDir();
        assertNull( "basedir", basedir );
    }
    
   /**
    * Test resource types.
    * @exception Exception if a test error occurs
    */
    public void testTypeNames() throws Exception
    {
        String path = "dpml/metro/dpml-composition-runtime";
        Resource resource = getLibrary().getResource( path );
        Type[] types = resource.getTypes();
        Type jar = types[0];
        Type part = types[1];
        assertEquals( "types-length", 2, types.length );
        assertEquals( "jar-type", "jar", jar.getID() );
        assertEquals( "part-type", "part", part.getID() );
    }
    
   /**
    * Test isa function on an implied type.
    * @exception Exception if a test error occurs
    */
    public void testIsa() throws Exception
    {
        String path = "dpml/tools/dpml-tools-ant";
        Resource resource = getLibrary().getResource( path );
        assertTrue( "isa-jar", resource.isa( "jar" ) );
        assertTrue( "isa-plugin", resource.isa( "part" ) );
        assertFalse( "isa-rabbit", resource.isa( "rabbit" ) );
    }
    
   /**
    * Test artifact uri function.
    * @exception Exception if a test error occurs
    */
    public void testArtifact() throws Exception
    {
        String path = "dpml/tools/dpml-tools-ant";
        Resource resource = getLibrary().getResource( path );
        Type[] types = resource.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            Type type = types[i];
            Artifact artifact = resource.getArtifact( type.getID() );
            String urn = 
              "artifact:" 
              + type.getID() 
              + ":" 
              + resource.getResourcePath() 
              + "#" 
              + resource.getVersion();
            assertEquals( "uri", urn, artifact.toURI().toString() );
        }
    }
    
   /**
    * Test enclosing parent module.
    * @exception Exception if a test error occurs
    */
    public void testGetParent() throws Exception
    {
        String path = "dpml/tools/dpml-tools-ant";
        Resource dpml = getLibrary().getResource( "dpml" );
        Resource tools = getLibrary().getResource( "dpml/tools" );
        Resource ant = getLibrary().getResource( "dpml/tools/dpml-tools-ant" );
        assertEquals( "parent", null, dpml.getParent() );
        assertEquals( "parent", dpml, tools.getParent() );
        assertEquals( "parent", tools, ant.getParent() );
    }

   /**
    * Test scoped non-transitive providers.
    * @exception Exception if a test error occurs
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
    * Testing aggregated providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedProvidersCase1() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-composition-runtime" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, false, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, false, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, false, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 6, runtime.length );
        assertEquals( "test", 6, test.length );
    }    
    
   /**
    * Testing aggregated providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedExpandedProvidersCase1() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-composition-runtime" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, true, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, true, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, true, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 12, runtime.length );
        assertEquals( "test", 12, test.length );
    }

   /**
    * Testing aggregated providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedProvidersCase2() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/transit/dpml-transit-tools" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, false, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, false, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, false, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 2, runtime.length );
        assertEquals( "test", 3, test.length );
    }    
    
   /**
    * Testing aggregated providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedExpandedProvidersCase2() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/transit/dpml-transit-tools" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, true, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, true, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, true, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 3, runtime.length );
        assertEquals( "test", 5, test.length );
    }
    
   /**
    * Testing aggregated providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedProvidersCase3() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-component-model" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, false, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, false, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, false, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 4, runtime.length );
        assertEquals( "test", 8, test.length );
    }    
    
   /**
    * Testing aggregated providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedExpandedProvidersCase3() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/metro/dpml-component-model" );
        Resource[] build = resource.getAggregatedProviders( Scope.BUILD, true, false );
        Resource[] runtime = resource.getAggregatedProviders( Scope.RUNTIME, true, false  );
        Resource[] test = resource.getAggregatedProviders( Scope.TEST, true, false );
        assertEquals( "build", 0, build.length );
        assertEquals( "runtime", 5, runtime.length );
        assertEquals( "test", 12, test.length );
    }
    
   /**
    * Testing classpath providers.
    * @exception Exception if a test error occurs
    */
    public void testRuntimeClasspathProviders() throws Exception
    {
        String path = "dpml/transit/dpml-transit-tools";
        Resource resource = getLibrary().getResource( path );
        Resource[] chain = resource.getClasspathProviders( Scope.RUNTIME );
        assertEquals( "chain-length", 3, chain.length );
    }
    
   /**
    * Testing test classpath providers.
    * @exception Exception if a test error occurs
    */
    public void testTestClasspathProviders() throws Exception
    {
        String path = "dpml/transit/dpml-transit-tools";
        Resource resource = getLibrary().getResource( path );
        Resource[] chain = resource.getClasspathProviders( Scope.TEST );
        assertEquals( "chain-length", 5, chain.length );
    }
    
   /**
    * Testing test classloader chain construction.
    * @exception Exception if a test error occurs
    */
    public void testClassloaderChainConstruction() throws Exception
    {
        doClasspathChainTest( "dpml/metro/dpml-composition-runtime", 0, 2, 7, 3 );
        doClasspathChainTest( "dpml/tools/dpml-tools-ant", 4, 0, 0, 5 );
        doClasspathChainTest( "dpml/transit/dpml-transit-tools", 0, 0, 0, 3 );
        //doClasspathChainTest( "dpml/metro/dpml-composition-runtime" );
        //doClasspathChainTest( "dpml/tools/dpml-tools-ant" );
        //doClasspathChainTest( "dpml/transit/dpml-transit-tools" );
    }
    
   /**
    * Testing direct consumers.
    * @exception Exception if a test error occurs
    */
    public void testDirectConsumers() throws Exception
    {
        String path = "dpml/transit/dpml-transit-main";
        Resource resource = getLibrary().getResource( path );
        Resource[] consumers = resource.getConsumers( false, false );
        assertEquals( "consumer-count", 5, consumers.length );
    }
    
   /**
    * Testing transitive consumers.
    * @exception Exception if a test error occurs
    */
    public void testTransitiveConsumers() throws Exception
    {
        String path = "dpml/transit/dpml-transit-main";
        Resource resource = getLibrary().getResource( path );
        Resource[] consumers = resource.getConsumers( true, true );
        assertEquals( "consumer-count", 18, consumers.length );
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
        Resource resource = getLibrary().getResource( path );
        Resource[] chain = resource.getClasspathProviders( category );
        int j = chain.length;
        if( n > -1 )
        {
            assertEquals( "chain-length in " + path, n, j );
        }
        else
        {
            System.out.println( "# " + path + ", " + category.getName().toUpperCase() );
            for( int i=0; i<chain.length; i++ )
            {
                System.out.println( "# (" + ( i+1 ) + ") " + chain[i] );
            }
        }
        return chain.length;
    }
}
