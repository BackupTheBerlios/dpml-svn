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

import net.dpml.library.Module;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

import junit.framework.TestCase;

/**
 * The ImportDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class AbsoluteTestCase extends AbstractTestCase
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
        File file = new File( test, "samples/absolute.xml" );
        Logger logger = new DefaultLogger( "test" );
        m_library = new DefaultLibrary( logger, file );
    }
    
   /**
    * Validation of an absolute module path expansion.  The module defintion
    * includes a path in the form "gov/nsa/acme" which should be expanded to
    * a structure containing the "gov" module, containing the "nsa" module,
    * containing the "acme" module.
    * @exception Exception if an error occurs during test execution
    */
    public void testAbsoluteModulePath() throws Exception
    {
        Module gov = m_library.getModule( "gov" );
        Module nsa = m_library.getModule( "gov/nsa" );
        Module acme = m_library.getModule( "gov/nsa/acme" );
    }
}
