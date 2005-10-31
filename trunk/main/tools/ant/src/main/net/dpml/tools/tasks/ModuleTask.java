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

import java.beans.Encoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;

import net.dpml.tools.ant.Context;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Type;
import net.dpml.tools.info.ModuleDirective;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModuleTask extends GenericTask
{
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
            ModuleDirective directive = module.export();
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
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( ModuleDirective.class.getClassLoader() );
        FileOutputStream output = null;
        try
        {
            log( "Exporting module to: " + file );
            file.getParentFile().mkdirs();
            output = new FileOutputStream( file );
            final BufferedOutputStream buffer = new BufferedOutputStream( output );
            XMLEncoder encoder = new XMLEncoder( buffer );
            encoder.setExceptionListener( new ModuleEncoderListener() );
            try
            {
                encoder.writeObject( directive );
                checksum( file );
                asc( file );
            }
            finally
            {
                encoder.close();
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Failed to export module.";
            throw new BuildException( error, e, getLocation() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
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
    
    private class ModuleEncoderListener implements ExceptionListener
    {
        public void exceptionThrown( Exception e )
        {
            Throwable cause = e.getCause();
            if( null != cause )
            {
                if( cause instanceof EncodingRuntimeException )
                {
                    EncodingRuntimeException ere = (EncodingRuntimeException) cause;
                    throw ere;
                }
                else
                {
                    final String error = 
                      "An error occured while attempting to encode module ["
                      + getResource().getResourcePath()
                      + "]\nCause: " + cause.toString();
                    throw new EncodingRuntimeException( error, cause );
                }
            }
            else
            {
                final String error = 
                  "An unexpected error occured while attempting to encode the type ["
                  + getResource().getResourcePath()
                  + "] due to: " + e.toString();
                throw new EncodingRuntimeException( error, e );
            }
        }
    }
    
    private static class EncodingRuntimeException extends RuntimeException
    {
        public EncodingRuntimeException( String message, Throwable cause )
        {
            super( message, cause );
        }
    }
}
