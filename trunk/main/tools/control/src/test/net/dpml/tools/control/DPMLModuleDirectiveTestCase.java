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

import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.control.ModuleDirectiveBuilder;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DPMLModuleDirectiveTestCase extends AbstractTestCase
{   
    private ModuleDirective m_module;
    
    public void setUp() throws Exception
    {
        m_module = load( "dpml.xml" );
    }
    
    public void testModuleCount() throws Exception
    {
        ModuleDirective[] modules = m_module.getModuleDirectives();
        int size = modules.length;
        assertEquals( "module-count", 3, size );
    }
    
    public void testProjectCount() throws Exception
    {
        ProjectDirective[] projects = m_module.getProjectDirectives();
        int size = projects.length;
        assertEquals( "project-count", 0, size );
    }
    
    public void testResourceCount() throws Exception
    {
        ResourceDirective[] resources = m_module.getResourceDirectives();
        int size = resources.length;
        assertEquals( "resource-count", 0, size );
    }
    
    public void testEncoding() throws Exception
    {
        doEncodingTest( m_module, "dpml-module-descritor.xml" );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_module );
    }
}
