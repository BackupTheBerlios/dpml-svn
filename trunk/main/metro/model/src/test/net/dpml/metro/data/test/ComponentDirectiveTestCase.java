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

import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.DefaultConfiguration;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.metro.part.ActivationPolicy;

import net.dpml.transit.Category;

import junit.framework.TestCase;

/**
 * ComponentDirectiveTestCase
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ContextDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
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
    private Parameters m_parameters;
    private Configuration m_configuration;
    private ClassLoaderDirective m_classloader;
    private ComponentDirective m_directive;
    
    public void setUp() throws Exception
    {
        m_name = "test";
        m_activation = ActivationPolicy.STARTUP;
        m_collection = CollectionPolicy.WEAK;
        m_lifestyle = LifestylePolicy.SINGLETON;
        m_classname = ComponentDirectiveTestCase.class.getName();
        m_categories = new CategoriesDirective( new CategoryDirective[0] );
        m_context = new ContextDirective( new PartReference[0] );
        m_parameters = DefaultParameters.EMPTY_PARAMETERS;
        m_configuration = new DefaultConfiguration( "" );
        m_classloader = createClassLoaderDirective();
        m_directive = 
          new ComponentDirective( 
            m_name, m_activation, m_collection, m_lifestyle, m_classname, 
            m_categories, m_context, m_parameters, m_configuration, m_classloader );
    }
    
    private ClassLoaderDirective createClassLoaderDirective()
    {
        ClasspathDirective[] paths = new ClasspathDirective[3];
        paths[0] = new ClasspathDirective( Category.PUBLIC, new URI[0] );
        paths[1] = new ClasspathDirective( Category.PROTECTED, new URI[0] );
        paths[2] = new ClasspathDirective( Category.PRIVATE, new URI[0] );
        return new ClassLoaderDirective( paths );
    }
    
    public void testEncoding() throws Exception
    {
        ComponentDirective result = 
          (ComponentDirective) executeEncodingTest( m_directive, "component-directive.xml" );
        assertEquals( "encoded-equality", m_directive, result );
    }
    
    public void testName()
    {
        assertEquals( "name", m_name, m_directive.getName() );
    }
    
    public void testUnsufficientName()
    {
        try
        {
            new ComponentDirective( 
              "", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, m_parameters, m_configuration, m_classloader );
            fail( "Did not throw an IllegalArgumentException for a '' name." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
    public void testIllegalPeriodInName()
    {
        try
        {
            new ComponentDirective( 
              "fred.blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, m_parameters, m_configuration, m_classloader );
            fail( "Did not throw an IllegalArgumentException for a name with a period." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
    public void testIllegalCommaInName()
    {
        try
        {
            new ComponentDirective( 
              "fred,blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, m_parameters, m_configuration, m_classloader );
            fail( "Did not throw an IllegalArgumentException for a name with a comma." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
    public void testIllegalFowardSlashInName()
    {
        try
        {
            new ComponentDirective( 
              "fred/blogs", m_activation, m_collection, m_lifestyle, m_classname, 
              m_categories, m_context, m_parameters, m_configuration, m_classloader );
            fail( "Did not throw an IllegalArgumentException for a name with a '/'." ); 
        }
        catch( IllegalArgumentException e )
        {
            // ok
        }
    }
    
    public void testActivationPolicy()
    {
        assertEquals( "activation", m_activation, m_directive.getActivationPolicy() );
    }
    
    public void testCollectionPolicy()
    {
        assertEquals( "collection", m_collection, m_directive.getCollectionPolicy() );
    }
    
    public void testLifestylePolicy()
    {
        assertEquals( "lifestyle", m_lifestyle, m_directive.getLifestylePolicy() );
    }
    
    public void testClassname()
    {
        assertEquals( "classname", m_classname, m_directive.getClassname() );
    }
    
    public void testCategories()
    {
        assertEquals( "categories", m_categories, m_directive.getCategoriesDirective() );
    }
    
    public void testContext()
    {
        assertEquals( "context", m_context, m_directive.getContextDirective() );
    }
    
    public void testParameters()
    {
        assertEquals( "parameters", m_parameters, m_directive.getParameters() );
    }
    
    public void testConfiguration()
    {
        assertEquals( "configuration", m_configuration, m_directive.getConfiguration() );
    }
    
    public void testClassLoaderDirective()
    {
        assertEquals( "classloader", m_classloader, m_directive.getClassLoaderDirective() );
    }
}
