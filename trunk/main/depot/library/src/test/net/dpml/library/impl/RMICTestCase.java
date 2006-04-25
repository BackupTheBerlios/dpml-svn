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

import net.dpml.library.info.RMICDirective;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

import junit.framework.TestCase;


/**
 * Test rmic criteria.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RMICTestCase extends TestCase
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
        File file = new File( test, "samples/rmic.xml" );
        Logger logger = new DefaultLogger( "test" );
        DefaultLibrary library = new DefaultLibrary( logger, file );
        Module acme = library.getModule( "acme" );
        RMICDirective rmic = (RMICDirective) acme.getDataDirective( RMICDirective.KEY );
        assertEquals( "includes", 3, rmic.getIncludes().length );
        assertEquals( "excludes", 2, rmic.getExcludes().length );
    }
}
