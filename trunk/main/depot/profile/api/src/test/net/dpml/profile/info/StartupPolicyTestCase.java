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

package net.dpml.profile.info;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class StartupPolicyTestCase extends AbstractTestCase
{
    public void testDisabled() throws Exception
    {
        StartupPolicy policy = StartupPolicy.parse( "disabled" );
        assertEquals( policy, StartupPolicy.DISABLED );
    }
    
    public void testManual() throws Exception
    {
        StartupPolicy policy = StartupPolicy.parse( "manual" );
        assertEquals( policy, StartupPolicy.MANUAL );
    }
    
    public void testAutomatic() throws Exception
    {
        StartupPolicy policy = StartupPolicy.parse( "automatic" );
        assertEquals( policy, StartupPolicy.AUTOMATIC );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( StartupPolicy.AUTOMATIC );
    }
    
    public void testXMLEncoding() throws Exception
    {
        doEncodingTest( StartupPolicy.AUTOMATIC, "startup-policy.xml" );
    }
}