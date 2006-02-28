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
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import net.dpml.lang.Plugin;
import net.dpml.lang.PluginFactory;
import net.dpml.lang.Strategy;
import net.dpml.lang.Classpath;
import net.dpml.lang.Category;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Logger;
import net.dpml.transit.StandardHandler;
import net.dpml.transit.DefaultPluginFactory;
import net.dpml.transit.DefaultStrategy;
import net.dpml.transit.DefaultClasspath;

import net.dpml.library.info.Scope;
import net.dpml.library.model.Resource;
import net.dpml.library.model.Type;

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
    * Constant artifact type for a plugin.
    */
    public static final String TYPE = "plugin";

   /**
    * Property key used to identify the plugin title.
    */
    public static final String PLUGIN_TITLE_KEY = "project.plugin.title";
    
   /**
    * Property key used to identify the plugin description.
    */
    public static final String PLUGIN_DESCRIPTION_KEY = "project.plugin.description";
    
   /**
    * Property key used to identify a custom plugin handler classname.
    */
    public static final String PLUGIN_HANDLER_KEY = "project.plugin.handler";
    
   /**
    * Default runtime plugin handler classname.
    */
    public static final String STANDARD_PLUGIN_HANDLER = StandardHandler.class.getName();
    
    private static final PluginFactory FACTORY = new DefaultPluginFactory();

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
                Plugin plugin = build( resource );
                
                // extenalize the plugin to XML
                
                final String path = context.getLayoutPath( TYPE );
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
    
   /**
    * Build the plugin definition.
    * @exception exception if a build related error occurs
    */
    private Plugin build( Resource resource ) throws Exception
    {
        URI uri = getPluginURI( resource );
        String title = getTitle( resource );
        String description = getDescription( resource );
        Strategy strategy = getStrategy( resource );
        Classpath classpath = getClasspath( resource );
        Type type = resource.getType( TYPE );
        String spec = type.getProperty( "project.plugin.factory" );
        PluginFactory factory = getPluginFactory( spec );
        return factory.newPlugin( title, description, uri, strategy, classpath );
    }
    
    
    private URI getPluginURI( Resource resource ) throws Exception
    {
        Artifact artifact = resource.getArtifact( TYPE );
        return artifact.toURI();
    }

    private String getTitle( Resource resource )
    {
        Type type = resource.getType( TYPE );
        return type.getProperty( PLUGIN_TITLE_KEY );
    }

    private String getDescription( Resource resource )
    {
        Type type = resource.getType( TYPE );
        return type.getProperty( PLUGIN_DESCRIPTION_KEY );
    }
    
    protected Strategy getStrategy( Resource resource )
    {
        Type type = resource.getType( TYPE );
        Properties properties = getProperties( type );
        String handler = type.getProperty( 
            PLUGIN_HANDLER_KEY, 
            STANDARD_PLUGIN_HANDLER );
        return new DefaultStrategy( handler, properties );
    }
    
    private Properties getProperties( Type type )
    {
        Properties properties = new Properties();
        String[] keys = type.getLocalPropertyNames();
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            if( !PLUGIN_HANDLER_KEY.equals( key ) )
            {
                String value = type.getProperty( key );
                properties.setProperty( key, value );
            }
        }
        return properties;
    }
    
    protected Classpath getClasspath( Resource resource ) throws IOException
    {
        URI[] sysUris = getURIs( resource, Category.SYSTEM );
        URI[] publicUris = getURIs( resource, Category.PUBLIC );
        URI[] protectedUris = getURIs( resource, Category.PROTECTED );
        URI[] privateUris = getURIs( resource, Category.PRIVATE, true );
        return new DefaultClasspath( sysUris, publicUris, protectedUris, privateUris );
    }

    private URI[] getURIs( Resource resource, Category category ) throws IOException
    {
        return getURIs( resource, category, false );
    }
    
    private URI[] getURIs( Resource resource, Category category, boolean self ) throws IOException
    {
        Resource[] resources = resource.getClasspathProviders( category );
        ArrayList list = new ArrayList();
        for( int i=0; i<resources.length; i++ )
        {
            Resource r = resources[i];
            addURI( list, r );
        }
        if( self )
        {
            addURI( list, resource );
        }
        URI[] uris = (URI[]) list.toArray( new URI[0] );
        return uris;
    }
    
    private void addURI( List list, Resource resource )  throws IOException
    {
        if( resource.isa( "jar" ) )
        {
            try
            {
                Artifact artifact = resource.getArtifact( "jar" );
                URI uri = artifact.toURI();
                list.add( uri );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to resolve resource.";
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
    }
    
    private PluginFactory getPluginFactory( String spec ) throws Exception
    {
        if( null == spec )
        {
            return FACTORY;
        }
        else
        {
            URI uri = new URI( spec );
            ClassLoader classloader = Plugin.class.getClassLoader();
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
                  "Plugin factory artifact argument [" 
                  + spec
                  + "] established an instance of ["
                  + instance.getClass().getName()
                  + "] which is not assignable to " 
                  + PluginFactory.class.getName()
                  + ".";
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
    
}
