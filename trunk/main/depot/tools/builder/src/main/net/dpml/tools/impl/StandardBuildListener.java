/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.impl;

import net.dpml.tools.model.Context;
import net.dpml.tools.model.Processor;

import net.dpml.tools.process.StandardProcess;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Project;

/**
 * Standard build listener.  The implementation provides support
 * for codebase structure normalization, and delegation of type-specific
 * build functions to build processors. 
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardBuildListener implements BuildListener
{
    private final Context m_context;
    private final StandardProcess m_standard;
    
   /**
    * Creation of a new standard build listener.
    * @param context the wotrking context
    */
    public StandardBuildListener( Context context )
    {
        if( null == context )
        {
            throw new NullPointerException( "context" );
        }
        
        m_context = context;
        m_standard = new StandardProcess();
    }
    
    /**
     * Signals that a build has started. This event
     * is fired before any targets have started.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     */
    public void buildStarted( BuildEvent event )
    {
    }

    /**
     * Signals that the last target has finished. This event
     * will still be fired if an error occurred during the build.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getException()
     */
    public void buildFinished( BuildEvent event )
    {
    }

    /**
     * Signals that a target is starting.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTarget()
     */
    public void targetStarted( BuildEvent event )
    {
        Target target = event.getTarget();
        String targetName = target.getName();
        Processor[] processors = m_context.getProcessors();
        if( "init".equals( targetName ) )
        {
            event.getProject().log( "executing initialization phase", Project.MSG_VERBOSE );
            m_standard.initialize( m_context );
            for( int i=0; i<processors.length; i++ )
            {
                Processor processor = processors[i];
                processor.initialize( m_context );
            }
        }
        else if( "prepare".equals( targetName ) )
        {
            event.getProject().log( "executing preparation phase", Project.MSG_VERBOSE );
            m_standard.prepare( m_context );
            for( int i=0; i<processors.length; i++ )
            {
                Processor processor = processors[i];
                processor.prepare( m_context );
            }
        }
        else if( "build".equals( targetName ) )
        {
            event.getProject().log( "executing build phase", Project.MSG_VERBOSE );
            m_standard.build( m_context );
            for( int i=0; i<processors.length; i++ )
            {
                Processor processor = processors[i];
                processor.build( m_context );
            }
        }
        else if( "package".equals( targetName ) )
        {
            event.getProject().log( "executing packaging phase", Project.MSG_VERBOSE );
            m_standard.pack( m_context );
            for( int i=0; i<processors.length; i++ )
            {
                Processor processor = processors[i];
                processor.pack( m_context );
            }
        }
        else if( "test".equals( targetName ) )
        {
            event.getProject().log( "executing validation phase", Project.MSG_VERBOSE );
            m_standard.validate( m_context );
            for( int i=0; i<processors.length; i++ )
            {
                Processor processor = processors[i];
                processor.validate( m_context );
            }
        }
        else if( "install".equals( targetName ) )
        {
            event.getProject().log( "executing installation phase", Project.MSG_VERBOSE );
            m_standard.install( m_context );
            for( int i=0; i<processors.length; i++ )
            {
                Processor processor = processors[i];
                processor.install( m_context );
            }
        }
    }

    /**
     * Signals that a target has finished. This event will
     * still be fired if an error occurred during the build.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getException()
     */
    public void targetFinished( BuildEvent event )
    {
    }
    
    /**
     * Signals that a task is starting.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTask()
     */
    public void taskStarted( BuildEvent event )
    {
    }

    /**
     * Signals that a task has finished. This event will still
     * be fired if an error occurred during the build.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getException()
     */
    public void taskFinished( BuildEvent event )
    {
    }

    /**
     * Signals a message logging event.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getMessage()
     * @see BuildEvent#getPriority()
     */
    public void messageLogged( BuildEvent event )
    {
    }
}
