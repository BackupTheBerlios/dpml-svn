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
public final class LibraryDirectiveTestCase extends AbstractTestCase
{
    static final ImportDirective[] IMPORTS = ImportDirectiveTestCase.IMPORTS;
    static final ModuleDirective[] MODULES = ModuleDirectiveTestCase.MODULES;
    
    private LibraryDirective m_library;
    
   /**
    * Setup the testcase.
    * @exception Exception if an error occurs during setup
    */
    public void setUp() throws Exception
    {
        m_library = 
          new LibraryDirective( IMPORTS, MODULES, PROPERTIES );
    }

   /**
    * Validate that the imports argument to the library directive 
    * throws an NPE when supplied with a null imports value.
    */
    public void testNullImports()
    {
        try
        {
            new LibraryDirective( null, MODULES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test the imports accessor.
    */
    public void testImportDirectives()
    {
        assertEquals( "imports", IMPORTS, m_library.getImportDirectives() );
    }
    
   /**
    * Test serialization of the library directive.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_library );
    }
}
