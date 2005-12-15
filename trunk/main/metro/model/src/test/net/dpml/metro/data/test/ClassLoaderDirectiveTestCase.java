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

import net.dpml.part.ActivationPolicy;

import net.dpml.transit.Category;

import junit.framework.TestCase;

/**
 * ClasspathDirectiveTestCase
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ClassLoaderDirectiveTestCase extends AbstractEncodingTestCase
{    
    private ClasspathDirective m_public;
    private ClasspathDirective m_protected;
    private ClasspathDirective m_private;
    private ClasspathDirective[] m_classpath;
    private ClassLoaderDirective m_directive;
    
    public void setUp() throws Exception
    {
        m_public = new ClasspathDirective( Category.PUBLIC, new URI[0] );
        m_protected = new ClasspathDirective( Category.PROTECTED, new URI[0] );
        m_private = new ClasspathDirective( Category.PRIVATE, new URI[0] );
        m_classpath = new ClasspathDirective[]{ m_public, m_protected, m_private };
        m_directive = new ClassLoaderDirective( m_classpath );
    }
    
    public void testEncoding() throws Exception
    {
        ClassLoaderDirective result = 
          (ClassLoaderDirective) executeEncodingTest( m_directive, "classloader-directive.xml" );
        assertEquals( "encoded-equality", m_directive, result );
    }
    
    public void testClasspath()
    {
        assertEquals( "classpath", m_classpath.length, m_directive.getClasspathDirectives().length );
    }
    
}
