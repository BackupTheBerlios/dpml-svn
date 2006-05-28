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
    static final String NAME = "test";
    static final int PRIORITY = 0;
    static final String SPEC = "local:plugin:acme/widget";
    static final String CLASSNAME = "net.dpml.tools.process.JarProcess";
    static final String[] DEPS = new String[0];
    static final ListenerDirective[] LISTENERS = new ListenerDirective[3];
    
    static
    {
        try
        {
            LISTENERS[0] = 
              new ListenerDirective( 
                "jar", 0, (URI) null, CLASSNAME, DEPS, PROPERTIES );
            LISTENERS[1] = 
              new ListenerDirective( 
                "acme", 1, new URI( SPEC ), null, DEPS, PROPERTIES );
            LISTENERS[2] = 
              new ListenerDirective( 
                "widget", 2, (URI) null, CLASSNAME, new String[]{"acme"}, PROPERTIES );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
    
   /**
    * Validate that the listener directive constructor throws an NPE 
    * when supplied with a null name.
    * @exception Exception if an error occurs
    */
    public void testNullName() throws Exception
    {
        try
        {
            new ListenerDirective( 
                null, 0, new URI( SPEC ), CLASSNAME, DEPS, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Validate that the listener directive constructor throws an NPE 
    * when supplied with a null depends argument.
    * @exception Exception if an error occurs
    */
    public void testNullDepends() throws Exception
    {
        try
        {
            new ListenerDirective( 
                NAME, 0, new URI( SPEC ), CLASSNAME, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test name accessor.
    * @exception Exception if an error occurs
    */
    public void testProcessName() throws Exception
    {
        ListenerDirective process = 
          new ListenerDirective( 
              NAME, 0, new URI( SPEC ), CLASSNAME, DEPS, PROPERTIES );
        assertEquals( "name", NAME, process.getName() );
    }
    
   /**
    * Test uri spec accessor.
    * @exception Exception if an error occurs
    */
    public void testProcessURN() throws Exception
    {
        ListenerDirective process = 
          new ListenerDirective( 
              NAME, 0, new URI( SPEC ), CLASSNAME, DEPS, PROPERTIES );
        assertEquals( "uri", SPEC, process.getURISpec() );
    }
    
   /**
    * Test dependencies accessor.
    * @exception Exception if an error occurs
    */
    public void testDependencies() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{dep1, dep2};
        ListenerDirective process = 
          new ListenerDirective( NAME, 0, new URI( SPEC ), CLASSNAME, deps, PROPERTIES );
        assertEquals( "deps", deps, process.getDependencies() );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        String dep1 = "abc";
        String dep2 = "def";
        String[] deps = new String[]{dep1, dep2};
        ListenerDirective process = 
          new ListenerDirective( NAME, 0, new URI( SPEC ), CLASSNAME, deps, PROPERTIES );
        doSerializationTest( process );
    }

}
