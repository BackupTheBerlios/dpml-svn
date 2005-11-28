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
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class LibraryDirectiveTestCase extends AbstractTestCase
{
    static ProcessorDescriptor[] PROCESSORS = ProcessorDescriptorTestCase.PROCESSORS;
    static ImportDirective[] IMPORTS = ImportDirectiveTestCase.IMPORTS;
    static ModuleDirective[] MODULES = ModuleDirectiveTestCase.MODULES;
    
    private LibraryDirective m_library;
    
    public void setUp() throws Exception
    {
        m_library = 
          new LibraryDirective( PROCESSORS, IMPORTS, MODULES, PROPERTIES );
    }

    public void testNullProcessors()
    {
        try
        {
            new LibraryDirective( null, IMPORTS, MODULES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testNullImports()
    {
        try
        {
            new LibraryDirective( PROCESSORS, null, MODULES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testProcessorDescriptors()
    {
        assertEquals( "process", PROCESSORS, m_library.getProcessorDescriptors() );
    }
    
    public void testImportDirectives()
    {
        assertEquals( "imports", IMPORTS, m_library.getImportDirectives() );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_library );
    }
    
    public void testXMLEncoding() throws Exception
    {
        doEncodingTest( m_library, "library-encoded.xml" );
    }
}
