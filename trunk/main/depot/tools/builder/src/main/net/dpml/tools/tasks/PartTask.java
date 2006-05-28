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
import java.util.ArrayList;
import java.util.List;

import net.dpml.lang.Classpath;
import net.dpml.lang.Category;
import net.dpml.lang.Info;
import net.dpml.lang.Part;
import net.dpml.lang.PartDecoder;

import net.dpml.library.Type;
import net.dpml.library.Resource;
import net.dpml.library.info.Scope;

import net.dpml.transit.Artifact;

import net.dpml.tools.Context;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import org.w3c.dom.Element;

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
    public static final String PLUGIN_TITLE_KEY = "project.title";
    
   /**
    * Property key used to identify the plugin description.
    */
    public static final String PLUGIN_DESCRIPTION_KEY = "project.description";
    
   /**
    * Property key used to identify a custom plugin handler classname.
    */
    public static final String PLUGIN_HANDLER_KEY = "project.plugin.handler";
    
    private boolean m_test = false;
    
    private File m_output;
    
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

   /**
    * Task execution.
    */
    public void execute()
    {
        Resource resource = getResource();
        Part part = build( resource );
        writePart( part );
    }
    
   /**
    * Externalize the part as a deliverable.
    * @param part the part to be externalized as XML
    */
    public void writePart( Part part )
    {
        File file = getOutputFile();
        if( file.exists() )
        {
            try
            {
                Part existing = Part.load( file.toURI(), false );
                if( part.equals( existing ) )
                {
                    return;
                }
            }
            catch( Exception e )
            {
                // continue
            }
        }
        
        log( "Building part: " + file );
        try
        {
            file.createNewFile();
            final OutputStream output = new FileOutputStream( file );
            try
            {
                part.encode( output );
                checksum( file );
                asc( file );
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
    * Build the part definition.
    * @param resource the resource
    * @return the part
    */
    protected Part build( Resource resource )
    {
        try
        {
            Info info = getInfo( resource );
            Classpath classpath = getClasspath( resource );
            Type type = resource.getType( TYPE );
            
            Element element = type.getElement();
            PartDecoder decoder = PartDecoder.getInstance();
            return decoder.build( info, classpath, element );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to build an external part defintion."
              + "\nResource: " + resource;
            throw new BuildException( error, e, getLocation() );
        }
    }
    
   /**
    * Construct the info object based on properties declared by the 
    * supplied resource.
    * @param resource the resource
    * @return the info descriptor
    */
    protected Info getInfo( Resource resource )
    {
        Artifact artifact = resource.getArtifact( TYPE );
        URI uri = artifact.toURI();
        String title = resource.getInfo().getTitle();
        String description = resource.getInfo().getDescription();
        return new Info( uri, title, description );
    }

    private String getTitle( Resource resource )
    {
        return resource.getProperty( PLUGIN_TITLE_KEY );
    }

    private String getDescription( Resource resource )
    {
        return resource.getProperty( PLUGIN_DESCRIPTION_KEY );
    }
    
   /**
    * Construct the classpath for the supplied resource.
    * @param resource the resource
    * @return the classpath 
    * @exception IOException is an IO error occurs
    */
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
