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

import net.dpml.state.State;
import net.dpml.state.Transition;
import net.dpml.state.Operation;
import net.dpml.state.Interface;
import net.dpml.state.Trigger;
import net.dpml.state.impl.DefaultOperation;
import net.dpml.state.impl.DefaultInterface;
import net.dpml.state.impl.DefaultTransition;
import net.dpml.state.impl.DefaultState;

/**
 * Default state test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultStateTestCase extends AbstractEncodingTestCase
{
    private String m_name;
    private Transition[] m_transitions;
    private Operation[] m_operations;
    private Interface[] m_interfaces;
    private State[] m_states;
    private Trigger[] m_triggers;
    private State m_state;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        m_name = "test";
        m_transitions = 
          new Transition[]
          {
            new DefaultTransition( "transition-one", "some-target", new DefaultOperation( "demo" ) )
          };
        m_operations = 
          new Operation[]
          {
            new DefaultOperation( "audit" )
          };
        m_interfaces = 
          new Interface[]
          {
            new DefaultInterface( "net.dpml.activity.Executable" )
          };
        m_states = 
          new State[]
          {
            new DefaultState( "sub-one", new Trigger[0], new Transition[0], new Interface[0], new Operation[0], new State[0] ),
            new DefaultState( "sub-two" ),
          };
        m_triggers = 
          new Trigger[]
          {
          };
        m_state = 
          new DefaultState(
            m_name, m_triggers, m_transitions, m_interfaces, m_operations, m_states );
    }
    
   /**
    * Test name accessor.
    * @exception Exception if an error occurs
    */
    public void testState() throws Exception
    {
        assertEquals( "name", m_name, m_state.getName() );
    }
    
   /**
    * Test triggers accessor.
    * @exception Exception if an error occurs
    */
    public void testTriggers() throws Exception
    {
        assertEquals( "triggers", m_triggers, m_state.getTriggers() );
    }

   /**
    * Test transitions accessor.
    * @exception Exception if an error occurs
    */
    public void testTransitions() throws Exception
    {
        assertEquals( "transitions", m_transitions, m_state.getTransitions() );
    }
    
   /**
    * Test operations accessor.
    * @exception Exception if an error occurs
    */
    public void testOperations() throws Exception
    {
        assertEquals( "operations", m_operations, m_state.getOperations() );
    }

   /**
    * Test interfaces accessor.
    * @exception Exception if an error occurs
    */
    public void testInterfacess() throws Exception
    {
        assertEquals( "interrfaces", m_interfaces, m_state.getInterfaces() );
    }

   /**
    * Test states accessor.
    * @exception Exception if an error occurs
    */
    public void testStates() throws Exception
    {
        assertEquals( "states", m_states, m_state.getStates() );
    }
    
   /**
    * Test states encoding/decoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        State state = (State) executeEncodingTest( m_state, "simple-state-encoded.xml" );
        assertEquals( "origin-equals-encoded", m_state, state );
    }

   /**
    * Test terminal falg accessor.
    * @exception Exception if an error occurs
    */
    public void testTerminalState() throws Exception
    {
        State state = new DefaultState( "terminal" );
        assertTrue( "terminal-is-terminal", state.isTerminal() );
    }
}



