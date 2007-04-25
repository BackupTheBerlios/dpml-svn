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

/**
 * The ImportDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ImportDirectiveTestCase extends AbstractTestCase
{
    static final ImportDirective[] IMPORTS = new ImportDirective[3];
    static
    {
        IMPORTS[0] = new ImportDirective( ImportDirective.URI, "something", PROPERTIES );
        IMPORTS[1] = new ImportDirective( ImportDirective.FILE, "bingo", PROPERTIES );
        IMPORTS[2] = new ImportDirective( ImportDirective.URI, "acme", PROPERTIES );
    }

   /**
    * Test that the import directive throws an NPE if a null name is supplied.
    */
    public void testNullName()
    {
        try
        {
            new ImportDirective( null, "value", PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test that the import directive throws an NPE if a null value is supplied.
    */
    public void testNullValue()
    {
        try
        {
            new ImportDirective( ImportDirective.FILE, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test the import mode accessor.
    */
    public void testImportMode()
    {
        ImportDirective include = 
          new ImportDirective( ImportDirective.FILE, "value", PROPERTIES );
        assertEquals( "mode", ImportDirective.FILE, include.getMode() );
    }
    
   /**
    * Test the import value accessor.
    */
    public void testImportValue()
    {
        ImportDirective include = 
          new ImportDirective( ImportDirective.URI, "value", PROPERTIES );
        assertEquals( "value", "value", include.getValue() );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        ImportDirective include = 
          new ImportDirective( ImportDirective.URI, "value", PROPERTIES );
        doSerializationTest( include );
    }

}
