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
    private URI m_base;
    
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
        m_base = null;
        m_directive = 
          new ComponentDirective( 
            m_name, m_activation, m_collection, m_lifestyle, m_classname, 
            m_categories, m_context, m_parts, m_base );
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
    * @exception Exception if an error occurs.
    */
    public void testInsufficientName() throws Exception
    {
        try
        {
            new ComponentDirective( 
              "", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null, null );
            fail( "Did not throw an IllegalArgumentException for a '' name." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test "." in name.
    * @exception Exception if an error occurs.
    */
    public void testIllegalPeriodInName() throws Exception
    {
        try
        {
            new ComponentDirective( 
              "fred.blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null, null );
            fail( "Did not throw an IllegalArgumentException for a name with a period." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test "," in name.
    * @exception Exception if an error occurs.
    */
    public void testIllegalCommaInName() throws Exception
    {
        try
        {
            new ComponentDirective( 
              "fred,blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null, null );
            fail( "Did not throw an IllegalArgumentException for a name with a comma." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
   /**
    * Test "/" in name.
    * @exception Exception if an error occurs.
    */
    public void testIllegalFowardSlashInName() throws Exception
    {
        try
        {
            new ComponentDirective( 
              "fred/blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, null, null );
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
    * Test null lifestyle policy returns SYSTEM.
    * @exception Exception if an error occurs.
    */
    public void testNullLifestylePolicy() throws Exception
    {
        ComponentDirective directive = new ComponentDirective( 
            m_name, m_activation, m_collection, null, m_classname, 
            m_categories, m_context, m_parts, null );
        LifestylePolicy lifestyle = directive.getLifestylePolicy();
        assertEquals( "null-lifestyle", LifestylePolicy.SYSTEM, lifestyle );
    }
    
   /**
    * Test classname accessor.
    */
    public void testClassname()
    {
        assertEquals( "classname", m_classname, m_directive.getClassname() );
    }
    
   /**
    * Test base accessor.
    */
    public void testBase()
    {
        assertEquals( "base", m_base, m_directive.getBaseURI() );
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
    * @exception Exception if an error occurs.
    */
    public void testEquality() throws Exception
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
            m_categories, m_context, m_parts, m_base );
            
        ComponentDirective b = new ComponentDirective( 
            m_name, m_activation, m_collection, m_lifestyle, m_classname, 
            m_categories, context, m_parts, m_base );
        
        if( a.equals( b ) )
        {
            fail( "non-equal directives return true for equals" );
        }
    }
}
