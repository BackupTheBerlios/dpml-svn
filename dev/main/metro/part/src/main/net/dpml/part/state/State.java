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

package net.dpml.part.state;

import java.io.Serializable;
import java.util.Map;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URI;

/**
 * The State interface is an interface representing an immutable state of 
 * a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public final class State implements Serializable
{
    private final String m_name;
    private final State m_parent;
    private final boolean m_terminal;

    private final Map m_operations = new Hashtable();
    private final Map m_transitions = new Hashtable();

    private Transition m_initialization;
    private Transition m_terminator;

    private final Map m_states = new Hashtable();

   /**
    * Creation of a new top-level non-terminal state.
    */
    public State()
    {
        this( false );
    }

   /**
    * Creation of a new top-level state.
    * @param terminal the terminal status of the state
    */
    public State( boolean terminal )
    {
        this( null, "root", terminal );
    }

   /**
    * Internal constructor for a state instance.  If the parent argument is null
    * the created state representing a root state in a state graph.  The terminal
    * attribute declares the terminal node status of the state.  If the state is
    * flagged as terminal, no sub-states or transitions may be added or assigned
    * to the state.
    *
    * @paren parent the parent of the created state
    * @param name the name that the state is index under
    * @param terminal TRUE if this is a terminal state else FALSE
    * @exception NullPointerException if the name argument is null
    */
    private State( State parent, String name, boolean terminal )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        m_parent = parent;
        m_terminal = terminal;
    }

   /**
    * Return the name of the state.
    *
    * @return the state name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the parent state.
    * @return the parent
    */
    protected State getParent()
    {
        return m_parent;
    }

   /**
    * Return the terminal status of this state.  If a state is terminal it cannot
    * contain sub-states or transitions.  Furthermore, a component declaring a current 
    * state where the current state is terminal cannot change it's state without explicit
    * re-initialization of the componet's service manager.
    * 
    * @return TRUE if this state is a terminal state
    */
    public boolean isTerminal()
    {
        return m_terminal;
    }

   /**
    * Add a new sub-state to the set of states contained within this 
    * state. If the supplied key is not unique within the scope of the graph
    * an exception will be thrown.
    *
    * @param key the key identifying the new state 
    * @return the created state
    */
    public State addState( String key )
    {
        return addState( this, key, false );
    }

   /**
    * Add a new terminal sub-state to the set of states contained within this 
    * state. If the supplied key is not unique within the scope of the graph
    * an exception will be thrown.
    *
    * @param key the key identifying the new state 
    * @return the created state
    */
    public State addTerminalState( String key )
    {
        return addState( this, key, true );
    }

   /**
    * Internal operation to construct a new sub-state state. The implementation
    * will delegate the state addition to the parent under which all states in the 
    * state graph are aggregated relative to unique keys.
    *
    * @param parent the enclosing state
    * @param key the key identifying the new state
    * @param terminal true if this is a terminal state
    * @return the created state
    * @exception IllegalStateException if the parent state is a terminal state
    * @exception DuplicateKeyException if a state with the supplied key is already assingned
    * @exception IllegalArgumentException if the initial parameter is true and an initial 
    *     state has already beeen declared
    */
    private State addState( State parent, String key, boolean terminal )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( m_parent != null )
        {
            return m_parent.addState( parent, key, terminal );
        }
        else
        {
            if( parent.isTerminal() )
            {
                final String error = 
                  "Cannot add substate to a terminal state."
                  + "\nState: " + parent.getName()
                  + "\nSubstate: " + key;
                throw new IllegalStateException( error );
            }

            if( m_states.containsKey( key ) )
            {
                throw new DuplicateKeyException( key );
            }
            else
            {
                State state = new State( parent, key, terminal );
                m_states.put( key, state );
                return state;
            }
        }
    }

   /**
    * Return an array of all states within the state graph. This includes
    * all substates of the root state and all substates of substates, etc.
    *
    * @return the state array
    */
    protected State[] getAllStates()
    {
        if( null != m_parent )
        {
            return m_parent.getAllStates();
        }
        else
        {
            ArrayList list = new ArrayList( );
            State[] nested = 
              (State[]) m_states.values().toArray( new State[0] );
            State[] states = new State[ nested.length + 1 ];
            states[0] = this;
            System.arraycopy( nested, 0, states, 1, nested.length );
            return states;
        }
    }

   /**
    * Return an array of local states directly contained within this state
    * instance.
    *
    * @return the local state array
    */
    protected State[] getStates()
    {
        return getStates( this );
    }

   /**
    * Return an array of all states contained within the supplied state.
    *
    * @param parent the enclosing parent state
    * @return the state array
    */
    protected State[] getStates( State parent )
    {
        ArrayList list = new ArrayList();
        State[] states = getAllStates();
        for( int i=(states.length -1); i>-1; i-- )
        {
            State state = states[i];
            if( parent.equals( state.getParent() ) )
            {
                list.add( state );
            }
        }
        return (State[]) list.toArray( new State[0] );
    }

   /**
    * Return a state from the state graph matching the supplied key.
    * The search does not include the root state of the state graph.
    *
    * @param key the state key
    * @return the state assigned to the key
    * @exception NoSuchStateException if the key does not match a state
    *  within the state graph
    */
    public State getState( String key )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null != m_parent )
        {
            return m_parent.getState( key );
        }
        else
        {
            State state = (State) m_states.get( key );
            if( null == state )
            {
                throw new NoSuchStateException( key );
            }
            else
            {
                return state;
            }
        }
    }

   /**
    * Add a transition to the state. Transition keys are unique within the scope
    * of the state under which the transition is assigned.  Transitions with the 
    * same name as a transition within a parent state will take precedence when 
    * selecting transitions.
    *
    * @param key the transition key
    * @param target the transition target state 
    */
    public void addTransition( String key, State target )
    {
        addTransition( key, null, target );
    }

   /**
    * Add a transition to the state. Transition keys are unique within the scope
    * of the state under which the transition is assigned.  Transitions with the 
    * same name as a transition within a parent state will take precedence when 
    * selecting transitions.
    *
    * @param key the transition key
    * @param target the transition target state 
    */
    public void addTransition( String key, String target )
    {
        State state = getState( target );
        addTransition( key, state );
    }

   /**
    * Add a transition to the state. Transition keys are unique within the scope
    * of the state under which the transition is assigned.  Transitions with the 
    * same name as a transition within a parent state will take precedence when 
    * selecting transitions.
    *
    * @param key the transition key
    * @param uri the uri identifying the handler to be assigned as the handler 
    *    of the transition action
    * @param target the transition target state 
    */
    public void addTransition( String key, URI uri, State target )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( m_transitions.containsKey( key ) )
        {
            throw new DuplicateKeyException( key );
        }
        validateTransition( TRANSITION, key, uri, target );
        Transition transition = new Transition( uri, target );
        m_transitions.put( key, transition );
    }

   /**
    * Add a transition to the state. Transition keys are unique within the scope
    * of the state under which the transition is assigned.  Transitions with the 
    * same name as a transition within a parent state will take precedence when 
    * selecting transitions.
    *
    * @param key the transition key
    * @param uri the uri identifying the handler to be assigned as the handler 
    *    of the transition action
    * @param target the transition target state 
    */
    public void addTransition( String key, URI uri, String target )
    {
        State state = getState( target );
        addTransition( key, uri, state );
    }


   /**
    * Optionally set the initializer for this state. During the state manager 
    * initialization phase an initializer declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of an initializer,  any initializer associated with the new state will be 
    * fired.  This process will continue until a state is established that does not 
    * delcare an initializor.
    *
    * @param target the target initialization state 
    */
    public void setInitialization( String target )
    {
        State state = getState( target );
        setInitialization( state );
    }

   /**
    * Optionally set the initializer for this state. During the state manager 
    * initialization phase an initializer declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of an initializer,  any initializer associated with the new state will be 
    * fired.  This process will continue until a state is established that does not 
    * delcare an initializor.
    *
    * @param target the target initialization state 
    */
    public void setInitialization( State target )
    {
        setInitialization( null, target );
    }

   /**
    * Optionally set the initializer for this state. During the state manager 
    * initialization phase an initializer declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of an initializer,  any initializer associated with the new state will be 
    * fired.  This process will continue until a state is established that does not 
    * delcare an initializor.
    *
    * @param uri the initialization uri 
    */
    public void setInitialization( URI uri )
    {
        setInitialization( uri, this );
    }

   /**
    * Optionally set the initializer for this state. During the state manager 
    * initialization phase an initializer declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of an initializer,  any initializer associated with the new state will be 
    * fired.  This process will continue until a state is established that does not 
    * delcare an initializor.
    *
    * @param uri the uri identifying the handler to be assigned as the handler 
    *    of the initialization action
    * @param target the target initialization state 
    */
    public void setInitialization( URI uri, String target )
    {
        State state = getState( target );
        setInitialization( uri, state );
    }

   /**
    * Optionally set the initializer for this state. During the state manager 
    * initialization phase an initializer declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of an initializer,  any initializer associated with the new state will be 
    * fired.  This process will continue until a state is established that does not 
    * delcare an initializor.
    *
    * @param uri the uri identifying the handler to be assigned as the handler 
    *    of the initialization action
    * @param target the target initialization state 
    */
    public void setInitialization( URI uri, State target )
    {
        if( null != m_initialization )
        {
            final String error = 
              "Initilization already set."
              + "\nState: " + getName();
            throw new IllegalStateException( error );
        }
        validateTransition( INITIALIZER, null, uri, target );
        m_initialization = new Transition( uri, target );
    }

   /**
    * Return the initialization transition assigned to the state.
    * @return the initialization transition (possibly null)
    */
    public Transition getInitialization()
    {
        return m_initialization;
    }

   /**
    * Optionally set the terminator for this state. During the state manager 
    * termination phase a terminator declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of a terminator, a terminator associated with the new state will be 
    * fired (if declared).  This process will continue until a terminal state is 
    * established or no additional terminators can be fired.
    *
    * @param target the transition target state 
    */
    public void setTerminator( String target )
    {
        State state = getState( target );
        setTerminator( null, state );
    }

   /**
    * Optionally set the terminator for this state. During the state manager 
    * termination phase a terminator declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of a terminator, a terminator associated with the new state will be 
    * fired (if declared).  This process will continue until a terminal state is 
    * established or no additional terminators can be fired.
    *
    * @param target the transition target state 
    */
    public void setTerminator( State target )
    {
        setTerminator( null, target );
    }

   /**
    * Optionally set the terminator for this state. During the state manager 
    * termination phase a terminator declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of a terminator, a terminator associated with the new state will be 
    * fired (if declared).  This process will continue until a terminal state is 
    * established or no additional terminators can be fired.
    *
    * @param uri the uri identifying the handler to be assigned as the handler 
    *    of the transition action
    * @param target the transition target state 
    */
    public void setTerminator( URI uri, String target )
    {
        State state = getState( target );
        setTerminator( uri, state );
    }

   /**
    * Optionally set the terminator for this state. During the state manager 
    * termination phase a terminator declared on the assigned state model 
    * will be invoked.  If the current state is modified as a result of invocation 
    * of a terminator, a terminator associated with the new state will be 
    * fired (if declared).  This process will continue until a terminal state is 
    * established or no additional terminators can be fired.
    *
    * @param uri the uri identifying the handler to be assigned as the handler 
    *    of the transition action
    * @param target the transition target state 
    */
    public void setTerminator( URI uri, State target )
    {
        if( null != m_terminator )
        {
            final String error = 
              "Terminator already set."
              + "\nState: " + getName();
            throw new IllegalStateException( error );
        }
        validateTransition( TERMINATOR, null, uri, target );
        m_terminator = new Transition( uri, target );
    }

   /**
    * Return the terminator transition assigned to the state.
    * @return the terminator transition (possibly null)
    */
    public Transition getTerminator()
    {
        return m_terminator;
    }

   /**
    * Return the first transition matching the supplied key in this state graph.  The search 
    * for a matching transition proceeds from the local state upwards uptil the root state is 
    * reached unless a local or intermidiate transition matches the supplied key.
    *
    * @param key the transition key
    * @return a matching transition
    * @exception NoSuchTransitionException if no matching transition can be found
    */
    public Transition getTransition( String key )
    {
        return getNamedTransition( key );
    }

   /**
    * Return the fisrt transition matching the supplied key in this state graph.  The search 
    * for a matching transition proceeds from the local state upwards uptil the root state is 
    * reached unless a local or intermidiate transition matches the supplied key.
    *
    * @param key the transition key
    * @return a matching transition
    * @exception NoSuchTransitionException if no matching transition can be found
    */
    public Transition getNamedTransition( String key )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        Transition transition = (Transition) m_transitions.get( key );
        if( null != transition )
        {
            return transition;
        }
        else
        {
            if( null == m_parent )
            {
                throw new NoSuchTransitionException( key );
            }
            else
            {
                return m_parent.getNamedTransition( key );
            }
        }
    }

   /**
    * Returns an array of transition names declared in this state and all 
    * parent states in the active state chain.  This method via the 
    * StateManager interface (through exposeure of State) to controlling 
    * applications.  The controlling application can apply a tranistion 
    * by invoking the 'apply' operation on the associated ServiceManager.
    *
    * @return the available transition names
    */
    public String[] getTransitionNames()
    {
         if( m_terminal )
         {
             return new String[0];
         }
         if( null == m_parent )
         {
             return getLocalTransitionNames();
         }
         else
         {
             String[] keys = m_parent.getTransitionNames();
             String[] local = getLocalTransitionNames();
             List list = new ArrayList( Arrays.asList( local ) );
             for( int i=0; i<keys.length; i++ )
             {
                 String key = keys[i];
                 if( false == list.contains( key ) )
                 {
                     if( false == getNamedTransition( key ).getTargetState().equals( this ) )
                     {
                         list.add( key );
                     }
                 } 
             }
             return (String[]) list.toArray( new String[0] );
         }
    }

   /**
    * Return the list of transition names local to this state.
    * @return the array of transition names
    */   
    public String[] getLocalTransitionNames()
    {
         return (String[]) m_transitions.keySet().toArray( new String[0] );
    }

   /**
    * An operation is the declarion of the association of a handler with a particular
    * state.  When the active state chain includes the state the the operation is 
    * asssigned to the operation is available.  Operation do not modify the current 
    * state maintained by the state manager (unlike transitions). Operations can be
    * be considered as dynamic methods that are exposed to a controller as a function
    * of the active state of the component.  The operation key must be unique within 
    * the scope of the state to which it is assigned.  If the an operation with the 
    * same name is assigned to a parent or higher state then this operation will 
    * override the higher operation.
    *
    * @param key the operation key
    * @param uri the uri identifying the operation handler
    * @exception NullPointerException if key or uri are null
    * @exception DuplicateKeyException if the key is already locally assigned
    */
    public void addOperation( String key, URI uri )
    {
        addOperation( key, uri, false );
    }

   /**
    * An operation is the declarion of the association of a handler with a particular
    * state.  When the active state chain includes the state the the operation is 
    * asssigned to the operation is available.  Operation do not modify the current 
    * state maintained by the state manager (unlike transitions). Operations can be
    * be considered as dynamic methods that are exposed to a controller as a function
    * of the active state of the component.  The operation key must be unique within 
    * the scope of the state to which it is assigned.  If the an operation with the 
    * same name is assigned to a parent or higher state then this operation will 
    * override the higher operation.  If an operation already exists locally on this
    * state matching the supplied key and the replace is policy is true, the existing
    * operation will be replaced otherwise a DuplicateKeyException will be thrown.
    *
    * @param key the operation key
    * @param uri the uri identifying the operation handler
    * @param replace operation replacement policy
    * @exception NullPointerException if key or uri are null
    * @exception DuplicateKeyException if the key is already locally assigned and 
    *   the supplied replacement policy is FALSE
    */
    public void addOperation( String key, URI uri, boolean replace )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        if( false == replace )
        {
            if( m_operations.containsKey( key ) )
            {
                throw new DuplicateKeyException( key );
            }
        }
        Operation operation = new Operation( uri );
        m_operations.put( key, operation );
    }

   /**
    * Return the first operation matching the supplied key in this state graph.  The search 
    * for a matching operation proceeds from the local state upwards uptil the root state is 
    * reached unless a local or intermidiate operation matches the supplied key.
    *
    * @param key the operation key
    * @return a matching operation 
    * @exception NoSuchOperationException if no matching operation can be found
    */
    public Operation getOperation( String key )
    {
        return getNamedOperation( key );
    }

   /**
    * Return the fisrt operation matching the supplied key in this state graph.  The search 
    * for a matching operation proceeds from the local state upwards uptil the root state is 
    * reached unless a local or intermidiate operation matches the supplied key.
    *
    * @param key the operation key
    * @return a matching operation 
    * @exception NoSuchOperationException if no matching operation can be found
    */
    public Operation getNamedOperation( String key )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        Operation operation = (Operation) m_operations.get( key );
        if( null != operation )
        {
            return operation;
        }
        else
        {
            if( null == m_parent )
            {
                throw new NoSuchOperationException( key );
            }
            else
            {
                return m_parent.getNamedOperation( key );
            }
        }
    }

   /**
    * Returns an array of operation names declared in this state and all 
    * parent states in the active state chain.  This method is exposed via the 
    * StateManager interface (through exposure of State) to controlling 
    * applications.  The controlling application can execute an operation 
    * by invoking the 'execute' method on the associated ServiceManager.
    *
    * @return the available operation names
    */
    public String[] getOperationNames()
    {
        if( null == m_parent )
        {
            return getLocalOperationNames();
        }
        else
        {
            String[] keys = m_parent.getOperationNames();
            String[] local = getLocalOperationNames();
            String[] result = new String[ keys.length + local.length ];
            System.arraycopy( keys, 0, result, 0, keys.length );
            System.arraycopy( local, 0, result, keys.length, local.length  );
            return result;
        }
    }

   /**
    * Return the list of operation names local to this state.
    * @return the array of operation names
    */   
    public String[] getLocalOperationNames()
    {
         return (String[]) m_operations.keySet().toArray( new String[0] );
    }

   /**
    * return a string representation of this state.
    * @return the string representation
    */
    public String toString()
    {
        if( null != m_parent )
        {
            return "[state " + m_name + " (from:" + m_parent.getName() + ")]";
        }
        else
        {
            return "[state " + m_name + "]";
        }
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( this.hashCode() == other.hashCode() )
        {
            return true;
        }
        else if( other instanceof State )
        {
            State state = (State) other;
            return m_name.equals( state.getName() );
        }
        else
        {
            return false;
        }
    }

    private boolean equals( Object one, Object two )
    {
        if( null == one )
        {
            return ( null == two );
        }
        else
        {
            return one.equals( two );
        }
    }

    public int hashCode()
    {
        return m_name.hashCode();
    }

   /**
    * Untility function that returns a string representation of this state
    * in the form of a graph.
    * @return the string representation of the graph represented by this 
    *    node and all child nodes
    */
    public String list()
    {
        StringBuffer buffer = new StringBuffer();
        graph( buffer, this, "" );
        return buffer.toString();
    }

   /**
    * Internal utility to handle graph construction.
    * @param buffer a string buffer to write to
    * @param state the state to include 
    * @param offset a character offset to apply when writting out the state features
    */
    private void graph( StringBuffer buffer, State state, String offset )
    {
        String name = state.getName();
        buffer.append( "\n" + offset );
        buffer.append( "state: " + name );
        if( state.isTerminal() )
        {
            buffer.append( " (terminal)" );
        }
        Transition initializer = state.getInitialization();
        if( initializer != null )
        {
            buffer.append( 
              "\n  " + offset + "initializer:" + name + " --> state:" 
              + initializer.getTargetState().getName() );
        }
        Transition terminator = state.getTerminator();
        if( terminator != null )
        {
            buffer.append( 
              "\n  " + offset + "terminator:" + name + " --> state:" 
              + terminator.getTargetState().getName() );
        }
        String[] transitions = state.getLocalTransitionNames();
        for( int i=0; i<transitions.length; i++ )
        {
            String t = transitions[i];
            Transition transition = state.getNamedTransition( t );
            State target = transition.getTargetState();
            buffer.append( "\n  " + offset + "transition:" + t + " --> state:" + target.getName() );
        }
        String[] operations = state.getLocalOperationNames();
        for( int i=0; i<operations.length; i++ )
        {
            String o = operations[i];
            Operation operation = state.getNamedOperation( o );
            buffer.append( "\n  " + offset + "operation:" + o );
        }

        State[] states = state.getStates();
        for( int i=0; i<states.length; i++ )
        {
            State s = states[i];
            graph( buffer, s, offset + "  " );
        }
    }

    private void validateTransition( String role, String key, URI uri, State target )
    {
        validateIsaMember( role, key, target );
        validateNonNullTarget( role, key, target );
        if( role.equals( TERMINATOR ) )
        {
            validateNotSelf( role, key, target );
        }
        else if( role.equals( TRANSITION ) )
        {
            validateNotSelf( role, key, target );
            validateNonTerminal( role, key, target );
        }
    }

    private void validateNotSelf( String role, String key, State target )
    {
        if( this == target )
        {
            String keyLine = "";
            if( null != key )
            {
                keyLine = "\nKey: " + key;
            }
            final String error = 
              "Cannot add " + role 
              + " because "
              + "the containing state and target state are the same."
              + "\nState: " + getName()
              + "\nTarget: " + target.getName()
              + keyLine;
            throw new IllegalArgumentException( error );
        }
    }

    private void validateNonTerminal( String role, String key, State target )
    {
        if( isTerminal() )
        {
            String line = "";
            String targetLine = "";
            if( null != key )
            {
                line = "\nKey: " + key;
            }
            if( null != target )
            {
                targetLine = "\nTarget: " + target.getName();
            }
            final String error = 
              "Cannot add " + role + " to a terminal state."
              + "\nState: " + getName()
              + targetLine
              + line;
            throw new IllegalStateException( error );
        }
    }

    private void validateIsaMember( String role, String key, State target )
    {
        String keyLine = "";
        if( null != key )
        {
            keyLine = "\nKey: " + key;
        }
        if( false == isaMemberOfGraph( target ) )
        {
            final String error = 
              "Cannot add " + role 
              + " because "
              + "the declared target state is not a member of the state graph."
              + "\nState: " + getName()
              + "\nTarget: " + target.getName()
              + keyLine ;
            throw new IllegalArgumentException( error );
        }
    }

    private void validateNonNullTarget( String role, String key, State target )
    {
        String keyLine = "";
        if( null != key )
        {
            keyLine = "\nKey: " + key;
        }
        if( null == target  )
        {
            final String error = 
              "Cannot add " + role + " due to undefined target."
              + "\nState: " + getName()
              + keyLine;
            throw new NullPointerException( error );
        }
    }

   /**
    * Utility operation to test if a supplied state instance is an 
    * instance from this state graph.
    *
    * @param state the instance to evalue
    * @return TRUE if the instance is a member else FALSE
    */
    private boolean isaMemberOfGraph( State state )
    {
        State[] states = getAllStates();
        for( int i=0; i<states.length; i++ )
        {
            State s = states[i];
            if( s == state ) return true;
        }
        return false;
    }

    public static final String TERMINATOR = "terminator";
    public static final String INITIALIZER = "initializer";
    public static final String TRANSITION = "transition";

}
