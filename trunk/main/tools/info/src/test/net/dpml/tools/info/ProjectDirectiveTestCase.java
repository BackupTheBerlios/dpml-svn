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
    public void testNullName()
    {
        try
        {
            ProjectDirective project = 
              new ProjectDirective( null, ".", new ArtifactDirective[0], new DependencyDirective[0] );
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
              new ProjectDirective( "abc", null, new ArtifactDirective[0], new DependencyDirective[0] );
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
              new ProjectDirective( "name", ".", null, new DependencyDirective[0] );
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
              new ProjectDirective( "abc", ".", new ArtifactDirective[0], null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testArtifactDirectives()
    {
        ArtifactDirective[] artifacts = new ArtifactDirective[3];
        artifacts[0] = new ArtifactDirective( "abc" );
        artifacts[1] = new ArtifactDirective( "def" );
        artifacts[0] = new ArtifactDirective( "ghi" );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".", artifacts, new DependencyDirective[0] );
        assertEquals( "artifact-count", 3, project.getArtifactDirectives().length );
    }
    
    public void testDependencyDirectives()
    {
        DependencyDirective[] deps = new DependencyDirective[2];
        deps[0] = new DependencyDirective( Scope.RUNTIME, new ResourceIncludeDirective[0] );
        deps[0] = new DependencyDirective( Scope.TEST, new ResourceIncludeDirective[0] );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".",  new ArtifactDirective[0], deps );
        assertEquals( "deps-count", 2, project.getDependencyDirectives().length );
    }
    
    public void testSerialization() throws Exception
    {
        ArtifactDirective[] artifacts = new ArtifactDirective[3];
        artifacts[0] = new ArtifactDirective( "abc" );
        artifacts[1] = new ArtifactDirective( "def" );
        artifacts[0] = new ArtifactDirective( "ghi" );
        DependencyDirective[] deps = new DependencyDirective[2];
        deps[0] = new DependencyDirective( Scope.RUNTIME, new ResourceIncludeDirective[0] );
        deps[0] = new DependencyDirective( Scope.TEST, new ResourceIncludeDirective[0] );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".", artifacts, deps );
        doSerializationTest( project );
    }
    
    public void testXMLEncoding() throws Exception
    {
        ArtifactDirective[] artifacts = new ArtifactDirective[3];
        artifacts[0] = new ArtifactDirective( "abc" );
        artifacts[1] = new ArtifactDirective( "def" );
        artifacts[0] = new ArtifactDirective( "ghi" );
        DependencyDirective[] deps = new DependencyDirective[2];
        deps[0] = new DependencyDirective( Scope.RUNTIME, new ResourceIncludeDirective[0] );
        deps[0] = new DependencyDirective( Scope.TEST, new ResourceIncludeDirective[0] );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".", artifacts, deps );
        doEncodingTest( project, "project-descriptor-encoded.xml" );
    }
}
