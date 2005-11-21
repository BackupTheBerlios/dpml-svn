/*
 * Copyright 2004 Apache Software Foundation
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.test;

import junit.framework.TestCase;

import java.util.Properties;

import net.dpml.transit.util.PropertyResolver;

/**
 * Testcases for the PropertyResolver
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version CVS $ Revision: 1.1 $
 */
public class PropertyResolverTestCase extends TestCase
{
    private Properties m_properties;

    public void setUp()
    {
        m_properties = new Properties();
        m_properties.put( "abc", "def" );
        m_properties.put( "def", "Hi" );
        m_properties.put( "mama", "abc" );
        m_properties.put( "papa", "def" );
        m_properties.put( "child", "ghi" );
        m_properties.put( "some.abc.def.ghi.value", "All that." );
    }

    public PropertyResolverTestCase( String name )
    {
        super( name );
    }

    public void testSimple1() throws Exception
    {
        String src = "${abc}";
        String result = PropertyResolver.resolve( m_properties, src );
        String expected = "def";
        assertEquals( expected, result );
    }

    public void testSimple2() throws Exception
    {
        String src = "Def = ${abc} is it.";
        String result = PropertyResolver.resolve( m_properties, src );
        String expected = "Def = def is it.";
        assertEquals( expected, result );
    }

    public void testSimple3() throws Exception
    {
        String src = "def = ${abc} = ${def}";
        String result = PropertyResolver.resolve( m_properties, src );
        String expected = "def = def = Hi";
        assertEquals( expected, result );
    }

    public void testComplex1() throws Exception
    {
        String src = "${${abc}}";
        String result = PropertyResolver.resolve( m_properties, src );
        String expected = "Hi";
        assertEquals( expected, result );
    }

    public void testComplex2() throws Exception
    {
        String src = "${some.${mama}.${papa}.${child}.value}";
        String result = PropertyResolver.resolve( m_properties, src );
        String expected = "All that.";
        assertEquals( expected, result );
    }
}
