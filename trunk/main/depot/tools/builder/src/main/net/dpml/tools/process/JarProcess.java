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

package net.dpml.tools.process;

import java.io.File;

import net.dpml.library.info.Scope;

import net.dpml.tools.tasks.JavacTask;
import net.dpml.tools.tasks.JarTask;
import net.dpml.tools.tasks.JUnitTestTask;

import net.dpml.tools.model.Context;
import net.dpml.tools.model.Processor;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.types.Path;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class JarProcess extends AbstractBuildListener
{
    private Processor m_processor;
    
   /**
    * Creation of a new jar process using the supplied 
    * processor as the source for property overrived.
    * @param processor the processor definition
    */
    public JarProcess( Processor processor )
    {
        m_processor = processor;
    }
    
    /**
     * Signals that a target is starting.
     *
     * @param event the build event.
     *
     * @see BuildEvent#getTarget()
     */
    public void targetStarted( BuildEvent event )
    {
        Target target = event.getTarget();
        String name = target.getName();
        if( "build".equals( name ) )
        {
            Project project = event.getProject();
            Context context = getContext( project );
            final JavacTask task = new JavacTask( context, m_processor );
            task.init();
            task.execute();
        }
        else if( "package".equals( name ) )
        {
            Project project = event.getProject();
            File jarSrcDir = new File( project.getProperty( "project.target.classes.main.dir" ) );
            if( jarSrcDir.exists() )
            {
                final JarTask task = new JarTask();
                task.setProject( project );
                task.setTaskName( "jar" );
                task.setSrc( jarSrcDir );
                final File jar = getJarFile( project );
                task.setDest( jar );
                task.init();
                task.execute();
            }
        }
        else if( "test".equals( name ) )
        {
            Project project = event.getProject();
            Context context = getContext( project );
            File src = new File( project.getProperty( "project.target.build.test.dir" ) );
            if( src.exists() )
            {
                final File jar = getJarFile( project );
                Path testCompilePath = context.getPath( Scope.TEST  );
                if( jar.exists() )
                {
                    testCompilePath.createPathElement().setLocation( jar );
                }
                final File dest = new File( project.getProperty( "project.target.classes.test.dir" ) );
                try
                {
                    final JavacTask task = new JavacTask( context, m_processor );
                    task.setProject( project );
                    task.setTaskName( "javac" );
                    task.setSrc( src );
                    task.setDest( dest );
                    task.setClasspath( testCompilePath );
                    task.init();
                    task.execute();
                }
                catch( BuildException e )
                {
                    throw e;
                }
                try
                {
                    testCompilePath.createPathElement().setLocation( dest );
                    final JUnitTestTask task = new JUnitTestTask();
                    task.setProject( project );
                    task.setTaskName( "junit" );
                    task.setSrc( src );
                    task.setClasspath( testCompilePath );
                    task.init();
                    task.execute();
                }
                catch( BuildException e )
                {
                    throw e;
                }
            }
        }
    }
    
    private File getJarFile( Project project )
    {
        Context context = getContext( project );
        File deliverables = new File( project.getProperty( "project.target.deliverables.dir" ) );
        File jars = new File( deliverables, "jars" );
        String filename = context.getLayoutPath( "jar" );
        return new File( jars, filename );
    }
    
   /**
    * Get the project definition.
    * @param project the project
    * @return the build context
    */
    protected Context getContext( Project project )
    {
        Context context = (Context) project.getReference( "project.context" );
        if( null == context )
        {
            final String error = 
              "Missing project context reference.";
            throw new BuildException( error );
        }
        return context;
    }
}
