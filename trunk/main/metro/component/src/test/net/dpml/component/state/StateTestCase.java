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

package net.dpml.component.state;

import java.net.URI;

import junit.framework.TestCase;

/**
 * Test creation of a state model.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class StateTestCase extends TestCase
{
    private State m_root = null;

   /**
    * Test the State as a vehicle for the creation of a state model.
    */
    public void setUp() throws Exception
    {
        State root = new State();
        root.addOperation( "foo", new URI( "handler:foo" ) );
        root.addOperation( "bar", new URI( "handler:bar" ) );
        m_root = root;
    }

    public void testRootName() throws Exception
    {
        String name = m_root.getName();
        assertEquals( "root name", "root", name );
    }

    public void testRootParent() throws Exception
    {
        State parent = m_root.getParent();
        assertNull( "root parent", parent );
    }

    public void testRootNotTerminal() throws Exception
    {
        assertFalse( "root is terminal", m_root.isTerminal() );
    }

    public void testRootTerminal() throws Exception
    {  
        State root = new State( true );
        assertTrue( "is terminal", root.isTerminal() );
    }

    public void testRootOperations() throws Exception
    {
        String[] operations = m_root.getOperationNames();
        if( operations.length < 2 )
        {
            fail( "root operation count is less than 2" );
        }
    }

    public void testStateCount() throws Exception
    {
        State[] states = m_root.getStates();
        if( states.length > 0 )
        {
            fail( "nested state count is not 0" );
        }
    }

    public void testAddStateWithNullKey() throws Exception
    {
        try
        {
            m_root.addState( null );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testAddState() throws Exception
    {
        String name = "running";
        m_root.addState( name );
        State[] states = m_root.getStates();
        if( states.length != 1 )
        {
            fail( "sub-state count != 1" );
        }
    }

    public void testAddDuplicateState() throws Exception
    {
        String name = "running";
        m_root.addState( name );
        try
        {
            m_root.addState( name );
        }
        catch( DuplicateKeyException e )
        {
            // success
        }        
        State[] states = m_root.getStates();
        if( states.length != 1 )
        {
            fail( "sub-state count != 1" );
        }
    }

    public void testAddTerminalState() throws Exception
    {
        String name = "running";
        State terminal = m_root.addTerminalState( name );
        State[] states = m_root.getStates();
        if( states.length != 1 )
        {
            fail( "sub-state count != 1" );
        }
    }

    public void testTerminalState() throws Exception
    {
        String name = "running";
        State terminal = m_root.addTerminalState( name );
        assertTrue( "terminal state is terminal", terminal.isTerminal() );
    }

    public void testAddDuplicateTerminalState() throws Exception
    {
        String name = "running";
        m_root.addTerminalState( name );
        try
        {
            m_root.addState( name );
        }
        catch( DuplicateKeyException e )
        {
            // success
        }        
        State[] states = m_root.getStates();
        if( states.length != 1 )
        {
            fail( "sub-state count != 1" );
        }
    }

    public void testLocalStateCount() throws Exception
    {
        m_root.addState( "aaa" );
        m_root.addState( "bbb" );
        m_root.addState( "ccc" );
        State[] states = m_root.getStates();
        if( states.length != 3 )
        {
            fail( "sub-state count != 3" );
        }
    }

    public void testAllStatesCount() throws Exception
    {
        m_root.addState( "aaa" );
        m_root.addState( "bbb" );
        m_root.addState( "ccc" );
        if( m_root.getAllStates().length != 4 )
        {
            fail( "total-state count != 4" );
        }
    }

    public void testAllStatesCountInGraph() throws Exception
    {
        int n = 1;
        for( int i=0; i<10; i++ )
        {
            State s = m_root.addState( "" + i );
            n++;
        }
        State[] states = m_root.getStates( m_root );
        for( int i=0; i<states.length; i++ )
        {
            State p = states[i];
            for( int j=0; j<10; j++ )
            {
                p.addState( p.getName() + "." + j );
                n++;
            }
        }
        if( m_root.getAllStates().length != n )
        {
            fail( "sub-state count != " + n );
        }
    }

    public void testStatesCountInTarget() throws Exception
    {
        int n = 0;
        State foo = m_root.addState( "foo" );
        for( int i=0; i<10; i++ )
        {
            State s = foo.addState( "" + i );
            n++;
        }
        State[] states = foo.getStates();
        if( states.length != n )
        {
            fail( "sub-state count " + states.length + " != " + n );
        }
    }

    public void testStatesCountInSuppliedTarget() throws Exception
    {
        int n = 0;
        State foo = m_root.addState( "foo" );
        for( int i=0; i<10; i++ )
        {
            State s = foo.addState( "" + i );
            n++;
        }
        State[] states = m_root.getStates( foo );
        if( states.length != n )
        {
            fail( "sub-state count " + states.length + " != " + n );
        }
    }

    public void testGetStateWithNull() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.getState( null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testGetInvalidState() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.getState( "bar" );
            fail( "do not throw NSSE" );
        }
        catch( NoSuchStateException e )
        {
            // success
        }        
    }

    public void testRootStateRetrieval() throws Exception
    {
        State foo = m_root.addState( "foo" );
        State s = m_root.getState( "foo" );
        assertEquals( "testing created is retrieved", foo, s );
    }

    public void testSubStateRetrieval() throws Exception
    {
        State foo = m_root.addState( "foo" );
        State s = foo.getState( "foo" );
        assertEquals( "testing created is retrieved", foo, s );
    }

    public void testAddTransitionNullKey() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.addTransition( null, new URI( "method:null" ), foo );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testAddTransitionNullURI() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.addTransition( "to-foo", null, foo );
        }
        catch( NullPointerException e )
        {
            fail( "null uris are allowed in transitions" );
        }        
    }

    public void testAddTransitionNullTarget() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.addTransition( "to-foo", new URI( "method:null" ), (State) null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testAddTransitionBadTarget() throws Exception
    {
        State foo = new State();
        try
        {
            m_root.addTransition( "to-foo", new URI( "method:null" ), foo );
            fail( "do not throw ISE" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }        
    }

    public void testAddTransitionInTerminal() throws Exception
    {
        State foo = m_root.addTerminalState( "foo" );
        try
        {
            foo.addTransition( "from-foo", new URI( "method:null" ), m_root );
            fail( "do not throw ISE" );
        }
        catch( IllegalStateException e )
        {
            // success
        }        
    }

    public void testAddTransitionToSelf() throws Exception
    {
        try
        {
            m_root.addTransition( "nowhere", new URI( "method:null" ), m_root );
            fail( "do not throw IAE" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }

    public void testSetInitializationNullURI() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.setInitialization( null, foo );
        }
        catch( NullPointerException e )
        {
            fail( "null uris are allowed in initializers" );
        }        
    }

    public void testSetInitializationNullTarget() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.setInitialization( new URI( "system:null" ), (State) null );
            fail( "No NPE." );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testSetInitializationBadTarget() throws Exception
    {
        State foo = new State();
        try
        {
            m_root.setInitialization( new URI( "method:null" ), foo );
            fail( "do not throw ISE" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }        
    }

    public void testSetInitializationInTerminal() throws Exception
    {
        State foo = m_root.addTerminalState( "foo" );
        try
        {
            foo.setInitialization( new URI( "method:null" ), m_root );
            // success
        }
        catch( IllegalStateException e )
        {
            fail( "Initializations in terminals are allowed." );
        }        
    }

    public void testTerminatorNullURI() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.setTerminator( null, foo );
        }
        catch( NullPointerException e )
        {
            fail( "null uris are allowed in terminators" );
        }        
    }

    public void testTerminatorNullTarget() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.setTerminator( new URI( "system:null" ), (State) null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testSetTerminatorBadTarget() throws Exception
    {
        State foo = new State();
        try
        {
            m_root.setTerminator( new URI( "method:null" ), foo );
            fail( "do not throw ISE" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }

    public void testSetTerminatorInTerminal() throws Exception
    {
        State foo = m_root.addTerminalState( "foo" );
        foo.setTerminator( m_root );
    }

    public void testGetTransition() throws Exception
    {
        String name = "bar";
        URI uri = new URI( "method:null" );
        State foo = m_root.addState( "foo" );
        m_root.addTransition( name, uri, foo );
        Transition bar = m_root.getTransition( name );
        assertEquals( "target", foo, bar.getTargetState() );
    }

    public void testGetTransitionWithInvalidKey() throws Exception
    {
        try
        {
            m_root.getTransition( "fred" );
            fail( "do not throw NSTE" );
        }
        catch( NoSuchTransitionException e )
        {
            // success
        }
    }

    public void testGetTransitionWithNullKey() throws Exception
    {
        try
        {
            m_root.getTransition( null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testGetNamedTransition() throws Exception
    {
        String name = "bar";
        URI uri = new URI( "method:null" );
        State foo = m_root.addState( "foo" );
        m_root.addTransition( name, uri, foo );
        Transition bar = m_root.getNamedTransition( name );
        assertEquals( "target", foo, bar.getTargetState() );
        assertEquals( "handler", uri, bar.getHandlerURI() );
    }

    public void testGetNamedTransitionWithNullKey() throws Exception
    {
        try
        {
            m_root.getNamedTransition( null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testGetNamedTransitionWithInvalidKey() throws Exception
    {
        try
        {
            m_root.getNamedTransition( "fred" );
            fail( "do not throw NSTE" );
        }
        catch( NoSuchTransitionException e )
        {
            // success
        }
    }

    public void testAddOperationNullKey() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.addOperation( null, new URI( "method:null" ) );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testAddOperationNullURI() throws Exception
    {
        State foo = m_root.addState( "foo" );
        try
        {
            m_root.addOperation( "key", null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }

    public void testAddDuplicateOperation() throws Exception
    {
        m_root.addOperation( "aaa", new URI( "method:null" ) );
        try
        {
            m_root.addOperation( "aaa", new URI( "method:null" ) );
            fail( "do not throw DKE" );
        }
        catch( DuplicateKeyException e )
        {
            // success
        }        
    }

    public void testAddDuplicateOperationWithPolicy() throws Exception
    {
        m_root.addOperation( "aaa", new URI( "method:null" ) );
        try
        {
            m_root.addOperation( "aaa", new URI( "method:null" ), false );
            fail( "do not throw DKE" );
        }
        catch( DuplicateKeyException e )
        {
            // success
        }        
    }

    public void testReplaceOperation() throws Exception
    {
        URI bar = new URI( "method:bar" );
        m_root.addOperation( "aaa", new URI( "method:foo" ) );
        m_root.addOperation( "aaa", bar, true );
        Operation operation = m_root.getNamedOperation( "aaa" );
        assertEquals( "uri", bar, operation.getHandlerURI() );
    }

    public void testGetOperationNullKey() throws Exception
    {
        try
        {
            m_root.getOperation( null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testGetOperationBadKey() throws Exception
    {
        try
        {
            m_root.getOperation( "xyz" );
            fail( "do not throw NSOE" );
        }
        catch( NoSuchOperationException e )
        {
            // success
        }        
    }

    public void testGetNamedOperationNullKey() throws Exception
    {
        try
        {
            m_root.getNamedOperation( null );
            fail( "do not throw NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }        
    }

    public void testGetNamedOperationBadKey() throws Exception
    {
        try
        {
            m_root.getNamedOperation( "xyz" );
            fail( "do not throw NSOE" );
        }
        catch( NoSuchOperationException e )
        {
            // success
        }        
    }

    public void testGetLocalOperationNames() throws Exception
    {  
        URI uri = new URI( "handler:gizmo" );
        State foo = m_root.addState( "foo" );
        foo.addOperation( "widget", uri );
        foo.addOperation( "gizmo", uri );
        String[] operations = foo.getLocalOperationNames();
        assertEquals( "operations", operations.length, 2 );
    }

    public void testGetNamedOperation() throws Exception
    {
        URI uri = new URI( "handler:gizmo" );
        State foo = m_root.addState( "foo" );
        foo.addOperation( "widget", uri );
        Operation operation = foo.getNamedOperation( "widget" );
        assertNotNull( "operation", operation );
        assertEquals( "uri", uri, operation.getHandlerURI() );
    }

    public void testGetOperationNames() throws Exception
    {  
        URI uri = new URI( "handler:gizmo" );
        int n = m_root.getOperationNames().length;
        State foo = m_root.addState( "foo" );
        foo.addOperation( "widget", uri );
        int m = m_root.getOperationNames().length;
        assertEquals( "operation count", m, n ); // we don't see child operations
    }

    public void testGetNestedOperationNames() throws Exception
    {
        URI uri = new URI( "handler:gizmo" );
        int n = m_root.getOperationNames().length;
        State foo = m_root.addState( "foo" );
        foo.addOperation( "widget", uri );
        int m = foo.getOperationNames().length;
        assertEquals( "operation count", m, n+1 ); // we see local plus parent operations
    }

    public void testGraphOperationCount() throws Exception
    {
        State root = createComplexState();
        State started = root.getState( "started" );
        assertEquals( "operations", started.getOperationNames().length, 3 );
    }

    public void testGraphTransitionCount() throws Exception
    {
        State root = createComplexState();
        State started = root.getState( "started" );
        assertEquals( "transitions", started.getTransitionNames().length, 1 );
    }

    public void testList() throws Exception
    {
        State root = createComplexState();
        root.list();
    }

    public State createComplexState() throws Exception
    {
        //
        // construct a state graph
        //

        State root = new State();
        State initialized = root.addState( "initialized" );
        State available = root.addState( "available" );
        State started = available.addState( "started" );
        State stopped = available.addState( "stopped" );
        State terminated = root.addTerminalState( "terminated" );

        //
        // add operations and transitions
        //

        root.setInitialization( new URI( "handler:demo" ), initialized );
        initialized.setTerminator( new URI( "handler:demo" ), terminated );
        available.addTransition( "start", new URI( "handler:demo" ), started );
        root.addOperation( "foo", new URI( "handler:bar" ) );
        started.addTransition( "stop", new URI( "handler:demo" ), stopped );
        started.addOperation( "do-this", new URI( "part:abc" ) );
        started.addOperation( "do-that", new URI( "method:cde" ) );
        stopped.addTransition( "start", new URI( "handler:demo" ), started );

        return root;
    }

}



