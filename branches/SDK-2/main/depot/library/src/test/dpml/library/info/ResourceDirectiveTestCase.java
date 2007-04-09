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

package dpml.library.info;

import dpml.library.Classifier;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ResourceDirectiveTestCase extends AbstractTestCase
{
    static final InfoDirective INFO = new InfoDirective( "test", "test description" );
    static final Classifier CLASSIFIER = ResourceDirective.LOCAL;
    static final DependencyDirective[] DEPENDENCIES = DependencyDirectiveTestCase.DEPENDENCIES;
    static final TypeDirective[] TYPES = TypeDirectiveTestCase.TYPES;
    static final ResourceDirective[] RESOURCES = new ResourceDirective[3];
    static final TypeDirective[] DATA = new TypeDirective[0];
    static final FilterDirective[] FILTERS = new FilterDirective[0];
    
    static
    {
        RESOURCES[0] = 
          new ResourceDirective( 
            "fred", null, CLASSIFIER, "example/fred", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        RESOURCES[1] = 
          new ResourceDirective( 
            "george", "1.3.0", CLASSIFIER, "example/george", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, false );
        RESOURCES[2] = 
          new ResourceDirective( 
            "mary", "2.7", CLASSIFIER, "example/mary", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
    }
   
   /**
    * Validate that the resource directive constructor
    * throws an NPE when supplied with a null name.
    */
    public void testNullName()
    {
        try
        {
            ResourceDirective resource = 
              new ResourceDirective( 
                null, "1.0", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Validate that the resource directive constructor
    * throws an NPE when supplied with a null type array.
    */
    public void testNullTypes()
    {
        try
        {
            ResourceDirective resource = 
              new ResourceDirective( 
                "fred", "1.0", CLASSIFIER, "test", INFO, null, DEPENDENCIES, PROPERTIES, FILTERS, true );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Validate that the resource directive constructor
    * throws an NPE when supplied with a null dependencies array.
    */
    public void testNullDependencies()
    {
        try
        {
            ResourceDirective resource = 
              new ResourceDirective( 
                "fred", "1.0", CLASSIFIER, "test", INFO, TYPES, null, PROPERTIES, FILTERS, true );
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
    public void testResourceName()
    {
        ResourceDirective resource = 
          new ResourceDirective(
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        assertEquals( "name", "resource", resource.getName() );
    }
    
   /**
    * Test the version accessor.
    */
    public void testResourceVersion()
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        assertEquals( "version", "2.7", resource.getVersion() );
    }
    
   /**
    * Test the basedir accessor.
    */
    public void testResourceBasedir()
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        assertEquals( "basedir", "test", resource.getBasedir() );
    }
    
   /**
    * Test the types array accessor.
    */
    public void testResourceTypes()
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        assertEquals( "types", 3, resource.getTypeDirectives().length );
    }
    
   /**
    * Test the dependencies array accessor.
    */
    public void testDependencyDirectives()
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        assertEquals( "dependencies", 3, resource.getDependencyDirectives().length );
    }
    
   /**
    * Test the properties accessor.
    */
    public void testResourceProperties()
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        assertEquals( "properties", PROPERTIES, resource.getProperties() );
    }
    
   /**
    * Test the directive serailization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "resource", "2.7", CLASSIFIER, "test", INFO, TYPES, DEPENDENCIES, PROPERTIES, FILTERS, true );
        doSerializationTest( resource );
    }
}
