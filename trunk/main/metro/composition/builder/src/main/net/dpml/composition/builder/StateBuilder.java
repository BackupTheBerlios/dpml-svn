/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.builder;

import java.net.URI;

import net.dpml.configuration.Configuration;

import net.dpml.part.state.State;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Build a state module from an .xgraph resource.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class StateBuilder
{
    private final Task m_task;

    public StateBuilder( Task task )
    {
        m_task = task;
    }

    public State build( Configuration config )
    {
        final String name = config.getName();
        if( false == "graph".equals( name ) )
        {
            final String error = 
              "Root element in the state graph is not a 'graph' element.";
            throw new BuildException( error );
        }

        return buildStateGraph( config );
    }

    private State buildStateGraph( Configuration config )
    {
        boolean terminal = config.getAttributeAsBoolean( "terminal", false );
        State state = new State( terminal );
        if( false == terminal )
        {
            addSubStates( state, config );
            addTransitions( state, config );
            addInitialization( state, config );
            addOperations( state, config );
            addTermination( state, config );
        }
        return state;
    }

    private void addSubStates( State state, Configuration config )
    {
        Configuration[] configs = config.getChildren( "state" );
        for( int i=0; i<configs.length; i++ )
        {
            Configuration conf = configs[i];
            addState( state, conf );
        }
    }

    private void addState( State parent, Configuration config )
    {
        String name = config.getAttribute( "name", null );
        if( null == name )
        {
            final String error = 
              "State declaration inside [" + parent.getName() + "] does not declare a name.";
            throw new BuildException( error );
        }
        boolean terminal = config.getAttributeAsBoolean( "terminal", false );
        if( terminal )
        {
            log( "adding terminal state [" + name + "] to [" + parent.getName() + "]" );
            parent.addTerminalState( name );
        }
        else
        {
            log( "adding sub-state [" + name + "] to [" + parent.getName() + "]" );
            State state = parent.addState( name );
            addSubStates( state, config );
        }
    }

    private void addInitialization( State state, Configuration config )
    {
        Configuration child = config.getChild( "initialization", false );
        if( null != child )
        {
            String target = child.getAttribute( "target", null );
            Configuration method = child.getChild( "method", false );
            if( null == target )
            {
                if( null == method )
                {
                    final String error = 
                      "Initialization element inside state [" + state.getName() 
                      + "] does not declare a target attribute or alternative nested method.";
                    throw new BuildException( error );
                }
                else
                {
                    URI uri = getExecutionURI( state, "initialization", method );
                    state.setInitialization( uri );
                    log( "set initialization [" + uri + "] on [" + state.getName() + "]" );
                }
            }
            else
            {
                if( null == method )
                {
                    state.setInitialization( target );
                    log( "set initialization target [" + target + "] on [" + state.getName() + "]" );
                }
                else
                {
                    URI uri = getExecutionURI( state, "initialization", method );
                    state.setInitialization( uri, target );
                    log( "set initialization target [" + target + "] on [" 
                      + state.getName() 
                      + "] using ["
                      + uri 
                      + "]." );
                }
            }
        }
        Configuration[] states = config.getChildren( "state" );
        for( int i=0; i<states.length; i++ )
        {
            Configuration c = states[i];
            State s = state.getState( c.getAttribute( "name", null ) );
            addInitialization( s, c );
        }
    }

    private void addTermination( State state, Configuration config )
    {
        Configuration child = config.getChild( "termination", false );
        if( null != child )
        {
            String target = child.getAttribute( "target", null );
            Configuration method = child.getChild( "method", false );
            if( null == target )
            {
                if( null == method )
                {
                    final String error = 
                      "Termination element inside state [" + state.getName() 
                      + "] does not declare a target attribute or alternative nested method.";
                    throw new BuildException( error );
                }
                else
                {
                    URI uri = getExecutionURI( state, "termination", method );
                    state.setTerminator( uri, state );
                    log( "set termination [" + uri + "] on [" + state.getName() + "]" );
                }
            }
            else
            {
                if( null == method )
                {
                    state.setTerminator( target );
                    log( "set termination target [" + target + "] on [" + state.getName() + "]" );
                }
                else
                {
                    URI uri = getExecutionURI( state, "termination", method );
                    state.setTerminator( uri, target );
                    log( "set termination target [" + target + "] on [" 
                      + state.getName() 
                      + "] using ["
                      + uri 
                      + "]." );
                }
            }
        }
        Configuration[] states = config.getChildren( "state" );
        for( int i=0; i<states.length; i++ )
        {
            Configuration c = states[i];
            State s = state.getState( c.getAttribute( "name", null ) );
            addTermination( s, c );
        }
    }

    private URI getExecutionURI( State state, String role, Configuration config )
    {
        String element = config.getName();
        if( "method".equals( element ) )
        {
            String name = config.getAttribute( "name", null );
            if( null == name )
            {
                final String error = 
                  "A method declaration within the [" 
                  + role 
                  + "] element under the state [" 
                  + state.getName()
                  + "] does not declare a name.";
                throw new BuildException( error );
            }
            return createURI( "method:" + name );
        }
        else
        {
            final String error = 
              "Don't know how to construct execution semantics using an element named [" 
              + element 
              + "] declared within the an [" 
              + role 
              + "] element under the state ["
              + state.getName()
              + "].";
            throw new BuildException( error );
        }
    }

    private void addTransitions( State state, Configuration config )
    {
        Configuration[] configs = config.getChildren( "transition" );
        for( int i=0; i<configs.length; i++ )
        {
            Configuration conf = configs[i];
            addTransition( state, conf );
        }
        Configuration[] states = config.getChildren( "state" );
        for( int i=0; i<states.length; i++ )
        {
            Configuration c = states[i];
            State s = state.getState( c.getAttribute( "name", null ) );
            addTransitions( s, c );
        }
    }

    private void addTransition( State state, Configuration config )
    {
        String name = config.getAttribute( "name", null );
        if( null == name )
        {
            final String error = 
              "A transition declaration inside [" 
              + state.getName() 
              + "] does not declare a name.";
            throw new BuildException( error );
        }
        String target = config.getAttribute( "target", null );
        if( null == target )
        {
            final String error = 
              "A transition declaration inside [" + state.getName() + "] does not declare a target.";
            throw new BuildException( error );
        }
        Configuration child = config.getChild( "method", false );
        if( null == child )
        {
            state.addTransition( name, target );
            log( "transition [" + name + "] added to [" + state.getName() + "] with target [" + target + "]" );
        }
        else
        {
            URI uri = getExecutionURI( state, "transition", child );
            state.addTransition( name, uri, target );
            log( 
              "transition [" + name + "] added to [" + state.getName() 
              + "] with target [" + target + "] and urn [" + uri + "]" );
        }
    }

    private void addOperations( State state, Configuration config )
    {
        Configuration[] configs = config.getChildren( "operation" );
        for( int i=0; i<configs.length; i++ )
        {
            Configuration conf = configs[i];
            addOperation( state, conf );
        }
        Configuration[] states = config.getChildren( "state" );
        for( int i=0; i<states.length; i++ )
        {
            Configuration c = states[i];
            State s = state.getState( c.getAttribute( "name", null ) );
            addOperations( s, c );
        }
    }

    private void addOperation( State state, Configuration config )
    {
        String name = config.getAttribute( "name", null );
        if( null == name )
        {
            final String error = 
              "An operation declaration inside [" 
              + state.getName() 
              + "] does not declare a name.";
            throw new BuildException( error );
        }
        Configuration child = config.getChild( "method", false );
        if( null == child )
        {
            final String error = 
              "An operation element in the state [" + state.getName() 
              + "] does not include a nested method.";
            throw new BuildException( error );
        }
        else
        {
            URI uri = getExecutionURI( state, "operation", child );
            state.addOperation( name, uri );
            log( 
              "operation [" + name + "] added to [" + state.getName() 
              + "] with urn [" + uri + "]" );
        }
    }

    private URI createURI( String path )
    {
        try
        {
            return new URI( path );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private void log( String message )
    {
        m_task.log( message );
    }
}

