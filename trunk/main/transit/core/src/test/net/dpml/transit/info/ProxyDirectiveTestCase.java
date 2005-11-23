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

import java.util.Arrays;

/**
 * Testing the CodeBaseDirective class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ProxyDirectiveTestCase extends AbstractTestCase
{
    private String m_host;
    private String[] m_excludes;
    private String m_username;
    private char[] m_password;
    
    public void setUp() throws Exception
    {
        m_host = "http://localhost:1234";
        m_excludes = new String[]{ "http://www.gotham-city.net" };
        m_username = "batman";
        m_password = new char[]{'r','o','b','i','n'};
    }
    
    public void testHost() throws Exception
    {
        ProxyDirective directive = new ProxyDirective( m_host, m_excludes, m_username, m_password );
        assertEquals( "host", m_host, directive.getHost() );
    }
    
    public void testExcludes() throws Exception
    {
        ProxyDirective directive = new ProxyDirective( m_host, m_excludes, m_username, m_password );
        assertEquals( "excludes", m_excludes, directive.getExcludes() );
    }
    
    public void testUsername() throws Exception
    {
        ProxyDirective directive = new ProxyDirective( m_host, m_excludes, m_username, m_password );
        assertEquals( "username", m_username, directive.getUsername() );
    }
    
    public void testPassword() throws Exception
    {
        ProxyDirective directive = new ProxyDirective( m_host, m_excludes, m_username, m_password );
        assertEquals( "password", m_password, directive.getPassword() );
    }
    
    public void testAllowableNullArguments() throws Exception
    {
        String host = "http://localhost:1234";
        String[] excludes = null;
        String username = null;
        char[] password = null;
        ProxyDirective directive = new ProxyDirective( host, excludes, username, password );
        assertEquals( "host", host, directive.getHost() );
        assertEquals( "excludes", 0, directive.getExcludes().length );
        assertEquals( "username", username, directive.getUsername() );
        assertEquals( "password", password, directive.getPassword() );
    }
    
    public void testNullHost() throws Exception
    {
        String host = null;
        String[] excludes = new String[]{ "http://www.osm.net" };
        String username = "me";
        char[] password = new char[0];
        try
        {
            ProxyDirective directive = new ProxyDirective( host, excludes, username, password );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testClassicSerialization() throws Exception
    {
        String host = "http://localhost:1234";
        String[] excludes = new String[]{ "http://www.osm.net" };
        String username = "me";
        char[] password = new char[0];
        ProxyDirective directive = new ProxyDirective( host, excludes, username, password );
        doSerializationTest( directive );
    }
    
    public void testClassicEncoding() throws Exception
    {
        String host = "http://localhost:1234";
        String[] excludes = new String[]{ "http://www.osm.net" };
        String username = "me";
        char[] password = new char[0];
        ProxyDirective directive = new ProxyDirective( host, excludes, username, password );
        ProxyDirective result = (ProxyDirective) doEncodingTest( directive, "proxy-directive.xml" );
        assertEquals( "encoded", directive, result );
    }

    public void testNullSerialization() throws Exception
    {
        String host = "http://localhost:1234";
        String[] excludes = null;
        String username = null;
        char[] password = null;
        ProxyDirective directive = new ProxyDirective( host, excludes, username, password );
        doSerializationTest( directive );
    }
    
    public void testNullEncoding() throws Exception
    {
        String host = "http://localhost:1234";
        String[] excludes = null;
        String username = null;
        char[] password = null;
        ProxyDirective directive = new ProxyDirective( host, excludes, username, password );
        ProxyDirective result = (ProxyDirective) doEncodingTest( directive, "proxy-directive.xml" );
        assertEquals( "encoded", directive, result );
    }
}
