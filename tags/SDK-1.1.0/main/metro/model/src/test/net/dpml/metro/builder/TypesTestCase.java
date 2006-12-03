/*
 * Copyright 2005-2006 Stephen J. McConnell.
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

package net.dpml.metro.builder;

import java.io.File;
import java.net.URI;

import net.dpml.metro.info.Type;
import net.dpml.metro.info.ContextDescriptor;
import net.dpml.metro.info.EntryDescriptor;

import net.dpml.util.Resolver;
import net.dpml.util.SimpleResolver;

import junit.framework.TestCase;

/**
 * Default state machine test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class TypesTestCase extends TestCase
{
    private ComponentTypeDecoder m_builder;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_builder = new ComponentTypeDecoder();
    }
    
   /**
    * List the state graph.
    * @exception Exception if an error occurs
    */
    public void testExampleOne() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "types/example-1.xml" );
        URI uri = example.toURI();
        Resolver resolver = new SimpleResolver();
        Type type = m_builder.loadType( uri, resolver );
        ContextDescriptor context = type.getContextDescriptor();
        EntryDescriptor[] entries = context.getEntryDescriptors();
        if( entries.length != 1 )
        {
            fail( "Invalid entries length - expected 1, found " + entries.length );
        }
        else
        {
            EntryDescriptor entry = entries[0];
            String key = entry.getKey();
            String classname = entry.getClassname();
            boolean optional = entry.isOptional();
            assertEquals( "key", "color", key );
            assertEquals( "classname", "java.awt.Color", classname );
            assertFalse( "optional", optional );
        }
    }
}



