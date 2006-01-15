/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.test.part;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Directive;
import net.dpml.part.local.Controller;

/**
 * Testcase validating part loading from a uri.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartLoadingTestCase extends TestCase
{
    private static final Controller CONTROLLER = Controller.STANDARD;

    private URI m_uri;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "test.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        m_uri = new File( test, path ).toURI();
    }

   /**
    * Test part loading via a controller.
    * @exception Exception if an error occurs
    */
    public void testPartLoading() throws Exception
    {
        Directive directive = CONTROLLER.loadDirective( m_uri );
        String classname = directive.getClass().getName();
        assertEquals( "directive-classname", "net.dpml.metro.data.ComponentDirective", classname );
    }
}
