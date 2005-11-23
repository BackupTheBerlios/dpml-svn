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
public final class LayoutDirectiveTestCase extends CodeBaseDirectiveTestCase
{
    private String m_id = "test";
    private String m_title = "title";
    
    public void testID() throws Exception
    {
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, m_codebase, m_values );
          
        assertEquals( "id", m_id, directive.getID() );
    }
    
    public void testTitle() throws Exception
    {
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, m_codebase, m_values );
          
        assertEquals( "title", m_title, directive.getTitle() );
    }
    
    public void testNullID() throws Exception
    {
        try
        {
            LayoutDirective directive = 
              new LayoutDirective( null, m_title, m_codebase, m_values );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullTitle() throws Exception
    {
        LayoutDirective directive = 
          new LayoutDirective( m_id, null, m_codebase, m_values );
        assertEquals( "title", m_id, directive.getTitle() );
    }
    
    public void testSerialization() throws Exception
    {
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, m_codebase, m_values );
        doSerializationTest( directive );
    }
    
    public void testEncoding() throws Exception
    {
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, m_codebase, m_values );
        LayoutDirective result = 
          (LayoutDirective) doEncodingTest( directive, "content-handler-directive.xml" );
        assertEquals( "encoded", directive, result );
    }
}
