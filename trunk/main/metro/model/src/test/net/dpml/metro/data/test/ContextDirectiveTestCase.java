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
public class ContextDirectiveTestCase extends AbstractEncodingTestCase
{
    private ContextDirective m_context;
    
    public void setUp() throws Exception
    {
        m_context = new ContextDirective( new PartReference[0] );
    }
    
    public void testEncoding() throws Exception
    {
        ContextDirective result = 
          (ContextDirective) executeEncodingTest( m_context, "context-directive.xml" );
        assertEquals( "encoded-equality", m_context, result );
    }
    
    public void testClassname()
    {
        assertEquals( "classname", null, m_context.getClassname() );
    }
    
    public void testEntries()
    {
        assertEquals( "entries", 0, m_context.getDirectives().length );
    }
    

}
