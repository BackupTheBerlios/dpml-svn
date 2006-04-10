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

package net.dpml.library.info;

import net.dpml.library.info.ResourceDirective.Classifier;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ModuleDirectiveTestCase extends AbstractTestCase
{
    static final ModuleDirective[] MODULES = new ModuleDirective[3];
    static
    {
        TypeDirective[] types = TypeDirectiveTestCase.TYPES;
        DependencyDirective[] dependencies = DependencyDirectiveTestCase.DEPENDENCIES;
        ResourceDirective[] resources = ResourceDirectiveTestCase.RESOURCES;
        InfoDirective info = new InfoDirective( "test", "test description" );
        
        MODULES[0] = new ModuleDirective( 
          "aaa", "1.1", ResourceDirective.LOCAL, ".", info, types, 
          dependencies, resources, PROPERTIES, null );
        MODULES[1] = new ModuleDirective( 
          "bbb", "1.1", ResourceDirective.LOCAL, ".", info, types, 
          dependencies, resources, PROPERTIES, null );
        MODULES[2] = new ModuleDirective( 
          "ccc", "1.1", ResourceDirective.LOCAL, ".", info, types, 
          dependencies, resources, PROPERTIES, null );
    }

    private final Classifier m_classifier = ResourceDirective.LOCAL;
    private final String m_name = "name";
    private final String m_version = "1.1.1";
    private final String m_basedir = ".";
    private final InfoDirective m_info = new InfoDirective( "test", "test description" );
    private final TypeDirective[] m_types = TypeDirectiveTestCase.TYPES;
    private final DependencyDirective[] m_dependencies = DependencyDirectiveTestCase.DEPENDENCIES;
    private final ResourceDirective[] m_resources = ResourceDirectiveTestCase.RESOURCES;
    private final FilterDirective[] m_filters = new FilterDirective[0];
    
    private ModuleDirective m_module;
    
   /**
    * Test case setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_module = 
          new ModuleDirective( 
            m_name, m_version, m_classifier, "test", m_info, m_types, m_dependencies, m_resources, PROPERTIES, m_filters );
    }

   /**
    * Validate that the name argument to the module directive 
    * throws an NPE when supplied with a null name.
    */
    public void testNullName()
    {
        try
        {
            new ModuleDirective( 
              null, m_version, m_classifier, "test", m_info, m_types, m_dependencies, m_resources, PROPERTIES, m_filters );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Validate that the module directive constructor
    * throws an NPE when supplied with a null version.
    */
    public void testNullVersion()
    {
        try
        {
            new ModuleDirective( 
              m_name, null, m_classifier, "test", m_info, m_types, m_dependencies, m_resources, PROPERTIES, m_filters );
        }
        catch( NullPointerException e )
        {
            fail( "NPE" );
        }
    }
    
   /**
    * Validate that the module directive constructor
    * throws an NPE when supplied with a null dependencies array.
    */
    public void testNullDependencies()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_classifier, "test", m_info, m_types, null, m_resources, PROPERTIES, m_filters );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Validate that the module directive constructor
    * throws an NPE when supplied with a null resources array.
    */
    public void testNullResources()
    {
        try
        {
            new ModuleDirective( 
              m_name, m_version, m_classifier, "test", m_info, m_types, m_dependencies, null, PROPERTIES, m_filters );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test the name accessor.
    */
    public void testName()
    {
        assertEquals( "name", m_name, m_module.getName() );
    }
    
   /**
    * Test the classifier accessor.
    */
    public void testClassifier()
    {
        assertEquals( "classifier", m_classifier, m_module.getClassifier() );
    }
    
   /**
    * Test the version accessor.
    */
    public void testVersion()
    {
        assertEquals( "version", m_version, m_module.getVersion() );
    }
    
   /**
    * Test the dependency directives accessor.
    */
    public void testDependencyDirectives()
    {
        assertEquals( "dependencies", m_dependencies, m_module.getDependencyDirectives() );
    }
    
   /**
    * Test the resource directives accessor.
    */
    public void testResourceDirectives()
    {
        assertEquals( "resources", m_resources, m_module.getResourceDirectives() );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_module );
    }
}
