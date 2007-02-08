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
import java.io.FileOutputStream;

import dpml.library.Resource;
import dpml.library.Module;
import dpml.library.Scope;
import dpml.library.Type;
import dpml.library.info.ModuleDirective;
import dpml.library.info.LibraryEncoder;

import dpml.tools.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

/**
 * Write a module to XML in a form suitable for publication.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModuleTask extends GenericTask
{
   /**
    * Task initialization.
    * @exception BuildException if a build error occurs.
    */
    public void init() throws BuildException
    {
        if( !isInitialized() )
        {
            super.init();
            getContext().getPath( Scope.TEST );
        }
    }
    
   /**
    * Execute the task.
    */
    public void execute()
    {
        Project project = getProject();
        final Context context = getContext();
        Resource resource = getResource();
        Type type = resource.getType( "module" );
        File module = type.getFile( true );
        writeModuleFile( module );
    }

    private void writeModuleFile( final File file )
    {
        Resource resource = getResource();
        if( resource instanceof Module )
        {
            Module module = (Module) resource;
            FileOutputStream output = null;
            try
            {
                log( "Exporting module to: " + file );
                final LibraryEncoder encoder = new LibraryEncoder();
                file.getParentFile().mkdirs();
                output = new FileOutputStream( file );
                module.export( output );
            }
            catch( Exception e )
            {
                final String error = 
                  "Failed to export module.";
                throw new BuildException( error, e, getLocation() );
            }
            finally
            {
                if( null != output )
                {
                    try
                    {
                        output.close();
                    }
                    catch( Exception ioe )
                    {
                    }
                }
            }
        }
        else
        {
            final String error = 
              "Project is not a module.";
            throw new BuildException( error, getLocation() );
        }
    }
}
