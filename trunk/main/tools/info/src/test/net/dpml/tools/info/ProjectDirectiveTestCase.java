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
public final class ProjectDirectiveTestCase extends AbstractTestCase
{
    public void testNullName()
    {
        try
        {
            ProjectDirective project = 
              new ProjectDirective( null, ".", new TypeDirective[0], new DependencyDirective[0] );
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
              new ProjectDirective( "abc", null, new TypeDirective[0], new DependencyDirective[0] );
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
              new ProjectDirective( "abc", ".", new TypeDirective[0], null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testTypeDirectives()
    {
        TypeDirective[] types = new TypeDirective[3];
        types[0] = new TypeDirective( "abc" );
        types[1] = new TypeDirective( "def" );
        types[0] = new TypeDirective( "ghi" );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".", types, new DependencyDirective[0] );
        assertEquals( "type-count", 3, project.getTypeDirectives().length );
    }
    
    public void testDependencyDirectives()
    {
        DependencyDirective[] deps = new DependencyDirective[3];
        deps[0] = new DependencyDirective( "build", new IncludeDirective[0] );
        deps[0] = new DependencyDirective( "test", new IncludeDirective[0] );
        deps[0] = new DependencyDirective( "runtime", new IncludeDirective[0] );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".",  new TypeDirective[0], deps );
        assertEquals( "deps-count", 3, project.getDependencyDirectives().length );
    }
    
    public void testSerialization() throws Exception
    {
        TypeDirective[] types = new TypeDirective[3];
        types[0] = new TypeDirective( "abc" );
        types[1] = new TypeDirective( "def" );
        types[0] = new TypeDirective( "ghi" );
        DependencyDirective[] deps = new DependencyDirective[3];
        deps[0] = new DependencyDirective( "build", new IncludeDirective[0] );
        deps[0] = new DependencyDirective( "test", new IncludeDirective[0] );
        deps[0] = new DependencyDirective( "runtime", new IncludeDirective[0] );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".", types, deps );
        doSerializationTest( project );
    }
    
    public void testXMLEncoding() throws Exception
    {
        TypeDirective[] types = new TypeDirective[3];
        types[0] = new TypeDirective( "abc" );
        types[1] = new TypeDirective( "def" );
        types[0] = new TypeDirective( "ghi" );
        DependencyDirective[] deps = new DependencyDirective[3];
        deps[0] = new DependencyDirective( "build", new IncludeDirective[0] );
        deps[0] = new DependencyDirective( "test", new IncludeDirective[0] );
        deps[0] = new DependencyDirective( "runtime", new IncludeDirective[0] );
        ProjectDirective project = 
           new ProjectDirective( "abc", ".", types, deps );
        doEncodingTest( project, "project-descriptor-encoded.xml" );
    }
}
