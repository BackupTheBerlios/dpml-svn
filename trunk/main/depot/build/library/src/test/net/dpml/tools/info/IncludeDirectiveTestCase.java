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

package net.dpml.build.info;

import net.dpml.transit.Category;

/**
 * The IncludeDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class IncludeDirectiveTestCase extends AbstractTestCase
{
    static IncludeDirective[] INCLUDES = new IncludeDirective[3];
    static
    {
        INCLUDES[0] = new IncludeDirective( IncludeDirective.REF, null, "value", PROPERTIES );
        INCLUDES[1] = new IncludeDirective( IncludeDirective.KEY, null, "value", PROPERTIES );
        INCLUDES[2] = new IncludeDirective( IncludeDirective.REF, Category.PUBLIC, "value", PROPERTIES );
    }

    public void testNullMode()
    {
        try
        {
            new IncludeDirective( null, null, "value", PROPERTIES );
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
            new IncludeDirective( IncludeDirective.REF, null, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testIncludeMode()
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, null, "value", PROPERTIES );
        assertEquals( "mode", IncludeDirective.REF, include.getMode() );
    }
    
    public void testIncludeValue()
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, null, "value", PROPERTIES );
        assertEquals( "value", "value", include.getValue() );
    }
    
    public void testIncludeCategory()
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, Category.PROTECTED, "value", PROPERTIES );
        assertEquals( "category", Category.PROTECTED, include.getCategory() );
    }
    
    public void testSerialization() throws Exception
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, Category.PROTECTED, "value", PROPERTIES );
        doSerializationTest( include );
    }

    public void testXMLEncoding() throws Exception
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, Category.PROTECTED, "value", PROPERTIES );
        doEncodingTest( include, "include-encoded.xml" );
    }
}
