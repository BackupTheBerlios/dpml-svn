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

import java.io.File;

import net.dpml.library.Resource;
import net.dpml.library.Type;
import net.dpml.library.info.Scope;
import net.dpml.library.info.JarTypeDirective;

import net.dpml.tools.Context;

import net.dpml.tools.tasks.PrepareTask;
import net.dpml.tools.tasks.InstallTask;
import net.dpml.tools.tasks.JavacTask;
import net.dpml.tools.tasks.JarTask;
import net.dpml.tools.tasks.JUnitTestTask;
import net.dpml.tools.tasks.ModuleTask;
import net.dpml.tools.tasks.RMICTask;
import net.dpml.tools.tasks.PartTask;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Delete;

/**
 * Standard process dealing with context initialization, codfebase 
 * normalization, and artifact installation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardProcess extends AbstractProcessor
{
   /**
    * Handles context initialization during which path definitions
    * for runtime and test concerns are bound to the active project.
    *
    * @param context the working context
    */
    public void initialize( Context context )
    {
    }
    
   /**
    * Handles normalization of a codebase during which a working copy of 
    * the codebase is created under the target/build directory.  Global 
    * filters are applied and merging of the src/main with etc/main and 
    * src/test wityh etc/test is undertaken.
    *
    * @param context the working context
    */
    public void prepare( Context context )
    {
        context.init();
        context.getProject().log( "commencing preparation", Project.MSG_VERBOSE );
        Project project = context.getProject();
        final PrepareTask task = new PrepareTask();
        task.setProject( project );
        task.setTaskName( "prepare" );
        task.init();
        task.execute();
        context.getProject().log( "preparation complete", Project.MSG_VERBOSE );
    }
    
   /**
    * Handles build related concerns based on the target directory layout content.
    *
    * @param context the working context
    */
    public void build( Context context )
    {
        // If a target/build/main or target/build/test exists then we should be  
        // triggering content compilation.  Compilation requires project input 
        // with respect to javac source selection and subsequent rmic 
        // postprocessing (both of which can be expressed as process data associated
        // with the target/build/main and target/build/test directories). In addition
        // there may be additional processes that need to be triggered (such as 
        // type compilation which consumes compiled classes).
        
        if( context.getTargetBuildMainDirectory().exists() )
        {
            // we need to add in here the aquisition of javac task 
            // parameters based on transformation directives associated 
            // with the target/classes/main directory
            
            Project project = context.getProject();
            final JavacTask task = new JavacTask( context );
            task.setProject( project );
            task.init();
            task.execute();
        }
        
        // resolve rmic selection
        
        Resource resource = context.getResource();
        if( resource.isa( "jar" ) )
        {
            Type type = resource.getType( "jar" );
            if( type instanceof JarTypeDirective )
            {
                JarTypeDirective directive = (JarTypeDirective) type;
                String[] includes = directive.getRMICIncludes();
                String[] excludes = directive.getRMICExcludes();
                RMICTask rmicTask = new RMICTask( context );
                Project project = context.getProject();
                rmicTask.setProject( project );
                rmicTask.setIncludes( includes );
                rmicTask.setExcludes( excludes );
                rmicTask.init();
                rmicTask.execute();
            }
        }
        
        // conditionaly compile test classes
        
        File source = context.getTargetBuildTestDirectory();
        if( source.exists() )
        {
            // we need to add in here the aquisition of javac task 
            // parameters based on transformation directives associated 
            // with the target/classes/main directory
            
            Project project = context.getProject();
            final JavacTask task = new JavacTask( context );
            task.setProject( project );
            File destination = context.getTargetClassesTestDirectory();
            task.setSrc( source );
            task.setDest( destination );
            Path path = context.getPath( Scope.TEST  );
            File main = context.getTargetClassesMainDirectory();
            if( main.exists() )
            {
                path.createPathElement().setLocation( main );
            }
            task.setClasspath( path );
            task.init();
            task.execute();
        }
    }

   /**
    * Packaging of type-specific data.
    * @param context the working context
    */
    public void pack( Context context )
    {
        Project project = context.getProject();
        Resource resource = context.getResource();
        
        // handle jar file packaging
        
        if( resource.isa( "jar" ) )
        {
            File base = context.getTargetClassesMainDirectory();
            final File jar = getJarFile( context );
            if( base.exists() )
            {
                final JarTask task = new JarTask();
                task.setProject( project );
                task.setTaskName( "jar" );
                task.setSrc( base );
                task.setDest( jar );
                task.init();
                task.execute();
            }
        }
        
        // handle part production
        
        if( resource.isa( "part" ) )
        {
            try
            {
                PartTask task = new PartTask();
                task.setProject( project );
                task.setTaskName( "part" );
                task.init();
                task.execute();
            }
            catch( BuildException e )
            {
                throw e;
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected failure during part externalization.";
                throw new BuildException( error, e );
            }
        }
        
        // handle module export
        
        if( resource.isa( "module" ) )
        {
            final ModuleTask task = new ModuleTask();
            task.setProject( project );
            task.setTaskName( "module" );
            task.init();
            task.execute();
        }
    }
    
   /**
    * Datatype validation.
    * @param context the working context
    */
    public void validate( Context context )
    {
        Project project = context.getProject();
        File src = context.getTargetBuildTestDirectory();
        if( src.exists() )
        {
            Path path = context.getPath( Scope.TEST  );
            final File jar = getJarFile( context );
            if( jar.exists() )
            {
                path.createPathElement().setLocation( jar );
            }
            else if( context.getTargetClassesMainDirectory().exists() )
            {
                File main = context.getTargetClassesMainDirectory();
                path.createPathElement().setLocation( main );
            }
            final File dest = context.getTargetClassesTestDirectory();
            path.createPathElement().setLocation( dest );
            final JUnitTestTask task = new JUnitTestTask();
            task.setProject( project );
            task.setTaskName( "junit" );
            task.setSrc( src );
            task.setClasspath( path );
            task.init();
            task.execute();
        }
    }
    
   /**
    * Handles replication of content under target/deliverables to the 
    * common cache directory - includes validation of the presence of declared 
    * artifacts during execution.
    *
    * @param context the working context
    */
    public void install( Context context )
    {
        Project project = context.getProject();
        final InstallTask task = new InstallTask();
        task.setProject( project );
        task.setTaskName( "install" );
        task.init();
        task.execute();
    }

   /**
    * Handles cleanup of generated content.
    *
    * @param context the working context
    */
    public void clean( Context context )
    {
        File dir = context.getTargetDirectory();
        Project project = context.getProject();
        final Delete task = new Delete();
        task.setProject( project );
        task.setTaskName( "delete" );
        task.setDir( dir );
        task.init();
        task.execute();
    }

    private File getJarFile( Context context )
    {
        Project project = context.getProject();
        File deliverables = context.getTargetDeliverablesDirectory();
        File jars = new File( deliverables, "jars" );
        String filename = context.getLayoutPath( "jar" );
        return new File( jars, filename );
    }
}
