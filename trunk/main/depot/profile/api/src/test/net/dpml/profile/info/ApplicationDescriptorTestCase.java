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

package net.dpml.station.info;

import java.net.URI;
import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ApplicationDescriptorTestCase extends AbstractTestCase
{
    static ValueDescriptor[] VALUES = new ValueDescriptor[2];
    static
    {
        VALUES[0] = new ValueDescriptor( "red" );
        VALUES[1] = new ValueDescriptor( "blue" );
    }
    
    public void testSerialization() throws Exception
    {
        final String codebase = "link:plugin:acme/widget";
        final String title = "widget";
        final String basedir = ".";
        final StartupPolicy policy = StartupPolicy.AUTOMATIC;
        final int startup = 10;
        final int shutdown = 20;
        final Properties properties = new Properties();
        final String config = null;
        
        ApplicationDescriptor descriptor = 
           new ApplicationDescriptor( 
             codebase, title, VALUES, basedir, policy, startup, shutdown, properties, config );
        doSerializationTest( descriptor );
    }
    
    public void testXMLEncoding() throws Exception
    {
        final String codebase = "link:plugin:acme/widget";
        final String title = "widget";
        final String basedir = ".";
        final StartupPolicy policy = StartupPolicy.AUTOMATIC;
        final int startup = 10;
        final int shutdown = 20;
        final Properties properties = new Properties();
        final String config = null;
        
        ApplicationDescriptor descriptor = 
           new ApplicationDescriptor( 
             codebase, title, VALUES, basedir, policy, startup,  shutdown, properties, config );
        doEncodingTest( descriptor, "application-directive.xml" );
    }
}
