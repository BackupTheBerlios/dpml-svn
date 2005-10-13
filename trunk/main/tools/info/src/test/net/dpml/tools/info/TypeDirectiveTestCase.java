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

package net.dpml.tools.info;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class TypeDirectiveTestCase extends AbstractTestCase
{
    public void testNullName()
    {
        try
        {
            TypeDirective type = new TypeDirective( null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testTypeName()
    {
        TypeDirective type = new TypeDirective( "abc", PROPERTIES );
        assertEquals( "type", "abc", type.getName() );
    }
    
    public void testSerialization() throws Exception
    {
        TypeDirective type = new TypeDirective( "abc", PROPERTIES );
        doSerializationTest( type );
    }

    public void testXMLEncoding() throws Exception
    {
        TypeDirective type = new TypeDirective( "abc", PROPERTIES );
        doEncodingTest( type, "type-descriptor-encoded.xml" );
    }
}
