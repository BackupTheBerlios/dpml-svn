/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.state.test;

import net.dpml.state.Action;
import net.dpml.state.Trigger;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.impl.DefaultOperation;
import net.dpml.state.impl.DefaultTrigger;

/**
 * Default trigger test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultTriggerTestCase extends AbstractEncodingTestCase
{
    private TriggerEvent m_event;
    private Action m_action;
    private Trigger m_trigger;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_event = Trigger.INITIALIZATION;
        m_action = new DefaultOperation( "op" );
        m_trigger = new DefaultTrigger( m_event, m_action );
    }
    
   /**
    * Test event accessor.
    * @exception Exception if an error occurs
    */
    public void testEvent() throws Exception
    {
        assertEquals( "event", m_event, m_trigger.getEvent() );
    }
    
   /**
    * Test action accessor.
    * @exception Exception if an error occurs
    */
    public void testAction() throws Exception
    {
        assertEquals( "action", m_action, m_trigger.getAction() );
    }
    
   /**
    * Test trigger encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        Trigger trigger = (Trigger) executeEncodingTest( m_trigger, "simple-trigger-encoded.xml" );
        assertEquals( "original-equals-encoded", m_trigger, trigger );
    }
    
   /**
    * Test that the constructor throws an NPE if supplied with null arguments.
    * @exception Exception if an error occurs
    */
    public void testNulls() throws Exception
    {
        try
        {
            new DefaultTrigger( null, null );
            fail( "NPE not thown" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
    }
    
   /**
    * Test that the constructor throws an NPE if supplied with a null name.
    * @exception Exception if an error occurs
    */
    public void testNullName() throws Exception
    {
        try
        {
            new DefaultTrigger( null, m_action );
            fail( "NPE not thown for null name" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
    }
    
   /**
    * Test that the constructor throws an NPE if supplied with a null target.
    * @exception Exception if an error occurs
    */
    public void testNullTrigger() throws Exception
    {
        try
        {
            new DefaultTrigger( m_event, null );
            fail( "NPE not thown for null target" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
    }
}



