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

import net.dpml.lang.ValueDirective;

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
    
   /**
    * Test id accessor.
    * @exception Exception if an error occurs
    */
    public void testID() throws Exception
    {
        String codebase = getCodebaseValue();
        ValueDirective[] values = getValueDirectives();
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, codebase, values );
          
        assertEquals( "id", m_id, directive.getID() );
    }
    
   /**
    * Test title accessor.
    * @exception Exception if an error occurs
    */
    public void testTitle() throws Exception
    {
        String codebase = getCodebaseValue();
        ValueDirective[] values = getValueDirectives();
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, codebase, values );
          
        assertEquals( "title", m_title, directive.getTitle() );
    }
    
   /**
    * Test invalid null id in constructor.
    * @exception Exception if an error occurs
    */
    public void testNullID() throws Exception
    {
        String codebase = getCodebaseValue();
        ValueDirective[] values = getValueDirectives();
        try
        {
            LayoutDirective directive = 
              new LayoutDirective( null, m_title, codebase, values );
            fail( "NPE expected" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test null title in constructor.
    * @exception Exception if an error occurs
    */
    public void testNullTitle() throws Exception
    {
        String codebase = getCodebaseValue();
        ValueDirective[] values = getValueDirectives();
        LayoutDirective directive = 
          new LayoutDirective( m_id, null, codebase, values );
        assertEquals( "title", m_id, directive.getTitle() );
    }
    
   /**
    * Test serailization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        String codebase = getCodebaseValue();
        ValueDirective[] values = getValueDirectives();
        LayoutDirective directive = 
          new LayoutDirective( m_id, m_title, codebase, values );
        doSerializationTest( directive );
    }
}
