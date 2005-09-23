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
    StateMachine machine = new StateMachine();
    State m_state;
    
    public void setUp() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "example.xgraph" );
        FileInputStream input = new FileInputStream( example );
        BufferedInputStream buffer = new BufferedInputStream( input );
        try
        {
            m_state = machine.loadGraph( buffer );
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
    
    public void testRootStateName()
    {
        State state = m_state;
        assertEquals( "name", "", state.getName() );
    }
    
    //public void testRootInitialization()
    //{
    //    Initialization init = m_state.getInitialization();
    //    assertEquals( "target", "initialized", init.getTargetName() );
    //}
    
    //public void testRootTermination()
    //{
    //    Termination exit = m_state.getTermination();
    //    assertEquals( "target", "terminated", exit.getTargetName() );
    //}
    
    public void testRootStates()
    {
        State[] states = m_state.getStates();
        System.out.println( "# count: " + states.length );
        //assertEquals( "count", 3, states.length );
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
        State[] states = state.getLocalStates();
        for( int i=0; i<states.length; i++ )
        {
            State s = states[i];
            System.out.println( pad + "# state " + "(" + (i+1) + "): [" + s.getName() + "]" );
        }
        State[] local = state.getLocalStates();
        for( int i=0; i<local.length; i++ )
        {
            State s = local[i];
            audit( visited, s, pad + "  " );
        }
        //if( null != state.getTermination() )
        //{
        //    System.out.println( pad + "# <-- [" + state.getTermination().getTargetName() + "]" );
        //}
    }
}



