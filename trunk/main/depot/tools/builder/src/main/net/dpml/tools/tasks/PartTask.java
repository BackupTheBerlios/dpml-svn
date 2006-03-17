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
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import net.dpml.lang.Classpath;
import net.dpml.lang.Category;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.lang.Logger;

import net.dpml.part.Part;
import net.dpml.part.Strategy;
import net.dpml.part.Info;
import net.dpml.part.PartBuilder;

import net.dpml.lang.Type;

import net.dpml.library.info.Scope;
import net.dpml.library.Resource;

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
public class PartTask extends GenericTask
{    
   /**
    * Constant artifact type for a plugin.
    */
    public static final String TYPE = "part";

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
    
    private boolean m_test = false;
    
    private File m_output;
    
    private Strategy m_strategy;
    
    public void setStrategy( Strategy strategy )
    {
        m_strategy = strategy;
    }
    
   /**
    * Set the test build policy.  The default is to include 
    * the project artifact in the classpath of a created part, however - in a 
    * test scenario we don't want to do this.  Setting test to true will result
    * in the association of a local file uri to the project resource.
    *
    * @param test true if this is a local test part
    */
    public void setTest( boolean test )
    {
        m_test = test;
    }

   /**
    * Override the default output destination.
    *
    * @param file the overriding destination
    */
    public void setDest( File file )
    {
        m_output = file;
    }

    public void execute()
    {
        Resource resource = getResource();
        Strategy strategy = getStrategy( resource );
        Part part = buildPart( resource, strategy );
        writePart( part );
    }
    
    public Part buildPart( Resource resource, Strategy strategy )
    {
        try
        {
            Info info = getInfo( resource );
            Classpath classpath = getClasspath( resource );
            return new Part( info, strategy, classpath );
        }
        catch( Exception e )
        {
            final String error = 
              "Part construction error.";
            throw new BuildException( error, e );
        }
    }
    
    public void writePart( Part part )
    {
        File file = getOutputFile();
        try
        {
            Object data = part.getStrategy().getDeploymentData();
            ClassLoader classloader = data.getClass().getClassLoader();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            file.createNewFile();
            final OutputStream output = new FileOutputStream( file );
            try
            {
                Thread.currentThread().setContextClassLoader( classloader );
                PartBuilder builder = new PartBuilder();
                builder.writePart( part, output, "" );
                checksum( file );
                asc( file );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( loader );
                try
                {
                    output.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Part externalization error.";
            throw new BuildException( error, e );
        }
    }
    
    private File getOutputFile()
    {
        if( null != m_output )
        {
            return m_output;
        }
        else
        {
            Context context = getContext();
            final String path = context.getLayoutPath( TYPE );
            final File deliverables = context.getTargetDeliverablesDirectory();
            final File parts = new File( deliverables, TYPE + "s" );
            final File file = new File( parts, path );
            parts.mkdirs();
            return file;
        }
    }
    
   /**
    * Build the plugin definition.
    * @exception exception if a build related error occurs
    */
    private Part build( Resource resource ) throws Exception
    {
        Info info = getInfo( resource );
        Strategy strategy = getStrategy( resource );
        Classpath classpath = getClasspath( resource );
        return new Part( info, strategy, classpath );
    }
    
    private Info getInfo( Resource resource )
    {
        String title = getTitle( resource );
        String description = getDescription( resource );
        return new Info( title, description );
    }

    private String getTitle( Resource resource )
    {
        return resource.getProperty( PLUGIN_TITLE_KEY );
    }

    private String getDescription( Resource resource )
    {
        return resource.getProperty( PLUGIN_DESCRIPTION_KEY );
    }
    
    protected Strategy getStrategy( Resource resource )
    {
        if( null == m_strategy )
        {
            Type type = resource.getType( TYPE );
            if( type instanceof Strategy )
            {
                return (Strategy) type;
            }
            else
            {
                Object data = type.getData();
                if( null != data )
                {
                    if( data instanceof Strategy )
                    {
                        return (Strategy) data;
                    }
                    else
                    {
                        final String error =
                          "Datatype associated as the 'part' datastructure is not an instance of "
                          + Strategy.class.getName() + "."
                          + "\nClass " + type.getClass().getName();
                        throw new BuildException( error, getLocation() );
                    }
                }
                else
                {
                    final String error = 
                      "No data associated with part declaration."
                      + "\nResource: " + resource
                      + "\nType: " + TYPE;
                    throw new BuildException( error, getLocation() );
                }
            }
        }
        else
        {
            return m_strategy;
        }
    }
    
    protected Classpath getClasspath( Resource resource ) throws IOException
    {
        URI[] sysUris = getURIs( resource, Category.SYSTEM );
        URI[] publicUris = getURIs( resource, Category.PUBLIC );
        URI[] protectedUris = getURIs( resource, Category.PROTECTED );
        URI[] privateUris = getURIs( resource, Category.PRIVATE, true );
        return new Classpath( sysUris, publicUris, protectedUris, privateUris );
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
                URI uri = toURI( resource );
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
    
    private URI toURI( Resource resource ) throws Exception
    {
        if( m_test && resource.equals( getResource() ) )
        {
            File local = getContext().getTargetDeliverable( "jar" );
            return local.toURI();
        }
        else
        {
            return resource.getArtifact( "jar" ).toURI();
        }
    }
    
    /*
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
    */
    
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
