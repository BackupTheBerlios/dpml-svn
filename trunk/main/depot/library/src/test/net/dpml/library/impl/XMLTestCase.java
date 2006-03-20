/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.library.impl;

import java.io.File;

import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.TypeDirective;

import junit.framework.TestCase;


/**
 * Library XML test case.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class XMLTestCase extends TestCase
{
    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
    
    private LibraryDecoder m_decoder;
    
   /**
    * Setup the library directive builder.
    * @exception Exception if an error occurs during test execution
    */
    public void setUp() throws Exception
    {
        m_decoder = new LibraryDecoder();
    }
    
   /**
    * Test an empty library definition.
    * @exception Exception if an error occurs during test execution
    */
    public void testEmptyLibrary() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-1.xml" );
        assertEquals( "imports", 0, library.getImportDirectives().length );
        assertEquals( "resources", 0, library.getResourceDirectives().length );
        assertEquals( "properties", 0, library.getProperties().keySet().size() );
    }
    
   /**
    * Test a library definition containing just properties.
    * @exception Exception if an error occurs during test execution
    */
    public void testLibraryWithProperties() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-2.xml" );
        assertEquals( "imports", 0, library.getImportDirectives().length );
        assertEquals( "resources", 0, library.getResourceDirectives().length );
        assertEquals( "properties", 2, library.getProperties().keySet().size() );
    }

   /**
    * Test a library definition containing just a few imports.
    * @exception Exception if an error occurs during test execution
    */
    public void testLibraryWithImports() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-3.xml" );
        assertEquals( "imports", 2, library.getImportDirectives().length );
        assertEquals( "resources", 0, library.getResourceDirectives().length );
        assertEquals( "properties", 0, library.getProperties().keySet().size() );
    }

   /**
    * Test a library definition containing just a modules imports.
    * @exception Exception if an error occurs during test execution
    */
    public void testLibraryWithModules() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-4.xml" );
        assertEquals( "imports", 2, library.getImportDirectives().length );
        assertEquals( "resources", 3, library.getResourceDirectives().length );
        assertEquals( "properties", 0, library.getProperties().keySet().size() );
    }

   /**
    * Test a direct module name.
    * @exception Exception if an error occurs during test execution
    */
    public void testModuleName() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-5.xml" );
        ResourceDirective[] resources = library.getResourceDirectives();
        ResourceDirective resource = resources[0];
        String name = resource.getName();
        assertEquals( "name", "acme", name );
    }
    
   /**
    * Test a direct module basedir.
    * @exception Exception if an error occurs during test execution
    */
    public void testModuleBasedir() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-5.xml" );
        ResourceDirective[] resources = library.getResourceDirectives();
        ResourceDirective resource = resources[0];
        String basedir = resource.getBasedir();
        assertEquals( "basedir", ".", basedir );
    }

   /**
    * Test the properties within a module.
    * @exception Exception if an error occurs during test execution
    */
    public void testModuleProperties() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-5.xml" );
        ResourceDirective[] resources = library.getResourceDirectives();
        ResourceDirective resource = resources[0];
        assertEquals( "properties", 3, resource.getProperties().keySet().size() );
    }

   /**
    * Test the import of a module into the <modules> element.
    * @exception Exception if an error occurs during test execution
    */
    public void testModuleImport() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-6.xml" );
        ResourceDirective[] resources = library.getResourceDirectives();
        assertEquals( "modules", 1, resources.length );
    }

   /**
    * Test the import of a module into the <modules> element.
    * @exception Exception if an error occurs during test execution
    */
    public void testNestedModuleImport() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-7.xml" );
        ResourceDirective[] resources = library.getResourceDirectives();
        assertEquals( "resources", 1, resources.length );
        ResourceDirective resource = resources[0];
        if( resource instanceof ModuleDirective )
        {
            ModuleDirective module = (ModuleDirective) resource;
            ResourceDirective[] ress = module.getResourceDirectives();
            assertEquals( "resources", 1,  ress.length );
        }
        else
        {
            fail( "Expecting a module - found a resource." );
        }
    }

   /**
    * Test the import of a module into the <modules> element.
    * @exception Exception if an error occurs during test execution
    */
    public void testResourceStatement() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-8.xml" );
        ResourceDirective[] resources = library.getResourceDirectives();
        assertEquals( "resources", 1, resources.length );
        ResourceDirective resource = resources[0];
        if( resource instanceof ModuleDirective )
        {
            ModuleDirective module = (ModuleDirective) resource;
            ResourceDirective[] ress = module.getResourceDirectives();
            assertEquals( "resources", 1, ress.length );
            ResourceDirective r = ress[0];
            TypeDirective[] types = r.getTypeDirectives();
            assertEquals( "types", 2, types.length );
        }
        else
        {
            fail( "Expecting a module - found a resource." );
        }
    }

    private LibraryDirective getDirective( String path ) throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, path );
        return m_decoder.build( file );
    }
}
