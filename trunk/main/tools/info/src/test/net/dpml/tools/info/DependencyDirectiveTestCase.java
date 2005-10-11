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
public final class DependencyDirectiveTestCase extends AbstractTestCase
{
    public void testNullScope()
    {
        try
        {
            DependencyDirective dep = new DependencyDirective( null, new IncludeDirective[0] );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullIncludes()
    {
        try
        {
            DependencyDirective dep = new DependencyDirective( "abc", null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testScope()
    {
        DependencyDirective dep = new DependencyDirective( "runtime", new IncludeDirective[0] );
        assertEquals( "scope", "runtime", dep.getScope() );
    }
    
    public void testIncludes()
    {
        IncludeDirective[] includes = new IncludeDirective[3];
        includes[0] = new IncludeDirective( "abc", "xzy" );
        includes[1] = new IncludeDirective( "def", "123" );
        includes[0] = new IncludeDirective( "ghi", "456" );
        DependencyDirective dep = new DependencyDirective( "runtime", includes );
        assertEquals( "includes-count", 3, dep.getIncludeDirectives().length );
    }
    
    public void testSerialization() throws Exception
    {
        IncludeDirective[] includes = new IncludeDirective[3];
        includes[0] = new IncludeDirective( "abc", "xzy" );
        includes[1] = new IncludeDirective( "def", "123" );
        includes[0] = new IncludeDirective( "ghi", "456" );
        DependencyDirective dep = new DependencyDirective( "runtime", includes );
        doSerializationTest( dep );
    }
    
    public void testXMLEncoding() throws Exception
    {
        IncludeDirective[] includes = new IncludeDirective[3];
        includes[0] = new IncludeDirective( "abc", "xzy" );
        includes[1] = new IncludeDirective( "def", "123" );
        includes[0] = new IncludeDirective( "ghi", "456" );
        DependencyDirective dep = new DependencyDirective( "runtime", includes );
        doEncodingTest( dep, "dependency-descriptor-encoded.xml" );
    }
}
