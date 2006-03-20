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

import net.dpml.component.Directive;
import net.dpml.lang.Value;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ValueDirective;

import junit.framework.TestCase;

/**
 * Default state machine test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PartsTestCase extends TestCase
{
    private ComponentDecoder m_builder;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_builder = new ComponentDecoder();
    }
    
   /**
    * List the state graph.
    * @exception Exception if an error occurs
    */
    public void testExampleOne() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "parts/array.xml" );
        URI uri = example.toURI();
        ComponentDirective component = m_builder.loadComponentDirective( uri );
        ContextDirective context = component.getContextDirective();
        PartReference[] entries = context.getDirectives();
        if( entries.length != 1 )
        {
            fail( "Invalid entries length - expected 1, found " + entries.length );
        }
        else
        {
            PartReference entry = entries[0];
            String key = entry.getKey();
            Directive directive = entry.getDirective();
            if( directive instanceof ValueDirective )
            {
                ValueDirective value = (ValueDirective) directive;
                String base = value.getBaseValue();
                String method = value.getMethodName();
                String target = value.getTargetExpression();
                Value[] values = value.getValues();
                //System.out.println( "# TARGET: " + target );
                //System.out.println( "# METHOD: " + method );
                //System.out.println( "# BASE: " + base );
                //System.out.println( "# VALUES: " + values.length );
            }
            else
            {
                fail( "Expected ValueDirective, found " + directive.getClass().getName() );
            }
        }
    }
}



