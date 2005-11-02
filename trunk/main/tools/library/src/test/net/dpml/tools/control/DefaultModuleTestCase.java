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

package net.dpml.tools.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.tools.model.Module;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Processor;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ProcessorNotFoundException;
import net.dpml.tools.info.LibraryDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.Scope;

import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Testing the DefaultModule implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultModuleTestCase extends AbstractTestCase
{   
    public void testExpandedAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) m_library.getResource( "dpml/transit" );
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, true, false );
        System.out.println( "# AGGREGATED MODULE PROVIDERS " + providers.length );
        for( int i=0; i<providers.length; i++ )
        {
            System.out.println( "# " + providers[i] );
        }
    }
    
   /**
    * Test expanded module request on the dpml module.
    */
    public void testModuleGetResources() throws Exception
    {
        Module metro = m_library.getModule( "dpml/metro" );
        Resource[] resources = metro.getResources();
        assertEquals( "nested-expanded-resource-count", 15, resources.length );
        //for( int i=0; i<resources.length; i++ )
        //{
        //    System.out.println( "> " + (i+1) + " " + resources[i] );
        //}
    }

   /**
    * Test aquisition of a named resource.
    */
    public void testGetResourceLevelOne() throws Exception
    {
        String spec = "dpml/metro";
        Module module = m_library.getModule( spec );
        String name = "dpml-composition-runtime";
        Resource resource = module.getResource( name );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec + "/" + name, path );
    }
    
   /**
    * Test aquisition of a named resource.
    */
    public void testGetResourceLevelTwo() throws Exception
    {
        String spec = "dpml";
        Module module = m_library.getModule( spec );
        String name = "metro/dpml-composition-runtime";
        Resource resource = module.getResource( name );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec + "/" + name, path );
    }
    
   /**
    * Test getModules operation.
    */
    public void testGetModule() throws Exception
    {
        Module dpml = m_library.getModule( "dpml" );
        Module[] modules = dpml.getModules();
        assertEquals( "nested-module-count", 4, modules.length );
    }
    
   /**
    * Test selection of the set of immediate child module from within 
    * the enclosing module.
    */
    public void testGetModules() throws Exception
    {
        Module dpml = m_library.getModule( "dpml" );
        Module[] modules = dpml.getModules();
        assertEquals( "nested-module-count", 4, modules.length );
    }
    
   /**
    * Test expanded module request on the dpml module.
    */
    public void testGetAllModules() throws Exception
    {
        Module dpml = m_library.getModule( "dpml" );
        Module[] modules = dpml.getAllModules();
        assertEquals( "nested-expanded-module-count", 4, modules.length );
    }
    
   /**
    * Test scoped non-transitive providers.
    */
    public void testProviders() throws Exception
    {
        try
        {
            String path = "dpml";
            doProviderTest( path, false, 9, 0, 0 ); 
            doProviderTest( path, true, 39, 39, 39 );
        }
        catch( Exception e )
        {
            throw e;
        }
        try
        {
            String path = "dpml/transit";
            doProviderTest( path, false, 4, 0, 0 );
            doProviderTest( path, true, 8, 4, 6 );
        }
        catch( Exception e )
        {
            throw e;
        }
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
    
    public void testSelectChildren() throws Exception
    {
        Resource[] resources = m_library.select( "*", true );
        assertEquals( "top-level-child-count", 6, resources.length );
    }
    
    public void testSelectAllModules() throws Exception
    {
        Resource[] resources = m_library.select( "**", false );
        assertEquals( "all-module-count", 10, resources.length );
    }

    public void testSelectWithPattern() throws Exception
    {
        Resource[] resources = m_library.select( "*l*", true );
        assertEquals( "'*l*'-selection", 2, resources.length );
    }
    
    public void testSelectAll() throws Exception
    {
        Resource[] resources = m_library.select( "**/*", false );
        //for( int i=0; i<resources.length; i++ )
        //{
        //    System.out.println( " (" + (i+1) + ")\t" + resources[i].getResourcePath() );
        //}
        assertEquals( "'**/*'-selection", 44, resources.length );
    }
    
    public void testSelectAllWithPattern() throws Exception
    {
        Resource[] resources = m_library.select( "**/*tools*", true );
        assertEquals( "'**/*tools*'-selection", 7, resources.length );
    }

    public void testAbsolutePattern() throws Exception
    {
        Resource[] resources = m_library.select( "dpml/tools/dpml-tools-control", false );
        assertEquals( "'**/*tools*'-selection", 1, resources.length );
    }
    
    public void testInvalidPatternWithWildcard() throws Exception
    {
        Resource[] resources = m_library.select( "dpml/tools/dpml-tools-control-*", false );
        assertEquals( "'dpml/tools/dpml-tools-control-*", 0, resources.length );
    }
}
