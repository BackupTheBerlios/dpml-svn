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
import net.dpml.library.Type;

import org.w3c.dom.Element;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultLibraryTestCase extends AbstractTestCase
{   
   /**
    * Test library properties.
    */
    public void testProperties()
    {
        String[] names = getLibrary().getPropertyNames();
        assertEquals( "property-count", 3, names.length );
    }
    
   /**
    * Test correct resolution of top-level modules from the library.
    */
    public void testRootModuleCount()
    {
        Module[] modules = getLibrary().getModules();
        assertEquals( "top-module-count", 6, modules.length );
    }
    
   /**
    * Test expanded module request and validate that the 'dpml' module
    * is on the end of the stack.
    * @exception Exception if the test fails
    */
    public void testExpandedModuleCount() throws Exception
    {
        Module[] modules = getLibrary().getAllModules();
        assertEquals( "expanded-module-count", 10, modules.length );
        Module dpml = getLibrary().getModule( "dpml" );
        assertEquals( "last-module", dpml, modules[ modules.length - 1 ] );
    }
    
   /**
    * Test aquisition of a named resource.
    * @exception Exception if the test fails
    */
    public void testGetResourceLevelOne() throws Exception
    {
        String spec = "ant";
        Resource resource = getLibrary().getResource( spec );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named resource.
    * @exception Exception if the test fails
    */
    public void testGetResourceLevelTwo() throws Exception
    {
        String spec = "ant/ant-junit";
        Resource resource = getLibrary().getResource( spec );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named resource.
    * @exception Exception if the test fails
    */
    public void testGetResourceLevelThree() throws Exception
    {
        String spec = "dpml/metro/dpml-composition-runtime";
        Resource resource = getLibrary().getResource( spec );
        String path = resource.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named module.
    * @exception Exception if the test fails
    */
    public void testGetModuleLevelOne() throws Exception
    {
        String spec = "dpml";
        Module module = getLibrary().getModule( spec );
        String path = module.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Test aquisition of a named module.
    * @exception Exception if the test fails
    */
    public void testGetModuleLevelTwo() throws Exception
    {
        String spec = "dpml/metro";
        Module module = getLibrary().getModule( spec );
        String path = module.getResourcePath();
        assertEquals( "spec-to-path", spec, path );
    }
    
   /**
    * Mock type class.
    */
    private static class MockType implements Type
    {
        private String m_name;
        private boolean m_alias;
        
       /**
        * Creation of a new mock type instance.
        * @param name the type name
        * @param alias the alias production flag
        */
        public MockType( String name, boolean alias )
        {
            m_name = name;
            m_alias = alias;
        }
        
       /**
        * Return the type name.
        * @return the name
        */
        public String getID()
        {
            return m_name;
        }
    
       /**
        * Return the type alias flag.
        * @return the flag
        */
        public boolean getAlias()
        {
            return m_alias;
        }
        
       /**
        * Return the type datastructure.
        * @return the data
        */
        public Element getElement()
        {
            return null;
        }
    }
}
