/*
 * Copyright 2007 Stephen J. McConnell.
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

import dpml.station.info.PlanDescriptor;
import dpml.station.info.EntryDescriptor;

/**
 * Scenario descriptor testcase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PlanDescriptorTestCase extends AbstractTestCase
{
   /**
    * Test integrity of the plan title.
    * @exception Exception if an error occurs
    */
    public void testScenarioTitle() throws Exception
    {
        PlanDescriptor scenario = loadPlanDescriptor( "plan.xml" );
        String title = scenario.getTitle();
        assertEquals( "title", "Plan Demo", title );
    }
    
   /**
    * Test integrity of the plan entry uris.
    * @exception Exception if an error occurs
    */
    public void testPlanURIs() throws Exception
    {
        PlanDescriptor scenario = loadPlanDescriptor( "plan.xml" );
        EntryDescriptor[] entries = scenario.getEntryDescriptors();
        assertEquals( "uri", 3, entries.length );
    }
}
