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

package dpml.library.impl;

import java.io.File;

import dpml.library.Info;
import dpml.library.Module;

import net.dpml.util.Logger;
import dpml.util.DefaultLogger;

import junit.framework.TestCase;


/**
 * Test filter definition and propergation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InfoTestCase extends TestCase
{
    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
    
    private DefaultLibrary m_library;
    
   /**
    * Setup the library directive builder.
    * @exception Exception if an error occurs during test execution
    */
    public void setUp() throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, "samples/info.xml" );
        Logger logger = new DefaultLogger( "test" );
        m_library = new DefaultLibrary( logger, file );
    }
    
   /**
    * Test an empty info descriptor.
    * @exception Exception if an error occurs during test execution
    */
    public void testEmptyInfo() throws Exception
    {
        Module acme = m_library.getModule( "acme-1" );
        Info info = acme.getInfo();
        assertNull( "title", info.getTitle() );
        assertNull( "description", info.getDescription() );
    }
    
   /**
    * Test an empty info descriptor.
    * @exception Exception if an error occurs during test execution
    */
    public void testEmptyInfoWithEmptyDescription() throws Exception
    {
        Module acme = m_library.getModule( "acme-2" );
        Info info = acme.getInfo();
        assertNull( "title", info.getTitle() );
        assertNull( "description", info.getDescription() );
    }

   /**
    * Test an empty info descriptor.
    * @exception Exception if an error occurs during test execution
    */
    public void testInfoTitle() throws Exception
    {
        Module acme = m_library.getModule( "acme-3" );
        Info info = acme.getInfo();
        assertEquals( "title", "Widget", info.getTitle() );
        assertNull( "description", info.getDescription() );
    }
    
   /**
    * Test an empty info descriptor.
    * @exception Exception if an error occurs during test execution
    */
    public void testInfoTitleAndDescription() throws Exception
    {
        Module acme = m_library.getModule( "acme-4" );
        Info info = acme.getInfo();
        assertEquals( "title", "Widget", info.getTitle() );
        assertEquals( "description", "An example of a widget.", info.getDescription() );
    }
    
   /**
    * Test an empty info descriptor.
    * @exception Exception if an error occurs during test execution
    */
    public void testInfoDescription() throws Exception
    {
        Module acme = m_library.getModule( "acme-5" );
        Info info = acme.getInfo();
        assertNull( "title", info.getTitle() );
        assertEquals( "description", "An example of a widget.", info.getDescription() );
    }
}
