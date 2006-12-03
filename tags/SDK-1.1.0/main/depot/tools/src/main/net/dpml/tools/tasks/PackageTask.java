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

import net.dpml.tools.Context;
import net.dpml.tools.BuildError;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Cleanup of generated target directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PackageTask extends GenericTask
{
   /**
    * Compiles main and test classes resulting in the creation of the 
    * target/classes/main and target/classes/test directories.
    */
    public void execute()
    {
        Context context = getContext();
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
                e.printStackTrace();
                final String error = 
                  "Unexpected failure during part externalization in ["
                    + resource.getName()
                    + "]";
                throw new BuildError( error, e, getLocation() );
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
    
    private File getJarFile( Context context )
    {
        Project project = context.getProject();
        File deliverables = context.getTargetDeliverablesDirectory();
        File jars = new File( deliverables, "jars" );
        String filename = context.getLayoutFilename( "jar" );
        return new File( jars, filename );
    }
}
