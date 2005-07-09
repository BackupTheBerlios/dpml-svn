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

package tutorial.artifact;

import java.io.InputStream;
import java.io.File;
import java.util.Properties;
import java.net.URL;

import net.dpml.transit.artifact.Handler;

import junit.framework.TestCase;

/**
 * Example of loading a property file using the artifact protocol and explicit 
 * handler declaration.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ExampleTestCase extends TestCase
{
    protected void setUp() throws Exception
    {
        File basedir = new File( System.getProperty( "project.dir" ) );
        File authority = new File( basedir, "dpml.transit.authority" );
        System.setProperty( "dpml.transit.authority.file", authority.getAbsolutePath() );
    }

    public void testPropertyLoading() throws Exception
    {
        Properties properties = new Properties();
        URL url = new URL( null, "artifact:spec:tutorial/example-property-file", new Handler() );
        InputStream input = url.openStream();
        properties.load( input );
        String message = properties.getProperty( "tutorial.message" );
        System.out.println( message );
    }
}
