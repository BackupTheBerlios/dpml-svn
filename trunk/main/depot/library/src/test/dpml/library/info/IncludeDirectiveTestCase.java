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

package dpml.library.info;

import dpml.util.Category;

/**
 * The IncludeDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class IncludeDirectiveTestCase extends AbstractTestCase
{
    static final IncludeDirective[] INCLUDES = new IncludeDirective[3];
    static
    {
        INCLUDES[0] = new IncludeDirective( IncludeDirective.REF, null, "value", PROPERTIES );
        INCLUDES[1] = new IncludeDirective( IncludeDirective.KEY, null, "value", PROPERTIES );
        INCLUDES[2] = new IncludeDirective( IncludeDirective.REF, Category.PUBLIC, "value", PROPERTIES );
    }

   /**
    * Test that an NPE is thrown when the null mode is supplied as a constructor argument.
    */
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
    
   /**
    * Test that an NPE is thrown when the null value is supplied as a constructor argument.
    */
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
    
   /**
    * Test the mode accessor.
    */
    public void testIncludeMode()
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, null, "value", PROPERTIES );
        assertEquals( "mode", IncludeDirective.REF, include.getMode() );
    }
    
   /**
    * Test the value accessor.
    */
    public void testIncludeValue()
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, null, "value", PROPERTIES );
        assertEquals( "value", "value", include.getValue() );
    }
    
   /**
    * Test include classloader category.
    */
    public void testIncludeCategory()
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, Category.PROTECTED, "value", PROPERTIES );
        assertEquals( "category", Category.PROTECTED, include.getCategory() );
    }
    
   /**
    * Test serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        IncludeDirective include = 
          new IncludeDirective( IncludeDirective.REF, Category.PROTECTED, "value", PROPERTIES );
        doSerializationTest( include );
    }
}
