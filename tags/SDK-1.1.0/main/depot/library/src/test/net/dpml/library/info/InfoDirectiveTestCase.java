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

package net.dpml.library.info;

/**
 * The ImportDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class InfoDirectiveTestCase extends AbstractTestCase
{
    static final InfoDirective INFO = new InfoDirective( "test", "about test" );

   /**
    * Test construction of an info directive with null title and description.
    */
    public void testNullArgs()
    {
        InfoDirective info = new InfoDirective( null, null );
        assertNull( "title", info.getTitle() );
        assertNull( "description", info.getDescription() );
        assertTrue( "is-null", info.isNull() );
    }
    
   /**
    * Test construction of an info directive with a title and null description.
    */
    public void testNullDescription()
    {
        InfoDirective info = new InfoDirective( "title", null );
        assertEquals( "title", "title", info.getTitle() );
        assertNull( "description", info.getDescription() );
        assertFalse( "is-null", info.isNull() );
    }
    
   /**
    * Test construction of an info directive with a null title and non-null description.
    */
    public void testNullTitle()
    {
        InfoDirective info = new InfoDirective( null, "something" );
        assertNull( "title", info.getTitle() );
        assertEquals( "description", "something", info.getDescription() );
        assertFalse( "is-null", info.isNull() );
    }
    
   /**
    * Test construction of an info directive with a title and description.
    */
    public void testFullyPopulatedInfo()
    {
        InfoDirective info = new InfoDirective( "title", "something" );
        assertEquals( "title", "title", info.getTitle() );
        assertEquals( "description", "something", info.getDescription() );
        assertFalse( "is-null", info.isNull() );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        InfoDirective info = 
          new InfoDirective( "test", "something" );
        doSerializationTest( info );
    }

}
