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

import net.dpml.tools.model.Type;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Processor;

import net.dpml.tools.ant.StandardBuilder;
import net.dpml.tools.process.JarProcess;
import net.dpml.tools.process.PluginProcess;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildException;

import net.dpml.transit.Transit;

/**
 * Execute the install phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
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
        getProject().addReference( "project.timestamp", new Date() );
        Resource resource = getResource();
        String info = getProject().getProperty( "project.info" );
        getProject().log( info );
        Type[] types = resource.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            Type type = types[i];
            String name = type.getName();
            try
            {
                if( name.equals( "jar" ) )
                {
                    JarProcess process = new JarProcess();
                    getProject().addBuildListener( process );
                }
                else if( name.equals( "plugin" ) )
                {
                    PluginProcess process = new PluginProcess();
                    getProject().addBuildListener( process );
                }
                else
                {
                    ClassLoader classloader = getClass().getClassLoader();
                    Project project = getProject();
                    Library library = getLibrary();
                    Processor processor = library.getProcessor( type );
                    URI uri = processor.getCodeBaseURI();
                    if( null == uri )
                    {
                        final String error = 
                          "Type process [" 
                          + type.getName()
                          + "] does not declare a plugin uri.";
                        throw new IllegalStateException( error );
                    }
                    Object[] params = new Object[]{project};
                    Object object = 
                      Transit.getInstance().getRepository().getPlugin( 
                        classloader, uri, params );
                    if( object instanceof BuildListener )
                    {
                        BuildListener listener = (BuildListener) object;
                        getProject().addBuildListener( listener );
                        log( "registered listener: " + listener.getClass().getName() );
                    }
                    else
                    {
                        final String error = 
                          "Build processor required for the type ["
                          + type
                          + "] with the uri ["
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
