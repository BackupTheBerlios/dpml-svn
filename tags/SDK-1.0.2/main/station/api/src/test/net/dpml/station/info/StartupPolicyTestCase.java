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

/**
 * Test StartupPolicy class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class StartupPolicyTestCase extends AbstractTestCase
{
   /**
    * Test DISABLED startup policy parse method.
    * @exception Exception if an error occurs
    */
    public void testParseDisabled() throws Exception
    {
        StartupPolicy policy = StartupPolicy.parse( "disabled" );
        assertEquals( "disabled", policy, StartupPolicy.DISABLED );
    }
    
   /**
    * Test MANUAL startup policy parse method.
    * @exception Exception if an error occurs
    */
    public void testParseManual() throws Exception
    {
        StartupPolicy policy = StartupPolicy.parse( "manual" );
        assertEquals( "manual", policy, StartupPolicy.MANUAL );
    }
    
   /**
    * Test AUTOMATIC startup policy parse method.
    * @exception Exception if an error occurs
    */
    public void testParseAutomatic() throws Exception
    {
        StartupPolicy policy = StartupPolicy.parse( "automatic" );
        assertEquals( "automatic", policy, StartupPolicy.AUTOMATIC );
    }
    
   /**
    * Test policy serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        StartupPolicy policy = StartupPolicy.AUTOMATIC;
        doSerializationTest( policy );
    }
}
