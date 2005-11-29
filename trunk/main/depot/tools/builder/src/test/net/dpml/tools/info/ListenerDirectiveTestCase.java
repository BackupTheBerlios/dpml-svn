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

import java.net.URI;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ListenerDirectiveTestCase extends AbstractTestCase
{
    static String NAME = "test";
    static String SPEC = "local:plugin:acme/widget";
    static String CLASSNAME = "net.dpml.tools.process.JarProcess";
    static String[] DEPS = new String[0];
    
    static ListenerDirective[] LISTENERS = new ListenerDirective[3];
    static
    {
        try
        {
            LISTENERS[0] = 
              new ListenerDirective( 
                "jar", (URI) null, CLASSNAME, DEPS, PROPERTIES );
            LISTENERS[1] = 
              new ListenerDirective( 
                "acme", new URI( SPEC ), null, DEPS, PROPERTIES );
            LISTENERS[2] = 
              new ListenerDirective( 
                "widget", (URI) null, CLASSNAME, new String[]{ "acme" }, PROPERTIES );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
    
    public void testNullName() throws Exception
    {
        try
        {
            new ListenerDirective( 
                null, new URI( SPEC ), CLASSNAME, DEPS, PROPERTIES );
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
            new ListenerDirective( 
                NAME, new URI( SPEC ), CLASSNAME, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testProcessName() throws Exception
    {
        ListenerDirective process = 
          new ListenerDirective( 
              NAME, new URI( SPEC ), CLASSNAME, DEPS, PROPERTIES );
        assertEquals( "name", NAME, process.getName() );
    }
    
    public void testProcessURN() throws Exception
    {
        ListenerDirective process = 
          new ListenerDirective( 
              NAME, new URI( SPEC ), CLASSNAME, DEPS, PROPERTIES );
        assertEquals( "uri", SPEC, process.getURISpec() );
    }
    
    public void testDependencies() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ListenerDirective process = 
          new ListenerDirective( NAME, new URI( SPEC ), CLASSNAME, deps, PROPERTIES );
        assertEquals( "deps", deps, process.getDependencies() );
    }
    
    public void testSerialization() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{ dep1, dep2 };
        ListenerDirective process = 
          new ListenerDirective( NAME, new URI( SPEC ), CLASSNAME, deps, PROPERTIES );
        doSerializationTest( process );
    }

}
