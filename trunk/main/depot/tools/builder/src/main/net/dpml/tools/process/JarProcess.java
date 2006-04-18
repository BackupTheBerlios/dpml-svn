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

import net.dpml.tools.model.Context;
//import net.dpml.tools.model.Processor;

import net.dpml.tools.tasks.JavacTask;
import net.dpml.tools.tasks.JarTask;
import net.dpml.tools.tasks.JUnitTestTask;

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
public class JarProcess extends AbstractProcessor
{
    public void build( Context context )
    {
        Project project = context.getProject();
        final JavacTask task = new JavacTask( context );
        task.setProject( project );
        task.init();
        task.execute();
    }
    
    public void pack( Context context )
    {
        Project project = context.getProject();
        String jarSrcPath = context.getProperty( "project.target.classes.main.dir" );
        File jarSrcDir = new File( jarSrcPath );
        if( jarSrcDir.exists() )
        {
            final JarTask task = new JarTask();
            task.setProject( project );
            task.setTaskName( "jar" );
            task.setSrc( jarSrcDir );
            final File jar = getJarFile( context );
            task.setDest( jar );
            task.init();
            task.execute();
        }
    }
    
    public void validate( Context context )
    {
        Project project = context.getProject();
        String srcPath = context.getProperty( "project.target.build.test.dir" );
        File src = new File( srcPath );
        if( src.exists() )
        {
            final File jar = getJarFile( context );
            Path testCompilePath = context.getPath( Scope.TEST  );
            if( jar.exists() )
            {
                testCompilePath.createPathElement().setLocation( jar );
            }
            final File dest = new File( project.getProperty( "project.target.classes.test.dir" ) );
            try
            {
                final JavacTask task = new JavacTask( context );
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
    
    private File getJarFile( Context context )
    {
        Project project = context.getProject();
        String deliverablesPath = context.getProperty( "project.target.deliverables.dir" );
        File deliverables = new File( deliverablesPath );
        File jars = new File( deliverables, "jars" );
        String filename = context.getLayoutPath( "jar" );
        return new File( jars, filename );
    }
    
}
