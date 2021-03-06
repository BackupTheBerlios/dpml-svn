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
import java.io.FileOutputStream;
import java.io.IOException;

import net.dpml.tools.tasks.GenericTask;

import net.dpml.state.State;
import net.dpml.state.StateEncoder;

import org.apache.tools.ant.BuildException;

/**
 * Task that handles the creation of an encoded state graph.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StateTask extends GenericTask
{
    private static final StateEncoder STATE_ENCODER = new StateEncoder();
    
    private File m_output;
    private String m_classname;
    private StateDataType m_data;
    
    StateDataType getData()
    {
        if( null == m_data )
        {
            m_data = new StateDataType( this, true );
        }
        return m_data;
    }
    
   /**
    * Override the default output destination.
    *
    * @param file the overriding destination
    */
    public void setDest( File file )
    {
        m_output = file;
    }

   /**
    * Override the class as the colocated destination.
    *
    * @param classname the classname of the target component.
    */
    public void setClass( String classname )
    {
        m_classname = classname;
    }

   /**
    * Set the state name.
    * @param name the cname of the state
    */
    public void setName( final String name )
    {
        getData().setName( name );
    }
    
   /**
    * Mark the state as a terminal state.
    * @param flag true if this is a terminal state
    */
    public void setTerminal( final boolean flag )
    {
        getData().setTerminal( flag );
    }
    
   /**
    * Add a substate within the state.
    * @return the sub-state datatype
    */
    public StateDataType createState()
    {
        return getData().createState();
    }
    
   /**
    * Add an operation within this state.
    * @return the operation datatype
    */
    public OperationDataType createOperation()
    {
        return getData().createOperation();
    }
    
   /**
    * Add an interface within this state.
    * @return the interface datatype
    */
    public InterfaceDataType createInterface()
    {
        return getData().createInterface();
    }
    
   /**
    * Add an transition within this state.
    * @return the operation datatype
    */
    public TransitionDataType createTransition()
    {
        return getData().createTransition();
    }

   /**
    * Add an trigger to the state.
    * @return the trigger datatype
    */
    public TriggerDataType createTrigger()
    {
        return getData().createTrigger();
    }
    
   /**
    * Execute the task.
    */
    public void execute()
    {
        File file = getOutputFile();
        File parent = file.getParentFile();
        if( !parent.exists() )
        {
            parent.mkdirs();
        }
        createGraph( file );
    }
    
   /**
    * Create an encoded state graph.
    * @param file the output file
    */
    public void createGraph( File file )
    {
        try
        {
            FileOutputStream output = new FileOutputStream( file );
            try
            {
                State graph = getData().getState();
                STATE_ENCODER.export( graph, output );
            }
            catch( Exception e )
            {
                throw new BuildException( "State encoding error.", e );
            }
            finally
            {
                try
                {
                    output.close();
                }
                catch( IOException ioe )
                {
                    ioe.printStackTrace();
                }
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to build the graph.";
            throw new BuildException( error, e, getLocation() );
        }
    }

    private File getOutputFile()
    {
        if( null != m_output )
        {
            return m_output;
        }
        else if( null != m_classname )
        {
            File classes = getContext().getTargetClassesMainDirectory();
            String path = m_classname.replace( '.', '/' );
            String filename = path + ".xgraph";
            return new File( classes, filename );
        }
        else
        {
            return getDefaultOutputFile();
        }
    }
    
   /**
    * Create and return the part output file. 
    * @return the part output file
    */
    protected File getDefaultOutputFile()
    {
        File deliverables = getContext().getTargetDeliverablesDirectory();
        String type = State.TYPE;
        String types = type + "s";
        File dir = new File( deliverables, types );
        String filename = getContext().getLayoutPath( type );
        return new File( dir, filename );
    }
}
