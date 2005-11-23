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
public final class HostDirectiveTestCase extends AbstractTestCase
{
    private String m_id = "test";
    private int m_priority = 100;
    private String m_host = "http://repository.dpml.net/classic";
    private String m_index = null;
    private String m_username = null;
    private char[] m_password = null;
    private boolean m_enabled = true;
    private boolean m_trusted = true;
    private String m_layout = "classic";
    private String m_scheme = "";
    private String m_prompt = "";
    
    public void testID() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "id", m_id, directive.getID() );
    }
    
    public void testPriority() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "priority", m_priority, directive.getPriority() );
    }
    
    public void testHost() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "host", m_host, directive.getHost() );
    }
    
    public void testIndex() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "index", m_index, directive.getIndex() );
    }
    
    public void testUsername() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "username", m_username, directive.getUsername() );
    }
    
    public void testPassword() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "password", m_password, directive.getPassword() );
    }
    
    public void testLayout() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "layout", m_layout, directive.getLayout() );
    }
    
    public void testEnabled() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, true, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertTrue( "enabled", directive.getEnabled() );
        directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, false, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertFalse( "enabled", directive.getEnabled() );
    }
    
    public void testTrusted() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, true, 
            m_layout, m_scheme, m_prompt );
        assertTrue( "trusted", directive.getTrusted() );
        directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, false, 
            m_layout, m_scheme, m_prompt );
        assertFalse( "trusted", directive.getTrusted() );
    }
    
    public void testScheme() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "scheme", m_scheme, directive.getScheme() );
    }
    
    public void testPrompt() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        assertEquals( "prompt", m_prompt, directive.getPrompt() );
    }
    
    public void testNullID() throws Exception
    {
        try
        {
            HostDirective directive = 
              new HostDirective( 
                null, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
                m_layout, m_scheme, m_prompt );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testNullHost() throws Exception
    {
        try
        {
            HostDirective directive = 
              new HostDirective( 
                m_id, m_priority, null, m_index, m_username, m_password, m_enabled, m_trusted, 
                m_layout, m_scheme, m_prompt );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullLayout() throws Exception
    {
        try
        {
            HostDirective directive = 
              new HostDirective( 
                m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
                null, m_scheme, m_prompt );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testSerialization() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        doSerializationTest( directive );
    }
    
    public void testEncoding() throws Exception
    {
        HostDirective directive = 
          new HostDirective( 
            m_id, m_priority, m_host, m_index, m_username, m_password, m_enabled, m_trusted, 
            m_layout, m_scheme, m_prompt );
        HostDirective result = (HostDirective) doEncodingTest( directive, "host-directive.xml" );
        assertEquals( "encoded", directive, result );
    }
}
