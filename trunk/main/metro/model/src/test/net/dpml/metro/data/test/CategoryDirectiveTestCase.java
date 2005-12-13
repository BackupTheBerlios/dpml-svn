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

import net.dpml.metro.part.ActivationPolicy;

import net.dpml.transit.Category;

import junit.framework.TestCase;

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

    public void setUp() throws Exception
    {
        m_name = "test";
        m_priority = Priority.ERROR;
        m_target = "target";
        m_directive = new CategoryDirective( m_name, m_priority, m_target );
    }
    
    public void testSingleArgConstructor() throws Exception
    {
        CategoryDirective directive = new CategoryDirective( m_name );
        assertEquals( "name", m_name, directive.getName() );
        assertNull( "priority", directive.getPriority() );
        assertNull( "target", directive.getTarget() );
    }
    
    public void testDualArgConstructor() throws Exception
    {
        CategoryDirective directive = new CategoryDirective( m_name, m_priority );
        assertEquals( "name", m_name, directive.getName() );
        assertEquals( "priority", m_priority, directive.getPriority() );
        assertNull( "target", directive.getTarget() );
    }
    
    public void testFullConstructor() throws Exception
    {
        CategoryDirective directive = new CategoryDirective( m_name, m_priority, m_target );
        assertEquals( "name", m_name, directive.getName() );
        assertEquals( "priority", m_priority, directive.getPriority() );
        assertEquals( "target", m_target, directive.getTarget() );
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
    
    public void testEncoding() throws Exception
    {
        CategoryDirective result = 
          (CategoryDirective) executeEncodingTest( m_directive, "category-directive.xml" );
        assertEquals( "encoded-equality", m_directive, result );
    }
}
