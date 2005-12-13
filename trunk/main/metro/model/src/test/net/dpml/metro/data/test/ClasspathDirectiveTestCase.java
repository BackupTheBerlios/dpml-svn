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
 * ClasspathDirectiveTestCase
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ClasspathDirectiveTestCase extends AbstractEncodingTestCase
{    
    private URI m_uriA;
    private URI m_uriB;
    private URI[] m_uris;
    
    private ClasspathDirective m_directive;

    public void setUp() throws Exception
    {
        m_uriA = new URI( "link:part:fred/wilma" );
        m_uriB = new URI( "link:part:batman/robin" );
        m_uris = new URI[]{m_uriA, m_uriB};
        m_directive = new ClasspathDirective( Category.PUBLIC, m_uris );
    }
    
    public void testEncoding() throws Exception
    {
        ClasspathDirective result = 
          (ClasspathDirective) executeEncodingTest( m_directive, "classpath-directive.xml" );
        assertEquals( "encoded-equality", m_directive, result );
    }
    
    public void testCategory()
    {
        assertEquals( "category", Category.PUBLIC, m_directive.getCategory() );
    }
    
    public void testURIs()
    {
        assertEquals( "uris", m_uris.length, m_directive.getURIs().length );
    }
}
