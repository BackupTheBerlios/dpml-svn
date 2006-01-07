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
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class TypeDirectiveTestCase extends AbstractTestCase
{
    static final TypeDirective[] TYPES = new TypeDirective[3];
    static
    {
        TYPES[0] = new TypeDirective( "jar", false, PROPERTIES );
        TYPES[1] = new TypeDirective( "plugin", true, PROPERTIES );
        TYPES[2] = new TypeDirective( "widget", false, PROPERTIES );
    }
    
   /**
    * Validate that the type directive constructor
    * throws an NPE when supplied with a null name.
    */
    public void testNullName()
    {
        try
        {
            TypeDirective type = new TypeDirective( null, true, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test the type directive name accessor.
    */
    public void testTypeName()
    {
        TypeDirective type = new TypeDirective( "abc", true, PROPERTIES );
        assertEquals( "type", "abc", type.getName() );
    }
    
   /**
    * Test the type directive alias accessor.
    */
    public void testTypeAlias()
    {
        TypeDirective type = new TypeDirective( "abc", true, PROPERTIES );
        assertTrue( "alias", type.getAlias() );
        type = new TypeDirective( "abc", false, PROPERTIES );
        assertFalse( "alias", type.getAlias() );
    }
    
   /**
    * Test the type directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        TypeDirective type = new TypeDirective( "abc", true, PROPERTIES );
        doSerializationTest( type );
    }

   /**
    * Test the type directive encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testXMLEncoding() throws Exception
    {
        TypeDirective type = new TypeDirective( "abc", true, PROPERTIES );
        doEncodingTest( type, "type-descriptor-encoded.xml" );
    }
}
