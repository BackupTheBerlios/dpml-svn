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
import net.dpml.state.impl.*;

/**
 * State testcase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultStateTestCase extends AbstractEncodingTestCase
{
    private String m_name;
    private Transition[] m_transitions;
    private Operation[] m_operations;
    private State[] m_states;
    private Trigger[] m_triggers;
    private State m_state;
    
    public void setUp() throws Exception
    {
        m_name = "test";
        m_transitions = 
          new Transition[]
          {
            new DefaultTransition( "transition-one", "some-target", new URI( "method:dosometing" ) )
          };
        m_operations = 
          new Operation[]
          {
            new DefaultOperation( "audit" )
          };
        m_states = 
          new State[]
          {
            new DefaultState( "sub-one", new Trigger[0], new Transition[0], new Operation[0], new State[0] ),
            new DefaultState( "sub-two" ),
          };
        m_triggers = 
          new Trigger[]
          {
          };
        m_state = 
          new DefaultState(
            m_name, m_triggers, m_transitions, m_operations, m_states );
    }
    
    public void testState() throws Exception
    {
        assertEquals( "name", m_name, m_state.getName() );
    }
    
    public void testTriggers() throws Exception
    {
        assertEquals( "triggers", m_triggers, m_state.getTriggers() );
    }

    public void testTransitions() throws Exception
    {
        assertEquals( "transitions", m_transitions, m_state.getTransitions() );
    }
    
    public void testOperations() throws Exception
    {
        assertEquals( "operations", m_operations, m_state.getOperations() );
    }

    public void testStates() throws Exception
    {
        assertEquals( "states", m_states, m_state.getStates() );
    }
    
    public void testEncoding() throws Exception
    {
        State state = (State) executeEncodingTest( m_state, "simple-state-encoded.xml" );
        assertEquals( "origin-equals-encoded", m_state, state );
    }

    public void testTerminalState() throws Exception
    {
        State state = new DefaultState( "terminal" );
        assertTrue( "terminal-is-terminal", state.isTerminal() );
    }
}



