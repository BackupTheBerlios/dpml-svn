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

package net.dpml.tools.tasks;

import java.io.File;

import net.dpml.library.Resource;
import net.dpml.library.impl.DefaultLibrary;
import net.dpml.tools.impl.StandardBuilder;

import net.dpml.lang.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;

/**
 * Resolves a sorted sequence of projects with a basedir equal to or 
 * derived from the current ${basedir}.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ReactorTask extends Task
{
    private String m_target;
    
   /**
    * Set the target task name.
    * @param target the target task
    */
    public void setTarget( String target )
    {
        m_target = target;
    }
    
    private String[] getTargets()
    {
        if( null != m_target )
        {
            return new String[]{m_target};
        }
        else
        {
            return new String[0];
        }
    }
    
   /**
    * Execute the task.
    */
    public void execute()
    {
        try
        {
            Logger logger = new LoggingAdapter();
            DefaultLibrary library = new DefaultLibrary( logger );
            StandardBuilder builder = new StandardBuilder( logger, library, false );
            File basedir = getProject().getBaseDir().getCanonicalFile();
            Resource[] resources = library.select( basedir, false );
            log( "Reactive selection: " + resources.length );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                if( !resource.getBaseDir().equals( basedir ) )
                {
                    executeTarget( builder, resource );
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
              "Unexpected error while resolving reactive project list.";
            throw new BuildException( error, e );
        }
    }
    
    private void executeTarget( StandardBuilder builder, final Resource resource ) throws BuildException
    {
        log( "Executing reactive build: " + resource );
        final Ant ant = (Ant) getProject().createTask( "ant" );
        ant.setDir( resource.getBaseDir() );
        ant.setInheritRefs( false );
        ant.setInheritAll( false );

        File template = builder.getTemplateFile( resource );
        ant.setAntfile( template.toString() );
        if( null != m_target )
        {
            if( !"default".equals( m_target ) )
            {
                log( "building " + resource + " with target: " + m_target );
                ant.setTarget( m_target );
            }
            else
            {
                log( "building " + resource + " with default target" );
            }
        }
        else
        {
            log( "building " + resource + " with default target" );
        }
        ant.init();
        ant.execute();
    }
}
