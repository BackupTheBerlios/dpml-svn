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

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import net.dpml.state.*;
import net.dpml.state.Trigger;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.impl.*;

/**
 * State testcase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultTriggerTestCase extends AbstractEncodingTestCase
{
    private TriggerEvent m_event;
    private Action m_action;
    private Trigger m_trigger;
    
    public void setUp() throws Exception
    {
        m_event = Trigger.INITIALIZATION;
        m_action = new DefaultOperation( "op", new URI( "method:xyz" ) );
        m_trigger = new DefaultTrigger( m_event, m_action );
    }
    
    public void testEvent() throws Exception
    {
        assertEquals( "event", m_event, m_trigger.getEvent() );
    }
    
    public void testAction() throws Exception
    {
        assertEquals( "action", m_action, m_trigger.getAction() );
    }
    
    public void testEncoding() throws Exception
    {
        Trigger trigger = (Trigger) executeEncodingTest( m_trigger, "simple-trigger-encoded.xml" );
        assertEquals( "original-equals-encoded", m_trigger, trigger );
    }
    
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
        try
        {
            new DefaultTrigger( null, m_action );
            fail( "NPE not thown for null name" );
        }
        catch( NullPointerException e )
        {
            // ok
        }
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



