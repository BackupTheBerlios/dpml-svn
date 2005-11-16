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

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import net.dpml.metro.tools.datatypes.CategoriesDataType;
import net.dpml.metro.tools.datatypes.ConfigurationDataType;
import net.dpml.metro.tools.datatypes.ContextDataType;
import net.dpml.metro.tools.datatypes.ParametersDataType;
import net.dpml.metro.tools.datatypes.PartsDataType;

import net.dpml.metro.runtime.CompositionController;
import net.dpml.metro.runtime.CompositionContext;

import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.ClasspathDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.part.ControllerContext;
import net.dpml.part.Part;
import net.dpml.part.Component;
import net.dpml.part.PartHolder;

import net.dpml.tools.ant.Context;
import net.dpml.tools.tasks.GenericTask;
import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Resource;

import net.dpml.transit.tools.AntAdapter;
import net.dpml.transit.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.Plugin;
import net.dpml.transit.Category;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Task that handles the construction of a serialized container part.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class ClassLoaderBuilderTask extends GenericTask
{
    protected CompositionController getController()
    {
        try
        {
            Logger logger = new AntAdapter( this );
            return new CompositionController( logger );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while creating controller.";
            throw new BuildException( error, e );
        }
    }

    protected ClassLoader createClassLoader()
    {
        Project project = getProject();
        Path path = getContext().getPath( Scope.RUNTIME );
        File classes = getContext().getTargetClassesMainDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader parentClassLoader = ClassLoaderBuilderTask.class.getClassLoader();
        return new AntClassLoader( parentClassLoader, project, path, true );
    }

    protected ClassLoaderDirective constructClassLoaderDirective()
    {
        ClasspathDirective sys = createClasspathDirective( Category.SYSTEM );
        ClasspathDirective pub = createClasspathDirective( Category.PUBLIC );
        ClasspathDirective pro = createClasspathDirective( Category.PROTECTED );
        ClasspathDirective pri = createClasspathDirective( Category.PRIVATE );
        ClasspathDirective[] cps = new ClasspathDirective[]{ sys, pub, pro, pri };
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
        return resource.getArtifact( "jar" ).toURI();
    }
    
    protected File getPartOutputFile()
    {
        File deliverables = getContext().getTargetDeliverablesDirectory();
        File dir = new File( deliverables, Part.ARTIFACT_TYPE + "s" );
        String filename = getContext().getLayoutPath( Part.ARTIFACT_TYPE );
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

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

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
