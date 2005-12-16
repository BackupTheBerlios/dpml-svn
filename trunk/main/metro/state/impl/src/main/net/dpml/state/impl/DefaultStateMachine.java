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

package net.dpml.state.impl;

import java.beans.Expression;
import java.beans.Statement;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.DefaultConfigurationBuilder;
import net.dpml.configuration.impl.ConfigurationUtil;

import net.dpml.state.State;
import net.dpml.state.Transition;
import net.dpml.state.Operation;
import net.dpml.state.StateBuilderRuntimeException;
import net.dpml.state.Trigger;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.Action;
import net.dpml.state.StateMachine;
import net.dpml.state.UnknownOperationException;
import net.dpml.state.UnknownTransitionException;
import net.dpml.state.IntegrityRuntimeException;

/**
 * Default state-machine implementation.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultStateMachine implements StateMachine
{
   /**
    * Constant name used to reference a state change in a property event.
    */
    public static final String PROPERTY_NAME = "state";
    
    private static final DefaultConfigurationBuilder BUILDER = new DefaultConfigurationBuilder();
    
   /**
    * Load a state descriptor from an input stream.
    * @param input the input stream
    * @return the state graph
    * @exception StateBuilderRuntimeException if an error occurs during loading
    */
    public static State load( InputStream input ) throws StateBuilderRuntimeException
    {
        State graph = null;
        try
        {
            Configuration config = BUILDER.build( input );
            return buildState( config, true );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to build state graph.";
            throw new StateBuilderRuntimeException( error, e );
        }
    }
   
   /**
    * Validate the state integrity.
    * @param state the state to validate
    */
    public static void validate( State state )
    {
        validateState( state );
    }

    //-------------------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------------------

    private final PropertyChangeSupport m_support;
    
    private State m_state;
    private boolean m_active = false;
    private boolean m_disposed = false;

    //-------------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------------

   /**
    * Creation of a new state machine using a supplied input stream of 
    * the source for the state graph.
    * @param input the input stream to a state graph
    */
    public DefaultStateMachine( InputStream input )
    {
        this( load( input ) );
    }
    
   /**
    * Creation of a new state machine using a state graph.
    * @param state the state graph
    */
    public DefaultStateMachine( State state )
    {
        m_state = state;
        m_support = new PropertyChangeSupport( this );
    }

    //-------------------------------------------------------------------------------
    // StateMachine
    //-------------------------------------------------------------------------------

   /**
    * Add a property change listener to the state machine.
    * @param listener the property change listener
    */
    public void addPropertyChangeListener( final PropertyChangeListener listener )
    {
        m_support.addPropertyChangeListener( listener );
    }
    
   /**
    * Remove a property change listener from the state machine.
    * @param listener the property change listener
    */
    public void removePropertyChangeListener( final PropertyChangeListener listener )
    {
        m_support.removePropertyChangeListener( listener );
    }

   /**
    * Return the current state.
    * @return the current state
    */
    public State getState()
    {
        checkDisposed();
        synchronized( m_state )
        {
            return m_state;
        }
    }
    
   /**
    * Locate and return the most immediate initialization action defined relative to 
    * the current state.  If an action is located within the current state it will be 
    * return otherwise the search will continue up the state graph until either an 
    * action is located or no further parent state is available in which case a null 
    * value is returned.
    * 
    * @return the initialization action or null if no action declared
    */
    public Action getInitializationAction()
    {
        checkDisposed();
        try
        {
            return getAction( m_state, Trigger.INITIALIZATION );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error during resolution of initialization actions.";
            throw new IntegrityRuntimeException( error, e );
        }
    }
    
   /**
    * Locate and return the most immediate termination action defined relative to 
    * the current state.  If an action is located within the current state it will be 
    * return otherwise the search will continue up the state graph until either an 
    * action is located or no further parent state is available in which case a null 
    * value is returned.
    * 
    * @return the termination action or null if no action declared
    */
    public Action getTerminationAction()
    {
        checkDisposed();
        try
        {
            return getAction( m_state, Trigger.TERMINATION );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error during resolution of termination actions.";
            throw new IntegrityRuntimeException( error, e );
        }
    }
    
   /**
    * Invoke initialization of the supplied object using the initialization action
    * declared under the current state path.
    * 
    * @param object the object to initialize
    * @return the state established as a sidee effect of the initialization
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of initialization
    */
    public State initialize( Object object ) throws InvocationTargetException
    {
        checkDisposed();
        ArrayList visited = new ArrayList();
        try
        {
            State result = initialize( visited, object );
            m_active = true;
            return result;
        }
        catch( UnknownTransitionException e )
        {
            final String error = 
              "Internal state machine error raised due to an unresolved transition.";
            throw new IntegrityRuntimeException( error, e );
        }
        catch( UnknownOperationException e )
        {
            final String error = 
              "Internal state machine error raised due to an unresolved operation.";
            throw new IntegrityRuntimeException( error, e );
        }
        catch( InvocationTargetException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error during state-machine initialization.";
            throw new IntegrityRuntimeException( error, e );
        }
    }
    
   /**
    * Execute a named operation on the supplied object.
    * @param name an operation name
    * @param object the target object
    * @param args operation arguments
    * @return the result of operation invocation
    * @exception UnknownOperationException if the operation is unknown
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of operation execution
    */
    public Object execute( String name, Object object, Object[] args ) 
      throws UnknownOperationException, InvocationTargetException
    {
        checkDisposed();
        Operation operation = getOperation( getState(), name );
        return execute( operation, object, args );
    }
    
   /**
    * Apply a named transition to the target object.
    * @param name the transition name
    * @param object the object against which any transition handler action are to be applied
    * @return the state established by the application of the transition
    * @exception UnknownTransitionException if the transition is unknown
    * @exception InvocationTargetException if an invocation error occurs as a 
    *   result of transition invocation
    */
    public State apply( String name, Object object ) 
      throws UnknownTransitionException, InvocationTargetException
    {
        checkDisposed();
        synchronized( m_state )
        {
            Transition transition = getTransition( m_state, name );
            return apply( transition, object );
        }
    }
    
   /**
    * Return all of the available transitions relative to the current state.
    * @return the available transitions
    */
    public Transition[] getTransitions()
    {
        checkDisposed();
        Hashtable table = new Hashtable();
        State[] states = m_state.getStatePath();
        for( int i=( states.length-1 ); i>-1; i-- )
        {
            State state = states[i];
            Transition[] transitions = state.getTransitions();
            for( int j=0; j<transitions.length; j++ )
            {
                Transition transition = transitions[j];
                String name = transition.getName();
                if( null == table.get( name ) )
                {
                    table.put( name, transition );
                }
            }
        }
        return (Transition[]) table.values().toArray( new Transition[0] );
    }
    
   /**
    * Return all of the available operations relative to the current state.
    * @return the available operations
    */
    public Operation[] getOperations() 
    {
        checkDisposed();
        Hashtable table = new Hashtable();
        State[] states = m_state.getStatePath();
        for( int i=( states.length-1 ); i>-1; i-- )
        {
            State state = states[i];
            Operation[] operations = state.getOperations();
            for( int j=0; j<operations.length; j++ )
            {
                Operation operation = operations[j];
                String name = operation.getName();
                if( null == table.get( name ) )
                {
                    table.put( name, operation );
                }
            }
        }
        return (Operation[]) table.values().toArray( new Operation[0] );
    }
    
   /**
    * Invoke termination of the supplied object using the termination action
    * declared under the current state path.
    * 
    * @param object the object to terminate
    * @return the state established as a side-effect of the termination
    */
    public State terminate( Object object )
    {
        checkDisposed();
        ArrayList visited = new ArrayList();
        try
        {
            State result = terminate( visited, object );
            m_active = false;
            return result;
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return getState();
        }
    }
    
   /**
    * Returns the active status of the state machine.
    * @return TRUE if the state machine has invoked initialization and 
    * termination has not been performed otherwise FALSE
    */
    public boolean isActive()
    {
        return m_active;
    }
    
   /**
    * Dispose of the state machine.
    */
    public void dispose()
    {
        m_disposed = true;
        m_state = null;
        PropertyChangeListener[] listeners = m_support.getPropertyChangeListeners();
        for( int i=0; i<listeners.length; i++ )
        {
            PropertyChangeListener listener = listeners[i];
            m_support.removePropertyChangeListener( listener );
        }
    }
    
    //-------------------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------------------
    
    private State initialize( List list, Object object ) throws Exception
    {
        Action action = getInitializationAction();
        if( null == action )
        {
            return m_state;
        }
        else if( list.contains( action ) )
        {
            return m_state;
        }
        else
        {
            list.add( action );
            if( action instanceof Operation )
            {
                Operation operation = (Operation) action;
                execute( operation, object, new Object[0] );
                return m_state;
            }
            else if( action instanceof Transition )
            {
                Transition transition = (Transition) action;
                State current = getState();
                State result = apply( transition, object );
                if( !current.equals( result ) )
                {
                    return initialize( list, object );
                }
                else
                {
                    return getState();
                }
            }
            else
            {
                final String error = "Unrecognized action: " + action;
                throw new IllegalStateException( error );
            }
        }
    }
    
    private Object execute( Operation operation, Object object, Object[] args ) throws InvocationTargetException
    {
        if( null == object )
        {
            return null;
        }
        
        String method = getMethodName( operation );
        
        try
        {
            Expression expression = new Expression( object, method, args );
            return expression.getValue();
        }
        catch( InvocationTargetException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new InvocationTargetException( e );
        }
    }
    private State terminate( List list, Object object ) throws Exception
    {
        Action action = getTerminationAction();
        if( null == action )
        {
            return m_state;
        }
        else if( list.contains( action ) )
        {
            return m_state;
        }
        else
        {
            if( action instanceof Operation )
            {
                Operation operation = (Operation) action;
                execute( operation, object, new Object[0] );
                return m_state;
            }
            else if( action instanceof Transition )
            {
                Transition transition = (Transition) action;
                State current = getState();
                State result = apply( transition, object );
                if( !current.equals( result ) )
                {
                    return terminate( list, object );
                }
                else
                {
                    return getState();
                }
            }
            else
            {
                final String error = "Unrecognized action: " + action;
                throw new IllegalStateException( error );
            }
        }
    }
    
    private State apply( Transition transition, Object object ) throws InvocationTargetException
    {
        synchronized( m_state )
        {
            State context = transition.getState();
            String target = transition.getTargetName();
            State state = getState( context, target );
            Operation operation = transition.getOperation();
            if( null != operation )
            {
                execute( operation, object, new Object[0] ); // TODO: add resolved values as args
            }
            setState( state );
            return state;
        }
    }
    
    private void execute( URI handler, Object object ) throws InvocationTargetException
    {
        if( null == object )
        {
            return;
        }
        String scheme = handler.getScheme();
        if( "method".equals( scheme ) )
        {
            String methodName = handler.getSchemeSpecificPart();
            Statement statement = new Statement( object, methodName, new Object[0] );
            try
            {
                statement.execute();
            }
            catch( InvocationTargetException e )
            {
                throw e;
            }
            catch( Exception e )
            {
                throw new InvocationTargetException( e );
            }
        }
        else
        {
            final String error = 
              "Operation scheme not recognized."
              + "\nScheme: " + scheme
              + "\nURI: " + handler;
            throw new IllegalArgumentException( error );
        }
    }
    
    private void setState( final State state )
    {
        synchronized( m_state )
        {
            if( m_state != state )
            {
                final State oldState = m_state;
                m_state = state;
                final PropertyChangeEvent event = 
                  new PropertyChangeEvent( 
                    this, PROPERTY_NAME, oldState, state );
                m_support.firePropertyChange( event );
            }
        }
    }
    
    private Action getAction( State state, TriggerEvent category )
      throws UnknownTransitionException, UnknownOperationException
    {
        Trigger[] triggers = state.getTriggers();
        for( int i=0; i<triggers.length; i++ )
        {
            Trigger trigger = triggers[i];
            if( trigger.getEvent().equals( category ) )
            {
                Action action = trigger.getAction();
                if( action instanceof ApplyAction )
                {
                    ApplyAction apply = (ApplyAction) action;
                    String id = apply.getID();
                    return getTransition( state, id );
                }
                else if( action instanceof ExecAction )
                {
                    ExecAction exec = (ExecAction) action;
                    String id = exec.getID();
                    return getOperation( state, id );
                }
                else
                {
                    return action;
                }
            }
        }
        State parent = state.getParent();
        if( null != parent )
        {
            return getAction( parent, category );
        }
        else
        {
            return null;
        }
    }
    
    private Transition getTransition( State state, String name ) throws UnknownTransitionException
    {
        Transition[] transitions = state.getTransitions();
        for( int i=0; i<transitions.length; i++ )
        {
            Transition transition = transitions[i];
            if( name.equals( transition.getName() ) )
            {
                return transition;
            }
        }
        State parent = state.getParent();
        if( null == parent )
        {
            final String error = 
              "Unable to resolve a transition named [" 
              + name
              + "] relative to the state [" 
              + state.getName()
              + "].";
            throw new UnknownTransitionException( error );
        }
        else
        {
            return getTransition( parent, name );
        }
    }

    private Operation getOperation( State state, String name ) throws UnknownOperationException
    {
        Operation[] operations = state.getOperations();
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            if( name.equals( operation.getName() ) )
            {
                return operation;
            }
        }
        State parent = state.getParent();
        if( null == parent )
        {
            throw new UnknownOperationException( name );
        }
        else
        {
            return getOperation( parent, name );
        }
    }

    //-------------------------------------------------------------------------------
    // static internals used to validate the integrity of a state graph
    //-------------------------------------------------------------------------------
    
    private static void validateState( State state )
    {
        Trigger[] triggers = state.getTriggers();
        validateTriggers( state, triggers );
        Transition[] transitions = state.getTransitions();
        validateTransitions( state, transitions );
        Operation[] operations = state.getOperations();
        validateOperations( state, operations );
        State[] states = state.getStates();
        validateStates( state, states );
    }

    private static void validateTransitions( State state, Transition[] transitions )
    {
        for( int i=0; i<transitions.length; i++ )
        {
            Transition transition = transitions[i];
            validateTransition( state, transition );
        }
    }

    private static void validateOperations( State state, Operation[] operations )
    {
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            validateOperation( state, operation );
        }
    }

    private static void validateTriggers( State state, Trigger[] triggers )
    {
        for( int i=0; i<triggers.length; i++ )
        {
            Trigger trigger = triggers[i];
            validateTrigger( state, trigger );
        }
    }
    
    private static void validateStates( State state, State[] states )
    {
        for( int i=0; i<states.length; i++ )
        {
            State s = states[i];
            validateState( s );
        }
    }
    
    private static void validateTrigger( State state, Trigger trigger )
    {
        TriggerEvent event = trigger.getEvent();
        Action action = trigger.getAction();
        if( action instanceof Transition )
        {
            Transition transition = (Transition) action;
            validateTransition( state, transition );
        }
        else if( action instanceof Operation )
        {
            Operation operation = (Operation) action;
            validateOperation( state, operation );
        }
        else if( action instanceof ApplyAction )
        {
            // TODO
        }
        else if( action instanceof ExecAction )
        {
            // TODO
        }
    }
    
    private static void validateTransition( State state, Transition transition )
    {
        String target = transition.getTargetName();
        State s = getState( state, target );
        if( null == state )
        {
            final String error = 
              "Transition target [" 
              + target 
              + "] does not exist relative to state ["
              + state;
            throw new IllegalStateException( error );
        }
    }
    
    private static void validateOperation( State state, Operation operation )
    {
        //System.out.println( "# v/operation: " + operation );
    }

    private static State getState( State state, String target )
    {
        if( target.startsWith( "/" ) )
        {
            //
            // its an absolute target 
            //
            
            String spec = target.substring( 1 );
            State root = state.getStatePath()[0];
            return getState( root, spec );
        }
        else if( target.startsWith( "../" ) )
        {
            //
            // its relative to the state's parent
            //
            
            String spec = target.substring( 3 );
            State parent = state.getParent();
            if( null != parent )
            {
                return getState( parent, spec );
            }
            else
            {
                final String error = 
                "Invalid relative reference [" 
                + spec
                + "] passed to root state.";
                throw new IllegalArgumentException( error );
            }
        }
        else if( target.indexOf( "/" ) > -1 )
        {
            //
            // its a composition address
            //
            
            int index = target.indexOf( "/" );
            String base = target.substring( 0, index );
            String remainder = target.substring( index + 1 );
            State s = getState( state, base );
            return getState( s, remainder );
        }
        else
        {
            //
            // its a name relative to the supplied state
            //
            
            State[] states = state.getStates();
            for( int i=0; i<states.length; i++ )
            {
                State s = states[i];
                if( target.equals( s.getName() ) )
                {
                    return s;
                }
            }
            final String error = 
              "Requested target state [" 
              + target
              + "] does not exist within the state ["
              + state.getName()
              + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    private void checkDisposed() throws IllegalStateException
    {
        if( m_disposed )
        {
            final String error = 
              "Instance has been disposed.";
            throw new IllegalStateException( error );
        }
    }

    private String getMethodName( Operation operation )
    {
        if( null != operation.getMethodName() )
        {
            return operation.getMethodName();
        }
        
        //
        // otherwise resolve it using java beans style getXxxx
        //
        
        String name = operation.getName();
        int n = name.length();
        if( n == 0 )
        {
            throw new IllegalArgumentException( "Operation name is empty." );
        }
        else if( n == 1 )
        {
            return "get" + name.toUpperCase();
        }
        else
        {
            return "get" 
              + name.substring( 0, 1 ).toUpperCase()
              + name.substring( 1 );
        }
    }
    
    //-------------------------------------------------------------------------------
    // static internals used top build a state graph from a supplied configuration
    //-------------------------------------------------------------------------------

    private static DefaultState buildState( Configuration config, boolean root ) throws Exception
    {
        String stateName = null;
        boolean terminal = false;
        String[] names = config.getAttributeNames();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            if( name.equals( "name" ) )
            {
                if( root )
                {
                    final String error = 
                      "Root state elements is declaring a name.";
                    throw new StateBuilderRuntimeException( error );
                }
                else
                {
                    stateName = config.getAttribute( "name" );
                }
            }
            else if( name.equals( "terminal" ) )
            {
                terminal = config.getAttributeAsBoolean( "terminal", false );
            }
            else
            {
                final String error = 
                  "State element attribute name ["
                  + name
                  + "] is not recognized.";
                throw new StateBuilderRuntimeException( error );
            }
        }
        
        if( null == stateName )
        {
            if( !root )
            {
                final String error = 
                  "State element doesn not delcare a 'name'."
                  + "\n"
                  + ConfigurationUtil.list( config );
                throw new StateBuilderRuntimeException( error );
            }
            else
            {
                stateName = "";
            }
        }
        ArrayList transitionList = new ArrayList();
        ArrayList operationList = new ArrayList();
        ArrayList stateList = new ArrayList();
        ArrayList triggerList = new ArrayList();
        Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String name = child.getName();
            if( name.equals( "transition" ) )
            {
                Transition transition = buildTransition( child );
                transitionList.add( transition );
            }
            else if( name.equals( "operation" ) )
            {
                Operation operation = buildOperation( child );
                operationList.add( operation );
            }
            else if( name.equals( "state" ) )
            {
                DefaultState state = buildState( child, false );
                stateList.add( state );
            }
            else if( name.equals( "trigger" ) )
            {
                Trigger trigger = buildTrigger( child );
                triggerList.add( trigger );
            }
            else
            {
                final String error = 
                  "A subsidiary element named ["
                  + name
                  + "] with the state element ["
                  + stateName
                  + "] is not recognized."
                  + ConfigurationUtil.list( child );
                throw new StateBuilderRuntimeException( error );
            }
        }
        DefaultTransition[] transitions = (DefaultTransition[]) transitionList.toArray( new DefaultTransition[0] );
        DefaultOperation[] operations = (DefaultOperation[]) operationList.toArray( new DefaultOperation[0] );
        DefaultState[] states = (DefaultState[]) stateList.toArray( new DefaultState[0] );
        DefaultTrigger[] triggers = (DefaultTrigger[]) triggerList.toArray( new DefaultTrigger[0] );
        return new DefaultState( stateName, triggers, transitions, operations, states, terminal );
    }
    
    private static Trigger buildTrigger( Configuration config ) throws Exception
    {
        String name = config.getName();
        if( name.equals( "trigger" ) )
        {
            String eventName = config.getAttribute( "event", Trigger.INITIALIZATION.getName() );
            TriggerEvent event = TriggerEvent.parse( eventName );
            if( config.getChildren().length == 1 )
            {
                Configuration c = config.getChildren()[0];
                Action action = buildAction( c );
                return new DefaultTrigger( event, action );
            }
            else
            {
                final String error = 
                  "Trigger element does not contain a nested action element.";
                throw new StateBuilderRuntimeException( error );
            }
        }
        else
        {
            final String error = 
              "Illegal element name ["
              + name
              + "] supplied to trigger builder.";
            throw new StateBuilderRuntimeException( error );
        }
    }

    private static Action buildAction( Configuration config ) throws Exception
    {
        String name = config.getName();
        if( name.equals( "transition" ) )
        {
            return buildTransition( config );
        }
        else if( name.equals( "operation" ) )
        {
            return buildOperation( config );
        }
        else if( name.equals( "apply" ) )
        {
            String id = config.getAttribute( "id" );
            return new ApplyAction( id );
        }
        else if( name.equals( "exec" ) )
        {
            String id = config.getAttribute( "id" );
            return new ExecAction( id );
        }
        else
        {
            final String error = 
              "Illegal element name ["
              + name
              + "] supplied to action builder.";
            throw new StateBuilderRuntimeException( error );
        }
    }

    private static DefaultOperation buildOperation( Configuration config ) throws Exception
    {
        if( null == config )
        {
            return null;
        }
        String name = config.getName();
        if( name.equals( "operation" ) )
        {
            String operationName = config.getAttribute( "name" );
            String methodName = config.getAttribute( "method", null );
            return new DefaultOperation( operationName, methodName );
        }
        else
        {
            final String error = 
              "Illegal element name ["
              + name
              + "] supplied to operation builder.";
            throw new StateBuilderRuntimeException( error );
        }
    }
    
    private static DefaultTransition buildTransition( Configuration config ) throws Exception
    {
        String name = config.getName();
        
        if( name.equals( "transition" ) )
        {
            String transitionName = config.getAttribute( "name" );
            String target = config.getAttribute( "target", "." );
            DefaultOperation operation = buildOperation( config.getChild( "operation", false ) );
            return new DefaultTransition( transitionName, target, operation );
        }
        else
        {
            final String error = 
              "Illegal element name ["
              + name
              + "] supplied to transition builder.";
            throw new StateBuilderRuntimeException( error );
        }
    }
    
    private static URI createURI( String path ) throws Exception
    {
        if( null == path )
        {
            return null;
        }
        else
        {
            return new URI( path );
        }
    }
}
