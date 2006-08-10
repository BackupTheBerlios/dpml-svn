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

package net.dpml.metro.info.test;

import net.dpml.metro.info.Descriptor;
import net.dpml.metro.info.CategoryDescriptor;
import net.dpml.metro.info.Priority;

/**
 * CategoryDescriptorTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoryDescriptorTestCase extends AbstractDescriptorTestCase
{
    private final String m_name = "name";
    private final Priority m_priority = Priority.WARN;

   /**
    * Return the category descriptor to test.
    * @return the descriptor
    */
    protected CategoryDescriptor getCategoryDescriptor()
    {
        return new CategoryDescriptor( m_name, m_priority, getProperties() );
    }
    
   /**
    * Test the category name accessor.
    */
    public void testName()
    {
        CategoryDescriptor category = getCategoryDescriptor();
        assertEquals( m_name, category.getName() );
    }

   /**
    * Test the category priority accessor.
    */
    public void testPriority()
    {
        CategoryDescriptor category = getCategoryDescriptor();
        assertEquals( m_priority, category.getDefaultPriority() );
    }

   /**
    * Test the category properties accessor.
    */
    public void testProperties()
    {
        CategoryDescriptor category = getCategoryDescriptor();
        assertEquals( getProperties() , category.getProperties() );
    }

   /**
    * Return the category descriptor to test.
    * @return the descriptor
    */
    protected Descriptor getDescriptor()
    {
        return getCategoryDescriptor();
    }
    
   /**
    * Validate the category descriptor.
    * @param desc the descriptor to validate
    */
    protected void checkDescriptor( Descriptor desc )
    {
        super.checkDescriptor( desc );
        CategoryDescriptor cat = (CategoryDescriptor) desc;
        assertEquals( "name", m_name, cat.getName() );
        assertEquals( "priority", m_priority, cat.getDefaultPriority() );
    }
   
   /**
    * Test encoding/decoding of the category descriptor.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        CategoryDescriptor descriptor = getCategoryDescriptor();
        executeEncodingTest( descriptor, "category.xml" );
    }
}
