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

package net.dpml.metro.info.test;

import net.dpml.lang.Version;

import net.dpml.metro.info.ServiceDescriptor;

import junit.framework.TestCase;

/**
 * Priority testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServiceDescriptorTestCase extends TestCase
{
   /**
    * Test classname.
    * @exception Exception if an error occurs
    */
    public void testClassname() throws Exception
    {
        String classname = "Widget";
        ServiceDescriptor service = new ServiceDescriptor( classname );
        assertEquals( "classname", classname, service.getClassname() );
    }

   /**
    * Test default version.
    * @exception Exception if an error occurs
    */
    public void testDefaultVersion() throws Exception
    {
        String classname = "Widget";
        Version version = Version.parse( "1" );
        ServiceDescriptor service = new ServiceDescriptor( classname );
        assertEquals( "version", version, service.getVersion() );
    }
    
   /**
    * Test explicit version.
    * @exception Exception if an error occurs
    */
    public void testExplicitVersion() throws Exception
    {
        String classname = "Widget";
        Version version = Version.parse( "2.1.3" );
        String spec = classname + "#" + version;
        ServiceDescriptor service = new ServiceDescriptor( spec );
        assertEquals( "version", version, service.getVersion() );
    }
}
