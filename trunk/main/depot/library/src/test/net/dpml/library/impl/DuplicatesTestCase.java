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
import net.dpml.library.impl.DefaultLibrary;

import net.dpml.lang.Logger;

import net.dpml.transit.monitor.LoggingAdapter;

import junit.framework.TestCase;


/**
 * Library XML test case.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DuplicatesTestCase extends TestCase
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
    public void testLibrary() throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, "samples/duplicates.xml" );
        Logger logger = new LoggingAdapter( "test" );
        DefaultLibrary library = new DefaultLibrary( logger, file );
    }
    
}
