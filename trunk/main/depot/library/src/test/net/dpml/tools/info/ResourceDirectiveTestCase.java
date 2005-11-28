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
public final class ResourceDirectiveTestCase extends AbstractTestCase
{
    static Classifier CLASSIFIER = ResourceDirective.LOCAL;
    static DependencyDirective[] DEPENDENCIES = DependencyDirectiveTestCase.DEPENDENCIES;
    static TypeDirective[] TYPES = TypeDirectiveTestCase.TYPES;
    
    static ResourceDirective[] RESOURCES = new ResourceDirective[3];
    static
    {
        RESOURCES[0] = new ResourceDirective( "fred", null, CLASSIFIER, "example/fred", TYPES, DEPENDENCIES, PROPERTIES );
        RESOURCES[1] = new ResourceDirective( "george", "1.3.0", CLASSIFIER, "example/george", TYPES, DEPENDENCIES, PROPERTIES );
        RESOURCES[2] = new ResourceDirective( "mary", "2.7", CLASSIFIER, "example/mary", TYPES, DEPENDENCIES, PROPERTIES );
    }

    public void testNullName()
    {
        try
        {
            ResourceDirective resource = 
              new ResourceDirective( null, "1.0", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullTypes()
    {
        try
        {
            ResourceDirective resource = 
              new ResourceDirective( "fred", "1.0", CLASSIFIER, "test", null, DEPENDENCIES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullDependencies()
    {
        try
        {
            ResourceDirective resource = 
              new ResourceDirective( "fred", "1.0", CLASSIFIER, "test", TYPES, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testResourceName()
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        assertEquals( "name", "resource", resource.getName() );
    }
    
    public void testResourceVersion()
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        assertEquals( "version", "2.7", resource.getVersion() );
    }
    
    public void testResourceBasedir()
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        assertEquals( "basedir", "test", resource.getBasedir() );
    }
    
    public void testResourceTypes()
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        assertEquals( "types", 3, resource.getTypeDirectives().length );
    }
    
    public void testDependencyDirectives()
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        assertEquals( "dependencies", 3, resource.getDependencyDirectives().length );
    }
    
    public void testResourceProperties()
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        assertEquals( "properties", PROPERTIES, resource.getProperties() );
    }
    
    public void testSerialization() throws Exception
    {
        ResourceDirective resource = new ResourceDirective( "resource", "2.7", CLASSIFIER, "test", TYPES, DEPENDENCIES, PROPERTIES );
        doSerializationTest( resource );
    }
    
    public void testXMLEncoding() throws Exception
    {
        ResourceDirective resource = 
          new ResourceDirective( 
            "name", "1.1.1", CLASSIFIER, "test", 
            TYPES, 
            DEPENDENCIES,
            PROPERTIES );
        doEncodingTest( resource, "resource-descriptor-encoded.xml" );
    }
}
