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
import java.util.ArrayList;
import java.util.List;

import net.dpml.state.State;
import net.dpml.state.Transition;
import net.dpml.state.Operation;
import net.dpml.state.Interface;
import net.dpml.state.Trigger;
import net.dpml.state.Action;
import net.dpml.state.impl.DefaultStateMachine;
import net.dpml.state.impl.StateDecoder;

import net.dpml.util.EventQueue;
import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

/**
 * Default state machine test-case.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultStateMachineTestCase extends AbstractEncodingTestCase
{
    private State m_state;
    private EventQueue m_queue;
    private DefaultStateMachine m_machine;
    private Logger m_logger;
    
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "example.xgraph" );
        try
        {
            StateDecoder builder = new StateDecoder();
            m_state = builder.loadState( example.toURI() );
            m_logger = new DefaultLogger( "test" );
            m_queue = new EventQueue( m_logger );
            m_machine = new DefaultStateMachine( m_queue, m_logger, m_state );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            fail( e.toString() );
        }
    }
    
   /**
    * List the state graph.
    * @exception Exception if an error occurs
    */
    public void testAudit() throws Exception
    {
        audit( m_state );
    }
    
   /**
    * Test state machine validation function.
    * @exception Exception if an error occurs
    */
    public void testValidation() throws Exception
    {
        DefaultStateMachine.validate( m_state );
    }
    
   /**
    * Test state machine initialization and termination internal state integrity.
    * @exception Exception if an error occurs
    */
    public void testExecution() throws Exception
    {
        // check that the termination action is null
        
        Action termination = m_machine.getTerminationAction();
        assertNull( "root-termination", termination );
        
        // check that the initialization action is correct
        
        Action action = m_machine.getInitializationAction();
        if( action instanceof Transition )
        {
            Transition transition = (Transition) action;
            String name = transition.getName();
            assertEquals( "init-transition-name", "init", name );
        }
        else
        {
            fail( "Root initialization action is not a transition." );
        }
        
        // initialize the state 
        
        State state = m_machine.initialize( null );
        assertEquals( "post-init-state", "started", state.getName() );
        State machineState = m_machine.getState();
        assertEquals( "machine-state", "started", machineState.getName() );
        
        if( action != m_machine.getInitializationAction() )
        {
            fail( 
              "Unexpected initialization action [" 
              + m_machine.getInitializationAction() 
              + "] declared by statemachine." );
        }
        
        // check that the termination action has changed to the overridden 'stop' transition
        
        termination = m_machine.getTerminationAction();
        assertNotNull( "termination-in-start", termination );
        assertEquals( "action-is-stop", "stop", termination.getName() );
        
        //
        // test if the 'stop' transition is accessible
        //
        
        Transition[] transitions = m_machine.getTransitions();
        assertEquals( "started-transition-count", 1, transitions.length );
        Transition stop = transitions[0];
        assertEquals( "stop-transition-name", "stop", stop.getName() );
        
        //
        // test available operations
        //
        
        Operation[] operations = m_machine.getOperations();
        assertEquals( "started-operation-count", 1, operations.length );
        Operation audit = operations[0];
        assertEquals( "audit-operation-name", "audit", audit.getName() );
        
        //
        // apply the stop transition
        //
        
        m_machine.apply( stop.getName(), null );
        assertEquals( "stopped-state-name", "stopped", m_machine.getState().getName() );
        
        //
        // restart
        //
        
        m_machine.apply( "start", null );
        assertEquals( "re-started-state-name", "started", m_machine.getState().getName() );
        
        //
        // terminate
        //
        
        m_machine.terminate( null );
        assertEquals( "terminated-state-name", "terminated", m_machine.getState().getName() );
    }
    
   /**
    * Test illegal attempt to initialize a disposed state machine.
    * @exception Exception if an error occurs
    */
    public void testInitializationInDisposed() throws Exception
    {
        m_machine.dispose();
        try
        {
            m_machine.initialize( null );
            fail( "disposed machine allowed initialization" );
        }
        catch( IllegalStateException e )
        {
            // success
        }
    }
    
   /**
    * Test illegal attempt to terminate a disposed state machine.
    * @exception Exception if an error occurs
    */
    public void testTerminationInDisposed() throws Exception
    {
        m_machine.dispose();
        try
        {
            m_machine.terminate( null );
            fail( "disposed machine allowed termination" );
        }
        catch( IllegalStateException e )
        {
            // success
        }
    }
    
   /**
    * Test encoding of a state graph.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        Object state = executeEncodingTest( m_state );
    }
    
    private void audit( State state )
    {
        ArrayList list = new ArrayList();
        audit( list, state, "" );
        System.out.println( "" + "# -------------------" );
    }
    
    private void audit( List visited, State state, String pad  )
    {
        if( visited.contains( state ) )
        {
            return;
        }
        else
        {
            visited.add( state );
        }
        System.out.println( pad + "# -------------------" );
        System.out.println( pad + "# state: [" + state.getName() + "]" );
        Trigger[] triggers = state.getTriggers();
        for( int i=0; i<triggers.length; i++ )
        {
            Trigger trigger = triggers[i];
            System.out.println( 
              pad + "# trigger " 
              + "(" + ( i+1 ) + "): [" 
              + trigger.toString() 
              + "]" );
        }
        Transition[] transitions = state.getTransitions();
        for( int i=0; i<transitions.length; i++ )
        {
            Transition transition = transitions[i];
            System.out.println( 
              pad + "# transition " 
              + "(" + ( i+1 ) + "): [" 
              + transition.getName() 
              + "] --> [" + transition.getTargetName() 
              + "]" );
        }
        Interface[] classes = state.getInterfaces();
        for( int i=0; i<classes.length; i++ )
        {
            Interface interfaceDef = classes[i];
            System.out.println( 
              pad + "# interface " 
              + "(" + ( i+1 ) + "): [" 
              + interfaceDef.getClassname() 
              + "]" );
        }
        Operation[] operations = state.getOperations();
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            System.out.println( 
              pad + "# operation " 
              + "(" + ( i+1 ) + "): [" 
              + operation.getName() 
              + "]" );
        }
        State[] states = state.getStates();
        for( int i=0; i<states.length; i++ )
        {
            State s = states[i];
            audit( visited, s, pad + "  " );
        }
    }
}



