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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import net.dpml.library.model.Resource;

import net.dpml.tools.model.Processor;
import net.dpml.tools.model.Workbench;

import net.dpml.tools.process.JarProcess;
import net.dpml.tools.process.PluginProcess;
import net.dpml.tools.process.ModuleProcess;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildException;

import net.dpml.transit.Transit;

/**
 * Execute the install phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InitializationTask extends GenericTask
{
    private ArrayList m_list = new ArrayList();
    
   /**
    * Initialize type to processor mapping.
    */
    public void execute()
    {
        if( null != getProject().getReference( "project.timestamp" ) )
        {
            return;
        }
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        getProject().addReference( "project.timestamp", new Date() );
        Resource resource = getResource();
        log( resource.toString(), Project.MSG_VERBOSE );
        Processor[] processors = getWorkbench().getProcessorSequence( resource );
        for( int i=0; i<processors.length; i++ )
        {
            Processor processor = processors[i];
            String name = processor.getName();
            
            // TODO: strip about the following hard-coded references to 
            // listener names and do the implementation properly 
            // based on standard and declared processors
            
            try
            {
                if( name.equals( "jar" ) )
                {
                    JarProcess process = new JarProcess( processor );
                    getProject().addBuildListener( process );
                }
                else if( name.equals( "plugin" ) )
                {
                    PluginProcess process = new PluginProcess();
                    getProject().addBuildListener( process );
                }
                else if( name.equals( "module" ) )
                {
                    ModuleProcess process = new ModuleProcess();
                    getProject().addBuildListener( process );
                }
                else
                {
                    ClassLoader classloader = getClass().getClassLoader();
                    Project project = getProject();
                    URI uri = processor.getCodeBaseURI();
                    if( null == uri )
                    {
                        final String error = 
                          "Processor [" 
                          + name
                          + "] does not declare a plugin uri.";
                        throw new IllegalStateException( error );
                    }
                    
                    Object object = null;
                    String classname = processor.getClassname();
                    Object[] params = new Object[]{project, resource, processor};
                    if( null == classname )
                    {
                        object = 
                          Transit.getInstance().getRepository().getPlugin( 
                          classloader, uri, params );
                    }
                    else
                    {
                        ClassLoader loader = 
                          Transit.getInstance().getRepository().getPluginClassLoader( 
                            classloader, uri );
                        Class c = loader.loadClass( classname );
                        object = Transit.getInstance().getRepository().instantiate( c, params ); 
                    }
                    if( object instanceof BuildListener )
                    {
                        BuildListener listener = (BuildListener) object;
                        getProject().addBuildListener( listener );
                        log( "registered listener: " + listener.getClass().getName() );
                    }
                    else
                    {
                        final String error = 
                          "Build processor [" 
                          + name
                          + "] from uri ["
                          + uri
                          + "] is not a build listener.";
                        throw new BuildException( error, getLocation() );
                    }
                }
            }
            catch( Exception e )
            {
                final String error = 
                  "Failed to establish build listener for type [" + name + "].";
                throw new BuildException( error, getLocation() );
            }
        }
    }
}
