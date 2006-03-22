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

package net.dpml.job.test;

import net.dpml.job.Commissionable;
import net.dpml.job.CommissionerEvent;

import junit.framework.TestCase;

/**
 * Validation of the timeout exception.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CommissionerEventTestCase extends TestCase
{
    
   /**
    * Test the commissioner event flag accessor.
    * @exception Exception if an error occurs
    */
    public void testFlag() throws Exception
    {
        Commissionable commissionable = new MockCommissionable();
        CommissionerEvent event = new CommissionerEvent( commissionable, true, 0 );
        assertEquals( "flag", true, event.isCommissioning() );
    }
    
   /**
    * Test the commissioner event duration accessor.
    * @exception Exception if an error occurs
    */
    public void testDuration() throws Exception
    {
        Commissionable commissionable = new MockCommissionable();
        CommissionerEvent event = new CommissionerEvent( commissionable, true, 0 );
        assertEquals( "timeout", 0, event.getDuration() );
    }
    
   /**
    * Test the commissioner event source accessor.
    * @exception Exception if an error occurs
    */
    public void testSource() throws Exception
    {
        Commissionable commissionable = new MockCommissionable();
        CommissionerEvent event = new CommissionerEvent( commissionable, true, 0 );
        assertEquals( "source", commissionable, event.getSource() );
    }
    
    private class MockCommissionable implements Commissionable
    {
       /**
        * Commission the object.
        * @throws Exception if a error occurs
        */
        public void commission() throws Exception
        {
        }
    
       /**
        * Decommission the object.
        */
        public void decommission()
        {
        }
    }
    
}
