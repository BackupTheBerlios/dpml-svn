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
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.library.impl.LibraryBuilder;
import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.TypeDirective;

import net.dpml.lang.Type;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentType;


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
    
    private LibraryBuilder m_builder;
    
   /**
    * Setup the library directive builder.
    */
    public void setUp() throws Exception
    {
        m_builder = new LibraryBuilder();
    }
    
   /**
    * Test an empty library definition.
    */
    public void testEmptyLibrary() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-1.xml" );
        assertEquals( "imports", 0, library.getImportDirectives().length );
        assertEquals( "modules", 0, library.getModuleDirectives().length );
        assertEquals( "properties", 0, library.getProperties().keySet().size() );
    }
    
   /**
    * Test a library definition containing just properties.
    */
    public void testLibraryWithProperties() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-2.xml" );
        assertEquals( "imports", 0, library.getImportDirectives().length );
        assertEquals( "modules", 0, library.getModuleDirectives().length );
        assertEquals( "properties", 2, library.getProperties().keySet().size() );
    }

   /**
    * Test a library definition containing just a few imports.
    */
    public void testLibraryWithImports() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-3.xml" );
        assertEquals( "imports", 2, library.getImportDirectives().length );
        assertEquals( "modules", 0, library.getModuleDirectives().length );
        assertEquals( "properties", 0, library.getProperties().keySet().size() );
    }

   /**
    * Test a library definition containing just a modules imports.
    */
    public void testLibraryWithModules() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-4.xml" );
        assertEquals( "imports", 2, library.getImportDirectives().length );
        assertEquals( "modules", 3, library.getModuleDirectives().length );
        assertEquals( "properties", 0, library.getProperties().keySet().size() );
    }

   /**
    * Test a direct module name.
    */
    public void testModuleName() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-5.xml" );
        ModuleDirective[] modules = library.getModuleDirectives();
        ModuleDirective module = modules[0];
        String name = module.getName();
        assertEquals( "name", "acme", name );
    }
    
   /**
    * Test a direct module basedir.
    */
    public void testModuleBasedir() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-5.xml" );
        ModuleDirective[] modules = library.getModuleDirectives();
        ModuleDirective module = modules[0];
        String basedir = module.getBasedir();
        assertEquals( "basedir", ".", basedir );
    }

   /**
    * Test the properties within a module.
    */
    public void testModuleProperties() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-5.xml" );
        ModuleDirective[] modules = library.getModuleDirectives();
        ModuleDirective module = modules[0];
        assertEquals( "properties", 3, module.getProperties().keySet().size() );
    }

   /**
    * Test the import of a module into the <modules> element.
    */
    public void testModuleImport() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-6.xml" );
        ModuleDirective[] modules = library.getModuleDirectives();
        assertEquals( "modules", 1, modules.length );
    }

   /**
    * Test the import of a module into the <modules> element.
    */
    public void testNestedModuleImport() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-7.xml" );
        ModuleDirective[] modules = library.getModuleDirectives();
        assertEquals( "modules", 1, modules.length );
        ModuleDirective module = modules[0];
        ResourceDirective[] resources = module.getResourceDirectives();
        assertEquals( "resources", 1, resources.length );
    }

   /**
    * Test the import of a module into the <modules> element.
    */
    public void testResourceStatement() throws Exception
    {
        LibraryDirective library = getDirective( "samples/example-8.xml" );
        ModuleDirective[] modules = library.getModuleDirectives();
        assertEquals( "modules", 1, modules.length );
        ModuleDirective module = modules[0];
        ResourceDirective[] resources = module.getResourceDirectives();
        assertEquals( "resources", 1, resources.length );
        ResourceDirective resource = resources[0];
        TypeDirective[] types = resource.getTypeDirectives();
        assertEquals( "types", 2, types.length );
    }

    private LibraryDirective getDirective( String path ) throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, path );
        return m_builder.build( file );
    }
}
