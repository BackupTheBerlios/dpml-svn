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

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.PartReference;

import net.dpml.component.ActivationPolicy;

/**
 * ComponentDirectiveTestCase
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentDirectiveTestCase extends AbstractEncodingTestCase
{
    private String m_name;
    private ActivationPolicy m_activation;
    private CollectionPolicy m_collection;
    private LifestylePolicy m_lifestyle;
    private String m_classname;
    private CategoriesDirective m_categories;
    private ContextDirective m_context;
    private ComponentDirective m_directive;
    private PartReference[] m_parts;
    
   /**
    * Setup the test case.
    * @exception Exception if an error occurs.
    */
    public void setUp() throws Exception
    {
        m_name = "test";
        m_activation = ActivationPolicy.STARTUP;
        m_collection = CollectionPolicy.WEAK;
        m_lifestyle = LifestylePolicy.SINGLETON;
        m_classname = ComponentDirectiveTestCase.class.getName();
        m_categories = new CategoriesDirective( new CategoryDirective[0] );
        m_context = 
          new ContextDirective( 
            new PartReference[]
            {
                new PartReference( "abc", new ValueDirective( "abc" ) ),
                new PartReference( "def", new ValueDirective( "def" ) )
            }
          );
        m_parts = new PartReference[0];
        m_directive = 
          new ComponentDirective( 
            m_name, m_activation, m_collection, m_lifestyle, m_classname, 
            m_categories, m_context, m_parts );
    }
    
   /**
    * Test the directive encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        ComponentDirective result = 
          (ComponentDirective) executeEncodingTest( m_directive );
        assertEquals( "encoded-equality", m_directive, result );
    }
    
   /**
    * Test the name accessor.
    */
    public void testName()
    {
        assertEquals( "name", m_name, m_directive.getName() );
    }
    
   /**
    * Test "" name.
    */
    public void testInsufficientName()
    {
        try
        {
            new ComponentDirective( 
              "", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null );
            fail( "Did not throw an IllegalArgumentException for a '' name." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test "." in name.
    */
    public void testIllegalPeriodInName()
    {
        try
        {
            new ComponentDirective( 
              "fred.blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null );
            fail( "Did not throw an IllegalArgumentException for a name with a period." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test "," in name.
    */
    public void testIllegalCommaInName()
    {
        try
        {
            new ComponentDirective( 
              "fred,blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null );
            fail( "Did not throw an IllegalArgumentException for a name with a comma." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test "/" in name.
    */
    public void testIllegalFowardSlashInName()
    {
        try
        {
            new ComponentDirective( 
              "fred/blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null );
            fail( "Did not throw an IllegalArgumentException for a name with a '/'." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test activation policy accessor.
    */
    public void testActivationPolicy()
    {
        assertEquals( "activation", m_activation, m_directive.getActivationPolicy() );
    }
    
   /**
    * Test collection policy accessor.
    */
    public void testCollectionPolicy()
    {
        assertEquals( "collection", m_collection, m_directive.getCollectionPolicy() );
    }
    
   /**
    * Test lifestyle policy accessor.
    */
    public void testLifestylePolicy()
    {
        assertEquals( "lifestyle", m_lifestyle, m_directive.getLifestylePolicy() );
    }
    
   /**
    * Test null lifestyle policy returns null.
    */
    public void testNullLifestylePolicy()
    {
        ComponentDirective directive = new ComponentDirective( 
            m_name, m_activation, m_collection, null, m_classname, 
            m_categories, m_context, m_parts );
        LifestylePolicy lifestyle = directive.getLifestylePolicy();
        assertEquals( "null-lifestyle", null, lifestyle );
    }
    
   /**
    * Test classname accessor.
    */
    public void testClassname()
    {
        assertEquals( "classname", m_classname, m_directive.getClassname() );
    }
    
   /**
    * Test categories accessor.
    */
    public void testCategories()
    {
        assertEquals( "categories", m_categories, m_directive.getCategoriesDirective() );
    }
    
   /**
    * Test context accessor.
    */
    public void testContext()
    {
        assertEquals( "context", m_context, m_directive.getContextDirective() );
    }
    
   /**
    * Test equality.
    */
    public void testEquality()
    {
        ContextDirective context = 
          new ContextDirective( 
            new PartReference[]
            {
                new PartReference( "abc", new ValueDirective( "abc" ) ),
                new PartReference( "def", new ValueDirective( "xyz" ) )
            }
          );
          
        ComponentDirective a = new ComponentDirective( 
            m_name, m_activation, m_collection, m_lifestyle, m_classname, 
            m_categories, m_context, m_parts );
            
        ComponentDirective b = new ComponentDirective( 
            m_name, m_activation, m_collection, m_lifestyle, m_classname, 
            m_categories, context, m_parts );
        
        if( a.equals( b ) )
        {
            fail( "non-equal directives return true for equals" );
        }
    }
}
