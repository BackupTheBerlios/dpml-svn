/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

//import net.dpml.metro.runtime.CompositionController;

import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.ClasspathDirective;

import net.dpml.tools.tasks.GenericTask;

import net.dpml.library.info.Scope;
import net.dpml.library.model.Resource;

import net.dpml.part.local.Controller;

import net.dpml.transit.tools.AntAdapter;
import net.dpml.transit.Logger;
import net.dpml.transit.Category;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Task that handles the construction of a serialized container part.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ClassLoaderBuilderTask extends GenericTask
{
    private boolean m_test = false;
    
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
    * Get the composition controller.
    * @return the controller
    */
    protected Controller getController()
    {
        try
        {
            return Controller.STANDARD;
            //Logger logger = new AntAdapter( this );
            //return new CompositionController( logger );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while creating controller.";
            throw new BuildException( error, e );
        }
    }

   /**
    * Return the runtime classloader.
    * @return the classloader
    */
    protected ClassLoader createClassLoader()
    {
        Project project = getProject();
        Path path = getContext().getPath( Scope.RUNTIME );
        File classes = getContext().getTargetClassesMainDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader parentClassLoader = ClassLoaderBuilderTask.class.getClassLoader();
        return new AntClassLoader( parentClassLoader, project, path, true );
    }

   /**
    * Create the classloader directive.
    * @return the classloader directive
    */
    protected ClassLoaderDirective constructClassLoaderDirective()
    {
        ClasspathDirective sys = createClasspathDirective( Category.SYSTEM );
        ClasspathDirective pub = createClasspathDirective( Category.PUBLIC );
        ClasspathDirective pro = createClasspathDirective( Category.PROTECTED );
        ClasspathDirective pri = createClasspathDirective( Category.PRIVATE );
        ClasspathDirective[] cps = new ClasspathDirective[]{sys, pub, pro, pri};
        return new ClassLoaderDirective( cps );
    }

    private ClasspathDirective createClasspathDirective( Category category )
    {
        try
        {
            Resource resource = getResource();
            Resource[] resources = resource.getClasspathProviders( category );
            if( category.equals( category.PRIVATE ) && resource.isa( "jar" ) )
            {
                Resource[] res = new Resource[ resources.length + 1 ];
                for( int i=0; i<resources.length; i++ )
                {
                    res[i] = resources[i];
                }
                res[ resources.length ] = resource;
                resources = res;
            }
            URI[] uris = new URI[ resources.length ];
            for( int i=0; i<uris.length; i++ )
            {
                uris[i] = toURI( resources[i] );
            }
            return new ClasspathDirective( category, uris );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error occured while building a classpath directive.";
            throw new RuntimeException( error, e  );
        }
    }
        
    private URI toURI( Resource resource ) throws Exception
    {
        if( m_test && resource.equals( getResource() ) )
        {
            File deliverable = getContext().getTargetDeliverable( "jar" );
            return deliverable.toURI();
        }
        else
        {
            return resource.getArtifact( "jar" ).toURI();
        }
    }
    
   /**
    * Create and return the part output file. 
    * @return the part output file
    */
    protected File getPartOutputFile()
    {
        File deliverables = getContext().getTargetDeliverablesDirectory();
        String type = "part"; // Part.ARTIFACT_TYPE
        String types = type + "s";
        File dir = new File( deliverables, types );
        String filename = getContext().getLayoutPath( type );
        return new File( dir, filename );
    }

   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return a strategy uri
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

   /**
    * Constant controller uri.
    */
    public static final URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

   /**
    * Constant builder uri.
    */
    public static final URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

   /**
    * Utility function to create a static uri.
    * @param spec the uri spec
    * @return the uri
    */
    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}
