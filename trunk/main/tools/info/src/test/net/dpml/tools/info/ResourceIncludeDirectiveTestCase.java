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
 * The IncludeDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ResourceIncludeDirectiveTestCase extends AbstractTestCase
{
    public void testNullName()
    {
        try
        {
            new ResourceIncludeDirective( null, "value" );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullValue()
    {
        try
        {
            new ResourceIncludeDirective( ResourceIncludeDirective.KEY, null );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testIncludeType()
    {
        ResourceIncludeDirective include = new ResourceIncludeDirective( ResourceIncludeDirective.KEY, "value" );
        assertEquals( "name", "key", include.getType() );
        assertEquals( "mode", ResourceIncludeDirective.KEY, include.getMode() );
    }
    
    public void testIncludeValue()
    {
        IncludeDirective include = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "value" );
        assertEquals( "value", "value", include.getValue() );
    }
    
    public void testSerialization() throws Exception
    {
        IncludeDirective include = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "value" );
        doSerializationTest( include );
    }

    public void testXMLEncoding() throws Exception
    {
        IncludeDirective include = new ResourceIncludeDirective( ResourceIncludeDirective.REF, "value" );
        doEncodingTest( include, "include-encoded.xml" );
    }
}
