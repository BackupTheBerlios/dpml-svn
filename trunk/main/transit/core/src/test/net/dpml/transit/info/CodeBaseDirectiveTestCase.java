/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.transit.info;

import java.net.URI;

/**
 * Testing the CodeBaseDirective class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CodeBaseDirectiveTestCase extends AbstractTestCase
{
    protected String m_codebase;
    protected ValueDirective[] m_values;
    
    public void setUp() throws Exception
    {
        m_codebase = "link:test:whatever";
        ValueDirective v1 = new ValueDirective( "abc" );
        ValueDirective v2 = new ValueDirective( "def" );
        m_values = new ValueDirective[]{v1, v2};
    }
    
    public void testGetCodeBaseURISpec() throws Exception
    {
        CodeBaseDirective directive = new CodeBaseDirective( m_codebase, m_values );
        assertEquals( "codebase-spec", m_codebase, directive.getCodeBaseURISpec() );
    }
    
    public void testGetCodeBaseURI() throws Exception
    {
        URI uri = new URI( m_codebase );
        CodeBaseDirective directive = new CodeBaseDirective( m_codebase, m_values );
        assertEquals( "codebase-spec", uri, directive.getCodeBaseURI() );
    }
    
    public void testGetValues() throws Exception
    {
        CodeBaseDirective directive = new CodeBaseDirective( m_codebase, m_values );
        assertEquals( "values", m_values, directive.getValueDirectives() );
    }
    
    public void testNullCodebaseInConstructor() throws Exception
    {
        try
        {
            CodeBaseDirective directive = new CodeBaseDirective( null, m_values );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullValuesInConstructor() throws Exception
    {
        try
        {
            CodeBaseDirective directive = new CodeBaseDirective( m_codebase, null );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testSerialization() throws Exception
    {
        CodeBaseDirective directive = new CodeBaseDirective( m_codebase, m_values );
        doSerializationTest( directive );
    }
    
    public void testEncoding() throws Exception
    {
        CodeBaseDirective directive = new CodeBaseDirective( m_codebase, m_values );
        CodeBaseDirective result = (CodeBaseDirective) doEncodingTest( directive, "value-directive.xml" );
        assertEquals( "encoded", directive, result );
    }
}
