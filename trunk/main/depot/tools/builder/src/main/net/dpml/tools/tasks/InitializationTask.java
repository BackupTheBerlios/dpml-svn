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
import net.dpml.tools.model.ProcessorNotFoundException;

import net.dpml.tools.process.JarProcess;
import net.dpml.tools.process.PluginProcess;
import net.dpml.tools.process.ModuleProcess;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildException;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;

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
        
        try
        {
            Processor[] processors = getProcessorSequence( resource );
            BuildListener[] listeners = getBuildListeners( processors );
            for( int i=0; i<listeners.length; i++ )
            {
                BuildListener listener = listeners[i];
                getProject().addBuildListener( listener );
            }
        }
        catch( ProcessorInstantiationException e )
        {
            final String error = 
              "Processor instantiation error.";
            throw new BuildException( error, e );
        }
    }
    
    private BuildListener[] getBuildListeners( Processor[] processors )
      throws ProcessorInstantiationException
    {
        Repository repository = Transit.getInstance().getRepository();
        BuildListener[] listeners = new BuildListener[ processors.length ];
        for( int i=0; i<processors.length; i++ )
        {
            Processor processor = processors[i];
            String name = processor.getName();
            URI uri = processor.getCodeBaseURI();
            if( null == uri )
            {
                String classname = processor.getClassname();
                if( null == classname )
                {
                    final String error = 
                      "Missing processor uri or classname in processor [" 
                      + name 
                      + "].";
                    throw new IllegalStateException( error );
                }
                
                try
                {
                    ClassLoader classloader = getClass().getClassLoader();
                    Class clazz = classloader.loadClass( classname );
                    Object[] args = new Object[]{ processor };
                    listeners[i] = (BuildListener) repository.instantiate( clazz, args );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Internal error while attempting to load a local processor."
                      + "\nClass: " + classname
                      + "\nName: " + name;
                    throw new ProcessorInstantiationException( error, e );
                }
            }
            else
            {
                try
                {
                    String classname = processor.getClassname();
                    Object[] params = new Object[]{processor};
                    ClassLoader classloader = getClass().getClassLoader();
                    if( null == classname )
                    {
                        listeners[i] = 
                          (BuildListener) Transit.getInstance().getRepository().getPlugin( 
                            classloader, uri, params );
                    }
                    else
                    {
                        ClassLoader loader = repository.getPluginClassLoader( classloader, uri );
                        Class c = loader.loadClass( classname );
                        listeners[i] = (BuildListener) repository.instantiate( c, params );
                    }
                }
                catch( ClassCastException e )
                {
                    final String error = 
                      "Build processor [" 
                      + name
                      + "] from uri ["
                      + uri
                      + "] is not a build listener.";
                    throw new BuildException( error, getLocation() );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Internal error while attempting to load an external processor."
                      + "\nURI: " + uri
                      + "\nName: " + name;
                    throw new ProcessorInstantiationException( error, e );
                }
            }
        }
        return listeners;
    }
    
    private Processor[] getProcessorSequence( Resource resource )
    {
        try
        {
            return getWorkbench().getProcessorSequence( resource );
        }
        catch( ProcessorNotFoundException e )
        {
            final String error = 
              "Internal referential error while resolving processors for ["
              + resource
              + "]";
            throw new BuildException( error, e );
        }
    }
}
