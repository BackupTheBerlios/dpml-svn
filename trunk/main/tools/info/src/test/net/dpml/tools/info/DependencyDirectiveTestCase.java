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
            DependencyDirective dep = new DependencyDirective( null, new ResourceIncludeDirective[0] );
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
            DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testRuntimeScope()
    {
        DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, new ResourceIncludeDirective[0] );
        assertEquals( "scope", Scope.RUNTIME, dep.getScope() );
    }
    
    public void testTestScope()
    {
        DependencyDirective dep = new DependencyDirective( Scope.TEST, new ResourceIncludeDirective[0] );
        assertEquals( "scope", Scope.TEST, dep.getScope() );
    }
    
    public void testIncludes()
    {
        ResourceIncludeDirective[] includes = new ResourceIncludeDirective[3];
        includes[0] = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "xzy" );
        includes[1] = new ResourceIncludeDirective( ResourceIncludeDirective.KEY, "123" );
        includes[0] = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "456" );
        DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, includes );
        assertEquals( "includes-count", 3, dep.getIncludeDirectives().length );
    }
    
    public void testSerialization() throws Exception
    {
        ResourceIncludeDirective[] includes = new ResourceIncludeDirective[3];
        includes[0] = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "xzy" );
        includes[1] = new ResourceIncludeDirective( ResourceIncludeDirective.KEY, "123" );
        includes[0] = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "456" );
        DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, includes );
        doSerializationTest( dep );
    }
    
    public void testXMLEncoding() throws Exception
    {
        ResourceIncludeDirective[] includes = new ResourceIncludeDirective[3];
        includes[0] = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "xzy" );
        includes[1] = new ResourceIncludeDirective( ResourceIncludeDirective.KEY, "123" );
        includes[0] = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "456" );
        DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, includes );
        doEncodingTest( dep, "dependency-descriptor-encoded.xml" );
    }
}
