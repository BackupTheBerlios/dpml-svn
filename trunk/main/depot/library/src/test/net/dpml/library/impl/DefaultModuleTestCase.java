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

import net.dpml.library.Module;
import net.dpml.library.Resource;
import net.dpml.library.info.Scope;

/**
 * Testing the DefaultModule implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultModuleTestCase extends AbstractTestCase
{   
   /**
    * Test expanded anonymous module providers.
    * @exception Exception if a test error occurs
    */
    public void testExpandedAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) getLibrary().getResource( "dpml/transit" );
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, true, false );
    }
    
   /**
    * Test expanded module request on the dpml module.
    * @exception Exception if a test error occurs
    */
    public void testModuleGetResources() throws Exception
    {
        Module metro = getLibrary().getModule( "dpml/metro" );
        Resource[] resources = metro.getResources();
        assertEquals( "nested-expanded-resource-count", 9, resources.length );
    }

   /**
    * Test aquisition of a named resource.
    * @exception Exception if a test error occurs
    */
    public void testGetResourceLevelOne() throws Exception
    {
        String spec = "dpml/metro";
        Module module = getLibrary().getModule( spec );
        String name = "dpml-composition-runtime";
        Resource resource = module.getResource( name );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec + "/" + name, path );
    }
    
   /**
    * Test aquisition of a named resource.
    * @exception Exception if a test error occurs
    */
    public void testGetResourceLevelTwo() throws Exception
    {
        String spec = "dpml";
        Module module = getLibrary().getModule( spec );
        String name = "metro/dpml-composition-runtime";
        Resource resource = module.getResource( name );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec + "/" + name, path );
    }
    
   /**
    * Test getModules operation.
    * @exception Exception if a test error occurs
    */
    public void testGetModule() throws Exception
    {
        Module dpml = getLibrary().getModule( "dpml" );
        Module[] modules = dpml.getModules();
        assertEquals( "nested-module-count", 4, modules.length );
    }
    
   /**
    * Test selection of the set of immediate child module from within 
    * the enclosing module.
    * @exception Exception if a test error occurs
    */
    public void testGetModules() throws Exception
    {
        Module dpml = getLibrary().getModule( "dpml" );
        Module[] modules = dpml.getModules();
        assertEquals( "nested-module-count", 4, modules.length );
    }
    
   /**
    * Test expanded module request on the dpml module.
    * @exception Exception if a test error occurs
    */
    public void testGetAllModules() throws Exception
    {
        Module dpml = getLibrary().getModule( "dpml" );
        Module[] modules = dpml.getAllModules();
        assertEquals( "nested-expanded-module-count", 4, modules.length );
    }
    
   /**
    * Test scoped non-transitive providers.
    * @exception Exception if a test error occurs
    */
    public void testProviders() throws Exception
    {
        try
        {
            String path = "dpml";
            doProviderTest( path, false, 0, 4, 0 );
            doProviderTest( path, true, 0, 34, 0 );
            //doProviderTest( path, false );
            //doProviderTest( path, true );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "dpml/transit";
            doProviderTest( path, false, 2, 2, 0 );
            doProviderTest( path, true, 2, 4, 0 );
            //doProviderTest( path, false );
            //doProviderTest( path, true );
        }
        catch( Exception e )
        {
            throw e;
        }
        /* issue
        */
        /*try
        {
            String path = "dpml/metro";
            doProviderTest( path, false, 22, 0, 0 );
            doProviderTest( path, true, 36, 36, 36 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "ant";
            doProviderTest( path, false, 4, 0, 0 );
            doProviderTest( path, true, 5, 0, 0 );
        }
        catch( Exception e )
        {
            throw e;
        }*/
    }
    
   /**
    * Test scoped non-transitive providers.
    * @exception Exception if a test error occurs
    */
    public void testAggregatedProviders() throws Exception
    {
    /*
        try
        {
            String path = "dpml";
            doAggregatedProviderTest( path, false, 9, 9, 9 );
            doAggregatedProviderTest( path, true, 39, 39, 39 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "dpml/transit";
            doAggregatedProviderTest( path, false, 4, 4, 4 );
            doAggregatedProviderTest( path, true, 8, 8, 8 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "dpml/metro";
            doAggregatedProviderTest( path, false, 22, 22, 22 );
            doAggregatedProviderTest( path, true, 36, 36, 36 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "ant";
            doAggregatedProviderTest( path, false, 4, 4, 4 );
            doAggregatedProviderTest( path, true, 5, 5, 5 );
        }
        catch( Exception e )
        {
            throw e;
        }
        */
    }
    
   /**
    * Test select *.
    * @exception Exception if a test error occurs
    */
    public void testSelectChildren() throws Exception
    {
        Resource[] resources = getLibrary().select( "*", true );
        assertEquals( "top-level-child-count", 7, resources.length );
    }
    
   /**
    * Test select **.
    * @exception Exception if a test error occurs
    */
    public void testSelectAllModules() throws Exception
    {
        Resource[] resources = getLibrary().select( "**", false );
        assertEquals( "all-module-count", 10, resources.length );
    }

   /**
    * Test select pattern.
    * @exception Exception if a test error occurs
    */
    public void testSelectWithPattern() throws Exception
    {
        Resource[] resources = getLibrary().select( "*l*", true );
        assertEquals( "'*l*'-selection", 2, resources.length );
    }
    
   /**
    * Test select all.
    * @exception Exception if a test error occurs
    */
    public void testSelectAll() throws Exception
    {
        Resource[] resources = getLibrary().select( "**/*", false );
        assertEquals( "'**/*'-selection", 41, resources.length );
    }
    
   /**
    * Test select intermidiate pattern.
    * @exception Exception if a test error occurs
    */
    public void testSelectAllWithPattern() throws Exception
    {
        Resource[] resources = getLibrary().select( "**/*tools*", true );
        assertEquals( "'**/*tools*'-selection", 7, resources.length );
    }

   /**
    * Test select absolute pattern.
    * @exception Exception if a test error occurs
    */
    public void testAbsolutePattern() throws Exception
    {
        Resource[] resources = getLibrary().select( "dpml/tools/dpml-tools-control", false );
        assertEquals( "'**/*tools*'-selection", 1, resources.length );
    }
    
   /**
    * Test select invalid pattern.
    * @exception Exception if a test error occurs
    */
    public void testInvalidPatternWithWildcard() throws Exception
    {
        Resource[] resources = getLibrary().select( "dpml/tools/dpml-tools-control-*", false );
        assertEquals( "'dpml/tools/dpml-tools-control-*", 0, resources.length );
    }
}
