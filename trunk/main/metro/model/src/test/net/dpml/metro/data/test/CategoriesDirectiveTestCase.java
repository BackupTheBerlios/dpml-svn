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

import java.net.URI;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.ClasspathDirective;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Priority;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.DefaultConfiguration;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.part.ActivationPolicy;

import net.dpml.transit.Category;

import junit.framework.TestCase;

/**
 * CategoryDirectiveTestCase
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
    
    public void testNameConstructor() throws Exception
    {
        CategoriesDirective directive = new CategoriesDirective( m_name );
        assertEquals( "name", m_name, directive.getName() );
        assertNull( "priority", directive.getPriority() );
        assertNull( "target", directive.getTarget() );
        assertEquals( "categories", 0, directive.getCategories().length );
    }
    
    public void testCategoriesConstructor() throws Exception
    {
        CategoriesDirective directive = new CategoriesDirective( m_categories );
        assertEquals( "name", "", directive.getName() );
        assertNull( "priority", directive.getPriority() );
        assertNull( "target", directive.getTarget() );
        assertEquals( "categories", m_categories.length, directive.getCategories().length );
    }
    
    public void testName() throws Exception
    {
        assertEquals( "name", m_name, m_directive.getName() );
    }
    
    public void testPriority() throws Exception
    {
        assertEquals( "priority", m_priority, m_directive.getPriority() );
    }
    
    public void testTarget() throws Exception
    {
        assertEquals( "target", m_target, m_directive.getTarget() );
    }
    
    public void testCategories() throws Exception
    {
        assertEquals( "categories", m_categories.length, m_directive.getCategories().length );
    }

    public void testEncoding() throws Exception
    {
        CategoryDirective result = 
          (CategoryDirective) executeEncodingTest( m_directive, "categories-directive.xml" );
        assertEquals( "encoded-equality", m_directive, result );
    }
}
