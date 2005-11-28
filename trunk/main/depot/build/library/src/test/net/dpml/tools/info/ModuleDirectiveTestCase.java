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

package net.dpml.build.info;

import net.dpml.build.info.ResourceDirective.Classifier;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModuleDirectiveTestCase extends AbstractTestCase
{
    static ModuleDirective[] MODULES = new ModuleDirective[3];
    static
    {
        TypeDirective[] types = TypeDirectiveTestCase.TYPES;
        DependencyDirective[] dependencies = DependencyDirectiveTestCase.DEPENDENCIES;
        ResourceDirective[] resources = ResourceDirectiveTestCase.RESOURCES;
        
        MODULES[0] = new ModuleDirective( 
          "aaa", "1.1", ResourceDirective.LOCAL, ".", types, 
          dependencies, resources, PROPERTIES );
        MODULES[1] = new ModuleDirective( 
          "bbb", "1.1", ResourceDirective.LOCAL, ".", types, 
          dependencies, resources, PROPERTIES );
        MODULES[2] = new ModuleDirective( 
          "ccc", "1.1", ResourceDirective.LOCAL, ".", types, 
          dependencies, resources, PROPERTIES );
    }

    private final Classifier m_classifier = ResourceDirective.LOCAL;
    private final String m_name = "name";
    private final String m_version = "1.1.1";
    private final String m_basedir = ".";
    private final TypeDirective[] m_types = TypeDirectiveTestCase.TYPES;
    private final DependencyDirective[] m_dependencies = DependencyDirectiveTestCase.DEPENDENCIES;
    private final ResourceDirective[] m_resources = ResourceDirectiveTestCase.RESOURCES;
    
    private ModuleDirective m_module;
    
    public void setUp() throws Exception
    {
        m_module = 
          new ModuleDirective( 
            m_name, m_version, m_classifier, "test", m_types, m_dependencies, m_resources, PROPERTIES );
    }

    public void testNullName()
    {
        try
        {
            new ModuleDirective( 
              null, m_version, m_classifier, "test", m_types, m_dependencies, m_resources, PROPERTIES );
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
              m_name, null, m_classifier, "test", m_types, m_dependencies, m_resources, PROPERTIES );
        }
        catch( NullPointerException e )
        {
            fail( "NPE" );
        }
    }
    
    public void testNullDependencies()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_classifier, "test", m_types, null, m_resources, PROPERTIES );
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
              m_name, m_version, m_classifier, "test", m_types, m_dependencies, null, PROPERTIES );
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
    
    public void testClassifier()
    {
        assertEquals( "classifier", m_classifier, m_module.getClassifier() );
    }
    
    public void testVersion()
    {
        assertEquals( "version", m_version, m_module.getVersion() );
    }
    
    public void testDependencyDirectives()
    {
        assertEquals( "dependencies", m_dependencies, m_module.getDependencyDirectives() );
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
