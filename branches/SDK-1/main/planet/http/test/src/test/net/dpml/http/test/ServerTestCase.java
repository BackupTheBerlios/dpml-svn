/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.http.test;

import java.net.URI;

import junit.framework.TestCase;

import net.dpml.lang.Part;

/**
 * Server component testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServerTestCase extends TestCase
{
   /**
    * Test the deployment of the Jetty server.
    * @exception Exception if an error occurs
    */
    public void testServerDeployment() throws Exception
    {
        URI uri = new URI( "link:part:dpml/planet/http/dpml-http-server" );
        Part part = Part.load( uri );
        Object object = part.instantiate( new Object[0] );
        assertEquals( "class", "net.dpml.http.Server", object.getClass().getName() );
    }
}
