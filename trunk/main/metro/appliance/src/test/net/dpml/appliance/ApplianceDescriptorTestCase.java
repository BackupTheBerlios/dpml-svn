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

package net.dpml.appliance;

import java.net.URI;
import java.util.Properties;
import java.util.Map;

import dpml.station.info.ApplianceDescriptor;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ApplianceDescriptorTestCase extends AbstractTestCase
{
   /**
    * Test the integrity of an appliance descriptor.
    * @exception Exception if an error occurs
    */
    public void testApplianceDescriptor() throws Exception
    {
        ApplianceDescriptor application = loadApplianceDescriptor( "appliance.xml" );
        String path = application.getPath();
        int startup = application.getStartupTimeout();
        int shutdown = application.getShutdownTimeout();
        Properties properties = application.getSystemProperties();
        Map environment = application.getEnvironmentMap();
        assertEquals( "startup", 0, startup );
        assertEquals( "shutdown", 0, shutdown );
        assertEquals( "path", null, path );
        assertEquals( "system", "bar", properties.getProperty( "foo" ) );
        assertEquals( "environment", "BAR", environment.get( "FOO" ) );
        URI uri = new URI( "link:part:dpml/metro/dpml-metro-sample?message=Hello%20World&port=1024" );
        URI target = application.getTargetURI();
        assertEquals( "uri", uri, target );
    }
}
