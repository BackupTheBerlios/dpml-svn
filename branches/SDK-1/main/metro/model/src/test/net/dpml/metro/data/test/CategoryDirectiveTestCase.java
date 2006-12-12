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

import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.info.Priority;

/**
 * CategoryDirectiveTestCase
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoryDirectiveTestCase extends AbstractEncodingTestCase
{    
    private String m_name;
    private Priority m_priority;
    private String m_target;
    private CategoryDirective m_directive;

   /**
    * Setup the test case.
    * @exception Exception if an error occurs.
    */
    public void setUp() throws Exception
    {
        m_name = "test";
        m_priority = Priority.ERROR;
        m_target = "target";
        m_directive = new CategoryDirective( m_name, m_priority, m_target );
    }
    
   /**
    * Test the single argument constructor.
    * @exception Exception if an error occurs
    */
    public void testSingleArgConstructor() throws Exception
    {
        CategoryDirective directive = new CategoryDirective( m_name );
        assertEquals( "name", m_name, directive.getName() );
        assertNull( "priority", directive.getPriority() );
        assertNull( "target", directive.getTarget() );
    }
    
   /**
    * Test the dual argument constructor.
    * @exception Exception if an error occurs
    */
    public void testDualArgConstructor() throws Exception
    {
        CategoryDirective directive = new CategoryDirective( m_name, m_priority );
        assertEquals( "name", m_name, directive.getName() );
        assertEquals( "priority", m_priority, directive.getPriority() );
        assertNull( "target", directive.getTarget() );
    }
    
   /**
    * Test the full argument constructor.
    * @exception Exception if an error occurs
    */
    public void testFullConstructor() throws Exception
    {
        CategoryDirective directive = new CategoryDirective( m_name, m_priority, m_target );
        assertEquals( "name", m_name, directive.getName() );
        assertEquals( "priority", m_priority, directive.getPriority() );
        assertEquals( "target", m_target, directive.getTarget() );
    }

   /**
    * Test the name accessor.
    * @exception Exception if an error occurs
    */
    public void testName() throws Exception
    {
        assertEquals( "name", m_name, m_directive.getName() );
    }
    
   /**
    * Test the priority accessor.
    * @exception Exception if an error occurs
    */
    public void testPriority() throws Exception
    {
        assertEquals( "priority", m_priority, m_directive.getPriority() );
    }
    
   /**
    * Test the target accessor.
    * @exception Exception if an error occurs
    */
    public void testTarget() throws Exception
    {
        assertEquals( "target", m_target, m_directive.getTarget() );
    }
    
   /**
    * Test the directive encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        CategoryDirective result = 
          (CategoryDirective) executeEncodingTest( m_directive );
        assertEquals( "encoded-equality", m_directive, result );
    }
}
