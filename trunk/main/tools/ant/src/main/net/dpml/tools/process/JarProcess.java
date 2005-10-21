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
import java.rmi.RemoteException;

import net.dpml.tools.tasks.JavacTask;
import net.dpml.tools.tasks.PrepareTask;
import net.dpml.tools.tasks.JarTask;
import net.dpml.tools.tasks.JUnitTestTask;

import net.dpml.tools.ant.Definition;
import net.dpml.tools.ant.Context;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.types.Path;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class JarProcess extends AbstractBuildListener
{
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
        String name = target.getName();
        if( "prepare".equals( name ) )
        {
            Project project = event.getProject();
            final PrepareTask task = new PrepareTask();
            task.setProject( project );
            task.setTaskName( "prepare" );
            task.init();
            task.execute();
        }
        else if( "build".equals( name ) )
        {
            Project project = event.getProject();
            final JavacTask task = new JavacTask();
            task.setProject( project );
            task.setTaskName( "javac" );
            task.setSrc( new File( project.getProperty( "project.target.build.main.dir" ) ) );
            task.setDest( new File( project.getProperty( "project.target.classes.main.dir" ) ) );
            task.setClasspathRef( "project.compile.path" );
            task.init();
            task.execute();
        }
        else if( "package".equals( name ) )
        {
            Project project = event.getProject();
            final JarTask task = new JarTask();
            task.setProject( project );
            task.setTaskName( "jar" );
            task.setSrc( new File( project.getProperty( "project.target.classes.main.dir" ) ) );
            final File jar = getJarFile( project );
            task.setDest( jar );
            task.init();
            task.execute();
        }
        else if( "test".equals( name ) )
        {
            Project project = event.getProject();
            File src = new File( project.getProperty( "project.target.build.test.dir" ) );
            if( src.exists() )
            {
                final File jar = getJarFile( project );
                Path testCompilePath = (Path) project.getReference( "project.test.path" );
                testCompilePath.createPathElement().setLocation( jar );
                final File dest = new File( project.getProperty( "project.target.classes.test.dir" ) );
                try
                {
                    final JavacTask task = new JavacTask();
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
        Definition definition = getDefinition( project );
        File deliverables = new File( project.getProperty( "project.target.deliverables.dir" ) );
        File jars = new File( deliverables, "jars" );
        String filename = definition.getLayoutPath( "jar" );
        return new File( jars, filename );
    }

   /**
    * Get the project definition.
    */
    protected Definition getDefinition( Project project )
    {
        return getContext( project ).getDefinition();
    }
    
   /**
    * Get the project definition.
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