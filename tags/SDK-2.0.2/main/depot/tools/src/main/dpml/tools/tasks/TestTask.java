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

package dpml.tools.tasks;

import java.io.File;

import dpml.library.Resource;
import dpml.library.Scope;
import dpml.library.Type;

import dpml.tools.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Cleanup of generated target directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TestTask extends GenericTask
{
   /**
    * Compiles main and test classes resulting in the creation of the 
    * target/classes/main and target/classes/test directories.
    */
    public void execute()
    {
        Context context = getContext();
        Project project = context.getProject();
        File src = context.getTargetBuildTestDirectory();
        if( src.exists() )
        {
            Path path = context.getPath( Scope.TEST  );
            Resource resource = getResource();
            if( resource.isa( "jar" ) )
            {
                Type type = resource.getType( "jar" );
                File jar = type.getFile( true );
                path.createPathElement().setLocation( jar );
            }
            
            //final File jar = getJarFile( context );
            //if( jar.exists() )
            //{
            //    path.createPathElement().setLocation( jar );
            //}
            //else if( context.getTargetClassesMainDirectory().exists() )
            //{
            //    File main = context.getTargetClassesMainDirectory();
            //    path.createPathElement().setLocation( main );
            //}
            
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
    
    //private File getJarFile( Context context )
    //{
    //    Project project = context.getProject();
    //    File deliverables = context.getTargetDeliverablesDirectory();
    //    File jars = new File( deliverables, "jars" );
    //    String filename = context.getLayoutFilename( "jar" );
    //    return new File( jars, filename );
    //}
}
