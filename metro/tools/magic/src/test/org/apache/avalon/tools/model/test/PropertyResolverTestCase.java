/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.tools.model.test;

import java.util.Properties;

import junit.framework.TestCase;
import org.apache.avalon.tools.model.PropertyResolver;


/**
 * Testcases for the PropertyResolver
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $ Revision: 1.1 $
 */
public class PropertyResolverTestCase extends TestCase
{
    private Properties m_Properties;
    
    public void setUp()
    {
        m_Properties = new Properties();
        m_Properties.put( "abc", "def" );
        m_Properties.put( "def", "Hi" );
        m_Properties.put( "mama", "abc" );
        m_Properties.put( "papa", "def" );
        m_Properties.put( "child", "ghi" );
        m_Properties.put( "some.abc.def.ghi.value", "All that." );
    }

    public PropertyResolverTestCase( String name )
    {
        super( name );
    }

    public void testSimple1() throws Exception
    {
        String src = "${abc}";
        String result = PropertyResolver.resolve( m_Properties, src );
        String expected = "def";
        assertEquals( expected, result );
    }

    public void testSimple2() throws Exception
    {
        String src = "Def = ${abc} is it.";
        String result = PropertyResolver.resolve( m_Properties, src );
        String expected = "Def = def is it.";
        assertEquals( expected, result );
    }

    public void testSimple3() throws Exception
    {
        String src = "def = ${abc} = ${def}";
        String result = PropertyResolver.resolve( m_Properties, src );
        String expected = "def = def = Hi";
        assertEquals( expected, result );
    }

    public void testComplex1() throws Exception
    {
        String src = "${${abc}}";
        String result = PropertyResolver.resolve( m_Properties, src );
        String expected = "Hi";
        assertEquals( expected, result );
    }

    public void testComplex2() throws Exception
    {
        String src = "${some.${mama}.${papa}.${child}.value}";
        String result = PropertyResolver.resolve( m_Properties, src );
        String expected = "All that.";
        assertEquals( expected, result );
    }
}
