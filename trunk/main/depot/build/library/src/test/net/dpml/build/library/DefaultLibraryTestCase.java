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

package net.dpml.build.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.build.model.Module;
import net.dpml.build.model.Library;
import net.dpml.build.model.Processor;
import net.dpml.build.model.Resource;
import net.dpml.build.model.Type;
import net.dpml.build.model.ProcessorNotFoundException;
import net.dpml.build.info.LibraryDirective;
import net.dpml.build.info.ResourceDirective;

import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultLibraryTestCase extends AbstractTestCase
{   
   /**
    * Test request for all processors.
    */
    public void testProcessors()
    {
        Processor[] processors = m_library.getProcessors();
        assertEquals( "processor-count", 3, processors.length );
    }
    
   /**
    * Test library properties.
    */
    public void testProperties()
    {
        String[] names = m_library.getPropertyNames();
        assertEquals( "property-count", 3, names.length );
        //for( int i=0; i<names.length; i++ )
        //{
        //    System.out.println( "# " + names[i] );
        //}
    }
    
   /**
    * Testing inclusion of processors based on declared processor 
    * dependencies.  In this testcase the resource declares a dependency 
    * on a plugin and this implies a dependency on a jar processor - hense 
    * two resource type and two processors irrespective of the fact that 
    * the XML defintion contains a single plugin type reference.
    */
    public void testGetProcessorSequence() throws Exception
    {
        Resource resource = m_library.getResource( "dpml/tools/dpml-tools-ant" );
        //Type[] types = resource.getTypes();
        //assertEquals( "resource-type-count", 2, types.length );
        Processor[] processors = m_library.getProcessorSequence( resource );
        assertEquals( "processor-sequence-count", 2, processors.length );
    }
    
   /**
    * Test correct resolution of top-level modules from the library.
    */
    public void testRootModuleCount()
    {
        Module[] modules = m_library.getModules();
        assertEquals( "top-module-count", 6, modules.length );
    }
    
   /**
    * Test expanded module request and validate that the 'dpml' module
    * is on the end of the stack.
    */
    public void testExpandedModuleCount() throws Exception
    {
        Module[] modules = m_library.getAllModules();
        assertEquals( "expanded-module-count", 10, modules.length );
        Module dpml = m_library.getModule( "dpml" );
        assertEquals( "last-module", dpml, modules[ modules.length - 1 ] );
    }
    
   /**
    * Test aquisition of a named resource.
    */
    public void testGetResourceLevelOne() throws Exception
    {
        String spec = "ant";
        Resource resource = m_library.getResource( spec );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named resource.
    */
    public void testGetResourceLevelTwo() throws Exception
    {
        String spec = "ant/ant-junit";
        Resource resource = m_library.getResource( spec );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named resource.
    */
    public void testGetResourceLevelThree() throws Exception
    {
        String spec = "dpml/metro/dpml-composition-runtime";
        Resource resource = m_library.getResource( spec );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named module.
    */
    public void testGetModuleLevelOne() throws Exception
    {
        String spec = "dpml";
        Module module = m_library.getModule( spec );
        String path = module.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named module.
    */
    public void testGetModuleLevelTwo() throws Exception
    {
        String spec = "dpml/metro";
        Module module = m_library.getModule( spec );
        String path = module.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
    private static class MockType implements Type
    {
        private String m_name;
        private boolean m_alias;
        
        public MockType( String name, boolean alias )
        {
            m_name = name;
            m_alias = alias;
        }
        
        public String getName()
        {
            return m_name;
        }
    
        public boolean getAlias()
        {
            return m_alias;
        }
    
        public String[] getPropertyNames()
        {
            return new String[0];
        }
    
        public String getProperty( String key )
        {
            return null;
        }
    
        public String getProperty( String key, String value )
        {
            return value;
        }
    }
}
