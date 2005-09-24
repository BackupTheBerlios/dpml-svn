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

package net.dpml.state.impl.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.net.URI;
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
public class StateTestCase extends AbstractEncodingTestCase
{
    State m_state;
    StateMachine m_machine;
    
    public void setUp() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "example.xgraph" );
        FileInputStream input = new FileInputStream( example );
        BufferedInputStream buffer = new BufferedInputStream( input );
        try
        {
            m_state = StateMachine.load( buffer );
            m_machine = new StateMachine( m_state );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            fail( e.toString() );
        }
        finally
        {
            input.close();
        }
    }
    
    public void testAudit() throws Exception
    {
        audit( m_state );
    }
    
    public void testValidation() throws Exception
    {
        StateMachine.validate( m_state );
    }
    
    public void testRootStateName()
    {
        State state = m_state;
        assertEquals( "name", "", state.getName() );
    }
    
    public void testRootStates()
    {
        State[] states = m_state.getStates();
        assertEquals( "count", 2, states.length );
    }

    public void testExecution()
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

    public void testEncoding() throws Exception
    {
        Object state = executeEncodingTest( m_state, "state-encoded.xml" );
    }
    
    private void audit( State state )
    {
        ArrayList list = new ArrayList();
        audit( list, state, "" );
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
        //if( null != state.getInitialization() )
        //{
        //    System.out.print( " --> [" + state.getInitialization().getTargetName()  + "]" );
        //}
        Trigger[] triggers = state.getTriggers();
        for( int i=0; i<triggers.length; i++ )
        {
            Trigger trigger = triggers[i];
            System.out.println( 
              pad + "# trigger " 
              + "(" + (i+1) + "): [" 
              + trigger.toString() 
              + "]" );
        }
        Transition[] transitions = state.getTransitions();
        for( int i=0; i<transitions.length; i++ )
        {
            Transition transition = transitions[i];
            System.out.println( 
              pad + "# transition " 
              + "(" + (i+1) + "): [" 
              + transition.getName() 
              + "] --> [" + transition.getTargetName() 
              + "]" );
        }
        Operation[] operations = state.getOperations();
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            System.out.println( 
              pad + "# operation " 
              + "(" + (i+1) + "): [" 
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



