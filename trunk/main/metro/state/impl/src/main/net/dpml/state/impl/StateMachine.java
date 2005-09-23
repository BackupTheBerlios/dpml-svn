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

import net.dpml.transit.model.DuplicateKeyException;

/**
 * Default state graph model builder.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class StateMachine
{
    private static DefaultConfigurationBuilder BUILDER = new DefaultConfigurationBuilder();
    
    public State loadGraph( InputStream input ) throws StateBuilderRuntimeException
    {
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
    
    private DefaultState buildState( Configuration config, boolean root ) throws Exception
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
        //Initialization initialization = null;
        //Termination termination = null;
        ArrayList transitionList = new ArrayList();
        ArrayList operationList = new ArrayList();
        ArrayList stateList = new ArrayList();
        ArrayList triggerList = new ArrayList();
        Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String name = child.getName();
            //if( name.equals( "initialization" ) )
            //{
            //    initialization = buildInitialization( child );
            //}
            //else if( name.equals( "termination" ) )
            //{
            //    termination = buildTermination( child );
            //}
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
        Transition[] transitions = (Transition[]) transitionList.toArray( new Transition[0] );
        Operation[] operations = (Operation[]) operationList.toArray( new Operation[0] );
        DefaultState[] states = (DefaultState[]) stateList.toArray( new DefaultState[0] );
        DefaultTrigger[] triggers = (DefaultTrigger[]) triggerList.toArray( new DefaultTrigger[0] );
        return new DefaultState( stateName, triggers, transitions, operations, states, terminal );
    }
    
    private Trigger buildTrigger( Configuration config ) throws Exception
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

    private Action buildAction( Configuration config ) throws Exception
    {
        String name = config.getName();
        if( name.equals( "transition" ) )
        {
            return buildTransition( config );
        }
        if( name.equals( "operation" ) )
        {
            return buildOperation( config );
        }
        else if( name.equals( "action" ) )
        {
            String id = config.getAttribute( "id" );
            return new DefaultDelegation( id );
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

    private Operation buildOperation( Configuration config ) throws Exception
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
    

    private Transition buildTransition( Configuration config ) throws Exception
    {
        String name = config.getName();
        String handler = config.getAttribute( "handler", null );
        String target = config.getAttribute( "target", "." );
        URI uri = createURI( handler );
        
        if( name.equals( "transition" ) )
        {
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
    
    private URI createURI( String path ) throws Exception
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
