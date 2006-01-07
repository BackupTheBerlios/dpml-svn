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

import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.info.Priority;

/**
 * CategoriesDirectiveTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoriesDirectiveTestCase extends AbstractEncodingTestCase
{    
    private String m_name;
    private Priority m_priority;
    private String m_target;
    private CategoryDirective[] m_categories;
    private CategoriesDirective m_directive;

   /**
    * Test case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        m_name = "test";
        m_priority = Priority.ERROR;
        m_target = "target";
        m_categories = 
          new CategoryDirective[]
          {
             new CategoryDirective( "aaa", Priority.INFO, null ),
             new CategoryDirective( "bbb", Priority.ERROR, "xml" ),
             new CategoryDirective( "ccc", Priority.WARN, "rabbit" )
          };
        m_directive = new CategoriesDirective( m_name, m_priority, m_target, m_categories );
    }
    
   /**
    * Test the CategoriesDirective( String name ) constructor.
    * @exception Exception if an error occurs
    */
    public void testNameConstructor() throws Exception
    {
        CategoriesDirective directive = new CategoriesDirective( m_name );
        assertEquals( "name", m_name, directive.getName() );
        assertNull( "priority", directive.getPriority() );
        assertNull( "target", directive.getTarget() );
        assertEquals( "categories", 0, directive.getCategories().length );
    }
    
   /**
    * Test the CategoriesDirective( CategoryDirective[] categories ) constructor.
    * @exception Exception if an error occurs
    */
    public void testCategoriesConstructor() throws Exception
    {
        CategoriesDirective directive = new CategoriesDirective( m_categories );
        assertEquals( "name", "", directive.getName() );
        assertNull( "priority", directive.getPriority() );
        assertNull( "target", directive.getTarget() );
        assertEquals( "categories", m_categories.length, directive.getCategories().length );
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
    * Test the categories array accessor.
    * @exception Exception if an error occurs
    */
    public void testCategories() throws Exception
    {
        assertEquals( "categories", m_categories.length, m_directive.getCategories().length );
    }

   /**
    * Test directive encoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        CategoryDirective result = 
          (CategoryDirective) executeEncodingTest( m_directive, "categories-directive.xml" );
        assertEquals( "encoded-equality", m_directive, result );
    }
}
