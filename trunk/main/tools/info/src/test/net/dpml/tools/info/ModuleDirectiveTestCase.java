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

package net.dpml.tools.info;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModuleDirectiveTestCase extends AbstractTestCase
{
    private final String m_name = "name";
    private final String m_version = "1.1.1";
    private final String m_basedir = ".";
    private final IncludeDirective[] m_refs = new IncludeDirective[0];
    private final ModuleDirective[] m_modules = new ModuleDirective[0];
    private final ProjectDirective[] m_projects = new ProjectDirective[0];
    private final ResourceDirective[] m_resources = new ResourceDirective[0];
    
    private ModuleDirective m_module;
    
    public void setUp() throws Exception
    {
        m_module = 
          new ModuleDirective( 
            m_name, m_version, m_basedir, m_refs, m_modules, m_projects, m_resources );
    }

    public void testNullName()
    {
        try
        {
            new ModuleDirective( 
              null, m_version, m_basedir, m_refs, m_modules, m_projects, m_resources );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullVersion()
    {
        try
        {
            new ModuleDirective( 
              m_name, null, m_basedir, m_refs, m_modules, m_projects, m_resources );
        }
        catch( NullPointerException e )
        {
            fail( "NPE" );
        }
    }
    
    public void testNullBasedir()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, null, m_refs, m_modules, m_projects, m_resources );
        }
        catch( NullPointerException e )
        {
            fail( "NPE" );
        }
    }
    
    public void testNullRefs()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_basedir, null, m_modules, m_projects, m_resources );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullModules()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_basedir, m_refs, null, m_projects, m_resources );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullProjects()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_basedir, m_refs, m_modules, null, m_resources );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullResources()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_basedir, m_refs, m_modules, m_projects, null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testName()
    {
        assertEquals( "name", m_name, m_module.getName() );
    }
    
    public void testVersion()
    {
        assertEquals( "version", m_version, m_module.getVersion() );
    }
    
    public void testBasedir()
    {
        assertEquals( "basedir", m_basedir, m_module.getBasedir() );
    }
    
    public void testIncludeDirectives()
    {
        assertEquals( "includes", m_refs, m_module.getIncludeDirectives() );
    }
    
    public void testModuleDirectives()
    {
        assertEquals( "modules", m_modules, m_module.getModuleDirectives() );
    }
    
    public void testProjectDirectives()
    {
        assertEquals( "projects", m_projects, m_module.getProjectDirectives() );
    }
    
    public void testResourceDirectives()
    {
        assertEquals( "resources", m_resources, m_module.getResourceDirectives() );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_module );
    }
    
    public void testXMLEncoding() throws Exception
    {
        doEncodingTest( m_module, "module-descriptor-encoded.xml" );
    }
}
