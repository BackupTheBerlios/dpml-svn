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
public final class ListenerDirectiveTestCase extends AbstractTestCase
{
    static ListenerDirective[] LISTENERS = new ListenerDirective[3];
    static
    {
        LISTENERS[0] = new ListenerDirective( "jar" );
        LISTENERS[1] = new ListenerDirective( "acme", "actifact:plugin:acme/whatever", new String[0] );
        LISTENERS[2] = new ListenerDirective( "widget", null, new String[]{ "acme" }, PROPERTIES );
    }
    
    public void testNullName()
    {
        try
        {
            ListenerDirective process = new ListenerDirective( null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullDepends() throws Exception
    {
        try
        {
            ListenerDirective listener = new ListenerDirective( "test", "plugin:whatever", null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testProcessName()
    {
        ListenerDirective process = new ListenerDirective( "test", "plugin:xxx", new String[0], PROPERTIES );
        assertEquals( "name", "test", process.getName() );
    }
    
    public void testProcessURN()
    {
        ListenerDirective process = new ListenerDirective( "test", "plugin:xxx", new String[0], PROPERTIES );
        assertEquals( "urn", "plugin:xxx", process.getURN() );
    }
    
    public void testDependencies()
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ListenerDirective process = new ListenerDirective( "test", "plugin:xxx", deps, PROPERTIES );
        assertEquals( "deps", deps, process.getDependencies() );
    }
    
    public void testSerialization() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ListenerDirective process = new ListenerDirective( "test", "plugin:xxx", deps, PROPERTIES );
        doSerializationTest( process );
    }

    public void testXMLEncoding() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ListenerDirective process = new ListenerDirective( "test", "plugin:xxx", deps, PROPERTIES );
        doEncodingTest( process, "listener-directive.xml" );
    }
}
