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

import net.dpml.lang.Version;


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
        TYPES[0] = new TypeDirective( "jar", null, PROPERTIES );
        TYPES[1] = new TypeDirective( "plugin", Version.NULL_VERSION, PROPERTIES );
        TYPES[2] = new TypeDirective( "widget", Version.parse( "1.2" ), PROPERTIES );
    }
    
   /**
    * Validate that the type directive constructor
    * throws an NPE when supplied with a null name.
    */
    public void testNullName()
    {
        try
        {
            TypeDirective type = new TypeDirective( null, Version.NULL_VERSION, PROPERTIES );
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
        TypeDirective type = new TypeDirective( "abc", Version.parse( "1.2" ), PROPERTIES );
        assertEquals( "type", "abc", type.getID() );
    }
    
   /**
    * Test the type directive alias accessor.
    */
    public void testNullTypeVersion()
    {
        TypeDirective type = new TypeDirective( "abc", null, PROPERTIES );
        assertEquals( "null-version", null, type.getVersion() );
    }
    
   /**
    * Test the type directive alias accessor.
    */
    public void testExplicitTypeVersion()
    {
        Version version = Version.parse( "1.2" );
        TypeDirective type = new TypeDirective( "abc", version, PROPERTIES );
        assertEquals( "version", version, type.getVersion() );
    }
    
   /**
    * Test the type directive alias accessor.
    */
    public void testLogicalNullVersion()
    {
        Version version = Version.NULL_VERSION;
        TypeDirective type = new TypeDirective( "abc", version, PROPERTIES );
        assertEquals( "version", version, type.getVersion() );
    }
    
   /**
    * Test the type directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        TypeDirective type = new TypeDirective( "abc", Version.parse( "1.2" ), PROPERTIES );
        doSerializationTest( type );
    }
}
