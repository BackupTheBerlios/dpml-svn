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

import net.dpml.library.Filter;
import net.dpml.library.Module;
import net.dpml.library.Resource;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

import junit.framework.TestCase;


/**
 * Test filter definition and propergation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FiltersTestCase extends TestCase
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
    * Test an library definition containing duplicate modules.
    * @exception Exception if an error occurs during test execution
    */
    public void testModuleFilters() throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, "samples/filters.xml" );
        Logger logger = new DefaultLogger( "test" );
        DefaultLibrary library = new DefaultLibrary( logger, file );
        Module acme = library.getModule( "acme" );
        Filter[] filters = acme.getFilters();
        assertEquals( "count", 1, filters.length );
        Filter filter = filters[0];
        assertEquals( "token", "PUBLISHER", filter.getToken() );
        assertEquals( "value", "ACME", filter.getValue( acme ) );
    }
    
   /**
    * Test an library definition containing duplicate modules.
    * @exception Exception if an error occurs during test execution
    */
    public void testResourceFilters() throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, "samples/filters.xml" );
        Logger logger = new DefaultLogger( "test" );
        DefaultLibrary library = new DefaultLibrary( logger, file );
        Resource widget = library.getResource( "acme/widget" );
        Filter[] filters = widget.getFilters();
        assertEquals( "count", 3, filters.length );
    }
}
