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

package net.dpml.library.impl;

import java.io.File;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

/**
 * The ImportDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PropertiesTestCase extends AbstractTestCase
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
        File file = new File( test, "samples/properties.xml" );
        Logger logger = new DefaultLogger( "test" );
        m_library = new DefaultLibrary( logger, file );
    }
    
   /**
    * Test the integrity of the local properties.
    * @exception Exception if an error occurs during test execution
    */
    public void testRootModuleProperties() throws Exception
    {
        DefaultModule acme = m_library.getDefaultModule( "org" );
        String[] names = acme.getLocalPropertyNames();
        assertEquals( "count", 2, names.length );
    }
    
   /**
    * Test the integrity of the local properties.
    * @exception Exception if an error occurs during test execution
    */
    public void testNestedModuleProperties() throws Exception
    {
        DefaultModule acme = m_library.getDefaultModule( "org/acme" );
        String[] names = acme.getLocalPropertyNames();
        assertEquals( "count", 1, names.length );
    }
}
