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

package net.dpml.tools.process;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import net.dpml.lang.Plugin;
import net.dpml.transit.Transit;

import net.dpml.library.info.Scope;
import net.dpml.library.model.Resource;
import net.dpml.library.model.Type;
import net.dpml.library.util.PluginFactory;
import net.dpml.library.util.DefaultPluginFactory;

import net.dpml.tools.tasks.GenericTask;
import net.dpml.tools.model.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginProcess extends AbstractBuildListener
{
    /**
     * Signals that a target is finished.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTarget()
     */
    public void targetFinished( BuildEvent event )
    {
        Target target = event.getTarget();
        String name = target.getName();
        if( "build".equals( name ) )
        {
            try
            {
                Project project = event.getProject();
                final Context context = getContext( project );
                Resource resource = context.getResource();
                final String path = context.getLayoutPath( TYPE );
                final File targetDir = context.getTargetDirectory();
                
                PluginFactory factory = getPluginFactory( resource );
                Plugin plugin = factory.build( targetDir, resource );
                
                // extenalize the plugin to XML
                
                final File deliverables = context.getTargetDeliverablesDirectory();
                final File plugins = new File( deliverables, "plugins" );
                final File file = new File( plugins, path );
                plugins.mkdirs();
                file.createNewFile();
                final OutputStream output = new FileOutputStream( file );
                
                try
                {
                    plugin.write( output );
                }
                finally
                {
                    try
                    {
                        output.close();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();
                    }
                }
                
                // md5 and asc
                
                GenericTask task = new GenericTask();
                task.setProject( project );
                task.setTaskName( "plugin" );
                task.init();
                task.checksum( file );
                task.asc( file );
                
            }
            catch( Exception e )
            {
                final String error =
                  "An error occured during the creation of the plugin descriptor.";
                throw new BuildException( error, e ); 
            }
        }
    }
    
    private PluginFactory getPluginFactory( Resource resource ) throws Exception
    {
        Type type = resource.getType( TYPE );
        String spec = type.getProperty( "project.plugin.factory" );
        if( null == spec )
        {
            return new DefaultPluginFactory();
        }
        else
        {
            URI uri = new URI( spec );
            ClassLoader classloader = Resource.class.getClassLoader();
            Object[] args = new Object[0];
            Object instance = 
              Transit.getInstance().getRepository().getPlugin( classloader, uri, args );
            if( instance instanceof PluginFactory )
            {
                return (PluginFactory) instance;
            }
            else
            {
                final String error = 
                  "Artifact [" 
                  + spec
                  + "] assigned as the plugin factory established an instance of ["
                  + instance.getClass().getName()
                  + "] which is assignable to the " 
                  + PluginFactory.class.getName()
                  + " interface.";
                throw new IllegalArgumentException( error );
            }
        }    
    }
    
   /**
    * Get the project definition.
    * @param project the project
    * @return the build context
    */
    protected Context getContext( Project project )
    {
        Context context = (Context) project.getReference( "project.context" );
        if( null == context )
        {
            final String error = 
              "Missing project context reference.";
            throw new BuildException( error );
        }
        context.getPath( Scope.TEST ); // triggers path initialization
        return context;
    }
    
    private static final String TYPE = "plugin";

}
