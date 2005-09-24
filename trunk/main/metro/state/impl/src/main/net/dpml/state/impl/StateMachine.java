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

import java.io.Serializable;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;
import net.dpml.configuration.impl.DefaultConfigurationBuilder;
import net.dpml.configuration.impl.ConfigurationUtil;

import net.dpml.state.State;
import net.dpml.state.Transition;
import net.dpml.state.Operation;
import net.dpml.state.StateBuilderRuntimeException;
import net.dpml.state.Trigger;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.Action;
import net.dpml.state.Delegation;

import net.dpml.transit.model.DuplicateKeyException;

/**
 * Default state graph model builder.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class StateMachine
{
    private static DefaultConfigurationBuilder BUILDER = new DefaultConfigurationBuilder();
    
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
    
    public static void validate( State state )
    {
        validateState( state );
    }

    //-------------------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------------------

    private State m_state;

    //-------------------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------------------

    public StateMachine( State state )
    {
        m_state = state;
    }

    //-------------------------------------------------------------------------------
    // StateMachine
    //-------------------------------------------------------------------------------

   /**
    * Return the current state.
    * @return the current state
    */
    public State getState()
    {
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
        return getAction( m_state, Trigger.INITIALIZATION );
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
        return getAction( m_state, Trigger.TERMINATION );
    }
    
    public State initialize( Object object )
    {
        ArrayList visited = new ArrayList();
        return initialize( visited, object );
    }
    
    public void execute( Operation operation, Object object )
    {
        URI handler = operation.getHandlerURI();
        if( null != handler )
        {
            execute( handler, object );
        }
    }
    
   /**
    * Apply a named transition to the target object.
    * @param name the transition name
    * @param object the object against which any transition handler action are to be applied
    */
    public State apply( String name, Object object )
    {
        synchronized( m_state )
        {
            Transition transition = getTransition( m_state, name );
            if( null == transition )
            {
                throw new IllegalArgumentException( "Unknown transition name: " + name );
            }
            return apply( transition, object );
        }
    }
    
    public Transition[] getTransitions()
    {
        Hashtable table = new Hashtable();
        State[] states = m_state.getStatePath();
        for( int i=(states.length-1); i>-1; i-- )
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
    
    public Operation[] getOperations()
    {
        Hashtable table = new Hashtable();
        State[] states = m_state.getStatePath();
        for( int i=(states.length-1); i>-1; i-- )
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
    
    public State terminate( Object object )
    {
        ArrayList visited = new ArrayList();
        return terminate( visited, object );
    }
    
    //-------------------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------------------
    
    private State initialize( List list, Object object )
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
            if( action instanceof Operation )
            {
                Operation operation = (Operation) action;
                execute( operation, object );
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
    
    private State terminate( List list, Object object )
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
                execute( operation, object );
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
    
    private State apply( Transition transition, Object object )
    {
        synchronized( m_state )
        {
            State context = transition.getState();
            String target = transition.getTargetName();
            State state = getState( context, target );
            URI handler = transition.getHandlerURI();
            if( null != handler )
            {
                execute( handler, object );
            }
            setState( state );
            return state;
        }
    }
    
    private void execute( URI handler, Object object )
    {
        // TODO: handle the declared handler
    }
    
    private void setState( State state )
    {
        synchronized( m_state )
        {
            m_state = state;
            // TODO: add stat change notification
        }
    }
    
    private Action getAction( State state, TriggerEvent category )
    {
        Trigger[] triggers = state.getTriggers();
        for( int i=0; i<triggers.length; i++ )
        {
            Trigger trigger = triggers[i];
            if( trigger.getEvent().equals( category ) )
            {
                Action action = trigger.getAction();
                if( action instanceof Delegation )
                {
                    URI uri = ((Delegation)action).getURI();
                    String scheme = uri.getScheme();
                    String path = uri.getSchemeSpecificPart();
                    if( "transition".equals( scheme ) )
                    {
                        return getTransition( state, path );
                    }
                    else if( "operation".equals( scheme ) )
                    {
                        return getOperation( state, path );
                    }
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
    
    private Transition getTransition( State state, String name )
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
            return null;
        }
        else
        {
            return getTransition( parent, name );
        }
    }

    private Operation getOperation( State state, String name )
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
            return null;
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
        else if( action instanceof Delegation )
        {
            Delegation delegation = (Delegation) action;
            validateDelegation( state, delegation );
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

    private static void validateDelegation( State state, Delegation delegation )
    {
        //System.out.println( "# v/action: " + delegation );
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
        else if( name.equals( "action" ) )
        {
            String id = config.getAttribute( "uri" );
            URI uri = new URI( id );
            return new DefaultDelegation( uri );
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
        String name = config.getName();
        if( name.equals( "operation" ) )
        {
            String operationName = config.getAttribute( "name" );
            String handler = config.getAttribute( "target", null );
            URI uri = createURI( handler );
            return new DefaultOperation( operationName, uri );
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
            String handler = config.getAttribute( "handler", null );
            String target = config.getAttribute( "target", "." );
            URI uri = createURI( handler );
            String transitionName = config.getAttribute( "name" );
            return new DefaultTransition( transitionName, target, uri );
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
