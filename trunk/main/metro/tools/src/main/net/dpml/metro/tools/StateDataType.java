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

package net.dpml.metro.tools;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.dpml.library.info.Scope;

import net.dpml.tools.tasks.GenericTask;

import net.dpml.state.Trigger;
import net.dpml.state.State;
import net.dpml.state.Operation;
import net.dpml.state.Transition;
import net.dpml.state.StateBuilder;
import net.dpml.state.impl.DefaultState;
import net.dpml.state.impl.DefaultStateMachine;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.AntClassLoader;

/**
 * Utility datatype supporting State instance construction.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StateDataType
{
    private final boolean m_root;
    private final GenericTask m_task;
    
    private String m_name;
    private List m_states = new ArrayList();
    private List m_operations = new ArrayList();
    private List m_transitions = new ArrayList();
    private List m_triggers = new ArrayList();
    private boolean m_terminal = false;
    
    private URI m_uri;
    private String m_classname;
    
    StateDataType( GenericTask task )
    {
        this( task, false );
    }
    
    StateDataType( GenericTask task, boolean root )
    {
        m_root = root;
        m_task = task;
    }
    
   /**
    * Set the state name.  Note that state names are only applicable to
    * substates.  A state name assigned to the root state will be ignored.
    * @param name the name of the state
    */
    public void setName( final String name )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
    }
    
   /**
    * Set a uri from which to resolve an encoded state graph.  May only 
    * applied to a root state.
    */
    public void setUri( final URI uri )
    {
        if( !m_root )
        {
            final String error = 
              "Illegal attempt to request import of a state graph within a nested state.";
            throw new BuildException( error, m_task.getLocation() );
        }
        m_uri = uri;
    }
    
   /**
    * Set a classname from which to resolve an embedded state graph.  May only 
    * applied to a root state.  May not be used in conjuction with other attributes or
    * nested elements.
    */
    public void setClass( final String classname )
    {
        if( !m_root )
        {
            final String error = 
              "Illegal attempt to request import of a state graph within a nested state.";
            throw new BuildException( error, m_task.getLocation() );
        }
        if( null != m_uri )
        {
            final String error = 
              "Illegal attempt to request import of a embedded state graph in conjuction with the uri attribute.";
            throw new BuildException( error, m_task.getLocation() );
        }
        m_classname = classname;
    }
    
   /**
    * Mark the state as a terminal state.
    * @param flag true if this is a terminal state
    */
    public void setTerminal( final boolean flag )
    {
        if( null != m_uri )
        {
            final String error = 
              "Terminal attribute may not be used in conjuction with a uri import.";
            throw new BuildException( error, m_task.getLocation() );
        }
        if( null != m_classname )
        {
            final String error = 
              "Terminal attribute may not be used in conjuction with the class attribute.";
            throw new BuildException( error, m_task.getLocation() );
        }
        m_terminal = flag;
    }
    
   /**
    * Add a substate within the state.
    * @return the sub-state datatype
    */
    public StateDataType createState()
    {
        if( null != m_uri )
        {
            final String error = 
              "Substates may not be used in conjuction with a uri import.";
            throw new BuildException( error, m_task.getLocation() );
        }
        if( null != m_classname )
        {
            final String error = 
              "Substates may not be used in conjuction with the class attribute.";
            throw new BuildException( error, m_task.getLocation() );
        }
        final StateDataType state = new StateDataType( m_task );
        m_states.add( state );
        return state;
    }
    
   /**
    * Add an operation within this state.
    * @return the operation datatype
    */
    public OperationDataType createOperation()
    {
        if( null != m_uri )
        {
            final String error = 
              "Operations may not be used in conjuction with a uri import.";
            throw new BuildException( error, m_task.getLocation() );
        }
        if( null != m_classname )
        {
            final String error = 
              "Operations may not be used in conjuction with the class attribute.";
            throw new BuildException( error, m_task.getLocation() );
        }
        final OperationDataType operation = new OperationDataType();
        m_operations.add( operation );
        return operation;
    }
    
   /**
    * Add an transition within this state.
    * @return the operation datatype
    */
    public TransitionDataType createTransition()
    {
        if( null != m_uri )
        {
            final String error = 
              "Transitions may not be used in conjuction with a uri import.";
            throw new BuildException( error, m_task.getLocation() );
        }
        if( null != m_classname )
        {
            final String error = 
              "Transitions may not be used in conjuction with the class attribute.";
            throw new BuildException( error, m_task.getLocation() );
        }
        final TransitionDataType transition = new TransitionDataType();
        m_transitions.add( transition );
        return transition;
    }

   /**
    * Add an trigger to the state.
    * @return the trigger datatype
    */
    public TriggerDataType createTrigger()
    {
        if( null != m_uri )
        {
            final String error = 
              "Triggers may not be used in conjuction with a uri import.";
            throw new BuildException( error, m_task.getLocation() );
        }
        if( null != m_classname )
        {
            final String error = 
              "Triggers may not be used in conjuction with the class attribute.";
            throw new BuildException( error, m_task.getLocation() );
        }
        final TriggerDataType trigger = new TriggerDataType();
        m_triggers.add( trigger );
        return trigger;
    }
    
    State getState()
    {
        if( null != m_uri )
        {
            m_task.log( "importing state graph: " + m_uri );
            return StateBuilder.readGraph( m_uri );
        }
        else if( null != m_classname )
        {
            try
            {
                ClassLoader classloader = createClassLoader();
                Class clazz = classloader.loadClass( m_classname );
                return loadStateFromResource( clazz );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unable to load an embedded state xgraph from the resource: " 
                  + m_classname + ".xgraph";
                throw new BuildException( error, e );
            }
        }
        else
        {
            m_task.log( "creating embedded state graph" );
            String name = getStateName();
            Trigger[] triggers = getTriggers();
            Operation[] operations = getOperations();
            Transition[] transitions = getTransitions();
            State[] states = getStates();
            return new DefaultState( name, triggers, transitions, operations, states, m_terminal );
        }
    }

    String getStateName()
    {
        if( m_root )
        {
            return "";
        }
        else
        {
            return m_name;
        }
    }
    
    Trigger[] getTriggers()
    {
        TriggerDataType[] types = (TriggerDataType[]) m_triggers.toArray( new TriggerDataType[0] );
        Trigger[] values = new Trigger[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            TriggerDataType data = types[i];
            values[i] = data.getTrigger();
        }
        return values;
    }
    
    Operation[] getOperations()
    {
        OperationDataType[] types = (OperationDataType[]) m_operations.toArray( new OperationDataType[0] );
        Operation[] values = new Operation[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            OperationDataType data = types[i];
            values[i] = data.getOperation();
        }
        return values;
    }
    
    Transition[] getTransitions()
    {
        TransitionDataType[] types = (TransitionDataType[]) m_transitions.toArray( new TransitionDataType[0] );
        Transition[] values = new Transition[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            TransitionDataType data = types[i];
            values[i] = data.getTransition();
        }
        return values;
    }
    
    State[] getStates()
    {
        StateDataType[] types = (StateDataType[]) m_states.toArray( new StateDataType[0] );
        State[] values = new State[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            StateDataType data = types[i];
            values[i] = data.getState();
        }
        return values;
    }

   /**
    * Return the runtime classloader.
    * @return the classloader
    */
    private ClassLoader createClassLoader()
    {
        Project project = m_task.getProject();
        Path path = m_task.getContext().getPath( Scope.RUNTIME );
        File classes = m_task.getContext().getTargetClassesMainDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader parentClassLoader = ClassLoaderBuilderTask.class.getClassLoader();
        return new AntClassLoader( parentClassLoader, project, path, true );
    }

    private State loadStateFromResource( Class subject )
    {
        String resource = subject.getName().replace( '.', '/' ) + ".xgraph";
        try
        {
            URL url = subject.getClassLoader().getResource( resource );
            if( null == url )
            {
                return null;
            }
            else
            {
                InputStream input = url.openConnection().getInputStream();
                return DefaultStateMachine.load( input );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to load component state graph resource [" 
              + resource 
              + "].";
            throw new BuildException( error, e );
        }
    }
}