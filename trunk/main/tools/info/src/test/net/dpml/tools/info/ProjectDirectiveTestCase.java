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
 * Test general integrity of a project directive.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ProjectDirectiveTestCase extends AbstractTestCase
{
    static ProductionDirective[] ARTIFACTS = new ProductionDirective[3];
    static
    {
        ARTIFACTS[0] = new ProductionDirective( "abc", true, PROPERTIES );
        ARTIFACTS[1] = new ProductionDirective( "123", false, PROPERTIES );
        ARTIFACTS[2] = new ProductionDirective( "456", true, PROPERTIES );
    }
    static DependencyDirective[] DEPENDENCIES = new DependencyDirective[2];
    static
    {
        DEPENDENCIES[0] = new DependencyDirective( Scope.RUNTIME, new TaggedIncludeDirective[0], PROPERTIES );
        DEPENDENCIES[1] = new DependencyDirective( Scope.TEST, new TaggedIncludeDirective[0], PROPERTIES );
    }
    
    static ProjectDirective[] PROJECTS = new ProjectDirective[3];
    static
    {
        PROJECTS[0] = new ProjectDirective( "abc", null, ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        PROJECTS[1] = new ProjectDirective( "def", "1.0", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        PROJECTS[2] = new ProjectDirective( "ghi", "2.3.4", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
    }
    
    public void testNullName()
    {
        try
        {
            ProjectDirective project = 
              new ProjectDirective( 
                null, "1.0", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullBasedir()
    {
        try
        {
            ProjectDirective project = 
              new ProjectDirective( 
                "abc", "1.0", null, ARTIFACTS, DEPENDENCIES, PROPERTIES );
        }
        catch( NullPointerException e )
        {
            fail( "NPE should not be thrown" );
        }
    }
    
    public void testNullVersion()
    {
        try
        {
            ProjectDirective project = 
              new ProjectDirective( 
                "abc", null, "dir", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        }
        catch( NullPointerException e )
        {
            fail( "NPE should not be thrown" );
        }
    }
    
    public void testNullTypes()
    {
        try
        {
            ProjectDirective project = 
              new ProjectDirective( "name", "1.0", ".", null, DEPENDENCIES, PROPERTIES );
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
            ProjectDirective project = 
              new ProjectDirective( "abc", "1.0", ".", ARTIFACTS, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testProductionDirectives()
    {
        ProjectDirective project = 
           new ProjectDirective( "abc", "1.0", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        assertEquals( "artifact-count", 3, project.getProductionDirectives().length );
    }
    
    public void testDependencyDirectives()
    {
        ProjectDirective project = 
           new ProjectDirective( "abc", "1.0", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        assertEquals( "deps-count", 2, project.getDependencyDirectives().length );
    }
    
    public void testSerialization() throws Exception
    {
        ProjectDirective project = 
           new ProjectDirective( "abc", "1.0", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        doSerializationTest( project );
    }
    
    public void testXMLEncoding() throws Exception
    {
        ProjectDirective project = 
           new ProjectDirective( "abc", "1.0", ".", ARTIFACTS, DEPENDENCIES, PROPERTIES );
        doEncodingTest( project, "project-descriptor-encoded.xml" );
    }
}
