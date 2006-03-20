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
import java.io.FileOutputStream;

import net.dpml.tools.model.Context;

import net.dpml.library.Resource;
import net.dpml.library.Module;
import net.dpml.library.info.Scope;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.impl.LibraryEncoder;

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
        final String path = context.getLayoutPath( "module" );
        final File deliverables = context.getTargetDeliverablesDirectory();
        final File modules = new File( deliverables, "modules" );
        final File module = new File( modules, path );
        writeModuleFile( module );
    }

    private void writeModuleFile( final File file )
    {
        Resource resource = getResource();
        if( resource instanceof Module )
        {
            Module module = (Module) resource;
            ModuleDirective directive = (ModuleDirective) module.export();
            writeModuleDirective( directive, file );
        }
        else
        {
            final String error = 
              "Project is not a module.";
            throw new BuildException( error, getLocation() );
        }
    }
    
    private void writeModuleDirective( ModuleDirective directive, File file )
    {
        FileOutputStream output = null;
        try
        {
            log( "Exporting module to: " + file );
            final LibraryEncoder encoder = new LibraryEncoder();
            file.getParentFile().mkdirs();
            output = new FileOutputStream( file );
            encoder.export( directive, output );
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
}
