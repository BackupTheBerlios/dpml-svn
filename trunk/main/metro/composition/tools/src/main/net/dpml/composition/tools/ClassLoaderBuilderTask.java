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

package net.dpml.composition.tools;

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

import net.dpml.composition.tools.datatypes.CategoriesDataType;
import net.dpml.composition.tools.datatypes.ConfigurationDataType;
import net.dpml.composition.tools.datatypes.ContextDataType;
import net.dpml.composition.tools.datatypes.ParametersDataType;
import net.dpml.composition.tools.datatypes.PartsDataType;
import net.dpml.composition.control.CompositionController;
import net.dpml.composition.control.CompositionContext;

import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ClasspathDirective;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ContextDirective;
import net.dpml.component.data.CategoriesDirective;
import net.dpml.component.info.EntryDescriptor;
import net.dpml.component.info.InfoDescriptor;
import net.dpml.component.info.Type;
import net.dpml.component.info.PartReference;
import net.dpml.component.control.ControllerContext;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.Container;
import net.dpml.component.runtime.Service;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.part.Part;
import net.dpml.part.PartHolder;
import net.dpml.part.PartContentHandlerFactory;

import net.dpml.tools.ant.Definition;
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
        Path path = getDefinition().getPath( project, Scope.BUILD );
        File classes = getDefinition().getTargetClassesMainDirectory();
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
            Resource[] resources = getDefinition().getClassPath( category );
            if( category.equals( category.PRIVATE ) && isaJar( getDefinition() ) )
            {
                Resource[] res = new Resource[ resources.length + 1 ];
                for( int i=0; i<resources.length; i++ )
                {
                    res[i] = resources[i];
                }
                Resource resource = getDefinition().toResource();
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
    
    private boolean isaJar( Definition definition )
    {
        String[] types = definition.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            if( "jar".equals( types[i] ) )
            {
                return true;
            }
        }
        return false;
    }
    
    private URI toURI( Resource resource ) throws Exception
    {
        String path = resource.getPath();
        String version = getResourceVersion( resource );
        if( null == version )
        {
            return new URI( "artifact:jar:" + path );
        }
        else
        {
            return new URI( "artifact:jar:" + path + "#" + version );
        }
    }
    
    private String getResourceVersion( Resource resource ) throws Exception
    {
        if( null != resource.getProject() )
        {
            return getDefinition().getVersion();
        }
        else
        {
            return resource.getVersion();
        }
    }

    protected File getPartOutputFile()
    {
        File deliverables = getDefinition().getTargetDeliverablesDirectory();
        File dir = new File( deliverables, Part.ARTIFACT_TYPE + "s" );
        String filename = getDefinition().getLayoutPath( Part.ARTIFACT_TYPE );
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
