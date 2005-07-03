/*
 * Copyright 2004 Stephen J. McConnell.
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

package tutorial.extension;

import java.io.InputStream;
import java.util.Properties;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Example of loading a property file using the artifact protocol with 
 * the protocol handler setup as part of the jvm.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ExampleTestCase extends TestCase
{
    protected void setUp() throws Exception
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        System.setProperty( "dpml.transit.authority.file", "target/test/dpml.transit.authority" );
    }

    public void testPropertyLoading() throws Exception
    {
        Properties properties = new Properties();
        URL url = new URL( "artifact:spec:tutorial/example-property-file" );
        InputStream input = url.openStream();
        properties.load( input );
        String message = properties.getProperty( "tutorial.message" );
        System.out.println( message );
    }
}
