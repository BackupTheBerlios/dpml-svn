/* 
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.data.test;

import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.info.PartReference;

/**
 * ContextDirectiveTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextDirectiveTestCase extends AbstractEncodingTestCase
{
    private ContextDirective m_context;
    
   /**
    * Setup the test case.
    * @exception Exception if an error occurs.
    */
    public void setUp() throws Exception
    {
        m_context = new ContextDirective( new PartReference[0] );
    }
    
   /**
    * Test the classname accessor.
    */
    public void testClassname()
    {
        assertEquals( "classname", null, m_context.getClassname() );
    }
    
   /**
    * Test the directive accessor.
    */
    public void testEntries()
    {
        assertEquals( "entries", 0, m_context.getDirectives().length );
    }
    
   /**
    * Test the directive encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        ContextDirective result = 
          (ContextDirective) executeEncodingTest( m_context );
        assertEquals( "encoded-equality", m_context, result );
    }
    
   /**
    * Test the nexted context directive encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testNestedContextEncoding() throws Exception
    {
        ContextDirective c = new ContextDirective( new PartReference[0] );
        PartReference ref = new PartReference( "fred", c );
        ContextDirective directive = new ContextDirective( new PartReference[]{ref} ); 
        ContextDirective result = 
          (ContextDirective) executeEncodingTest( directive );
        assertEquals( "encoded-equality", directive, result );
    }
    
}
