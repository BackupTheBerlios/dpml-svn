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

import net.dpml.library.info.Scope;

import net.dpml.tools.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Cleanup of generated target directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class BuildTask extends GenericTask
{
   /**
    * Compiles main and test classes resulting in the creation of the 
    * target/classes/main and target/classes/test directories.
    */
    public void execute()
    {
        // If a target/build/main or target/build/test exists then we should be  
        // triggering content compilation.  Compilation requires project input 
        // with respect to javac source selection and subsequent rmic 
        // postprocessing (both of which can be expressed as process data associated
        // with the target/build/main and target/build/test directories). In addition
        // there may be additional processes that need to be triggered (such as 
        // type compilation which consumes compiled classes).
        
        Context context = getContext();
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
}
