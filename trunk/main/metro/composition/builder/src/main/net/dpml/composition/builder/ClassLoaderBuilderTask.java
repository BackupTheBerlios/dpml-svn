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

package net.dpml.composition.builder;

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

import net.dpml.composition.builder.datatypes.CategoriesDataType;
import net.dpml.composition.builder.datatypes.ConfigurationDataType;
import net.dpml.composition.builder.datatypes.ContextDataType;
import net.dpml.composition.builder.datatypes.ParametersDataType;
import net.dpml.composition.builder.datatypes.PartsDataType;
import net.dpml.composition.control.CompositionController;
import net.dpml.composition.control.CompositionControllerContext;
import net.dpml.composition.data.ClassLoaderDirective;
import net.dpml.composition.data.ClasspathDirective;
import net.dpml.composition.data.ComponentDirective;
import net.dpml.composition.data.ContextDirective;
import net.dpml.composition.data.DeploymentDirective;
import net.dpml.composition.data.CategoriesDirective;
import net.dpml.composition.info.EntryDescriptor;
import net.dpml.composition.info.InfoDescriptor;
import net.dpml.composition.info.Type;
import net.dpml.composition.info.TypeHolder;

import net.dpml.configuration.Configuration;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Policy;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.tasks.ProjectTask;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.part.Part;
import net.dpml.part.PartHolder;
import net.dpml.part.PartReference;
import net.dpml.part.PartContentHandlerFactory;
import net.dpml.part.control.ControllerContext;
import net.dpml.part.component.Component;
import net.dpml.part.component.Container;
import net.dpml.part.component.Service;

import net.dpml.transit.tools.AntAdapter;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;

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
public abstract class ClassLoaderBuilderTask extends ProjectTask
{
    protected CompositionController getController()
    {
        try
        {
            Logger logger = new AntAdapter( this );
            ContentModel model = PartContentHandlerFactory.newContentModel( logger, null );
            return new CompositionController( logger, model );
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
        Path path = getDefinition().getPath( project, Policy.RUNTIME );
        File classes = getContext().getClassesDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader parentClassLoader = ClassLoaderBuilderTask.class.getClassLoader();
        return new AntClassLoader( parentClassLoader, project, path, true );
    }

    protected ClassLoaderDirective constructClassLoaderDirective()
    {
        ArrayList list = new ArrayList();
        ArrayList visited = new ArrayList();
        URI[] uris = createURISequence( ResourceRef.API, visited );
        if( uris.length > 0 )
        {
            list.add( new ClasspathDirective( "api", uris ) );
        }
        uris = createURISequence( ResourceRef.SPI, visited );
        if( uris.length > 0 )
        {
            list.add( new ClasspathDirective( "spi", uris ) );
        }
        uris = createURISequence( ResourceRef.IMPL, visited, true );
        if( uris.length > 0 )
        {
            list.add( new ClasspathDirective( "impl", uris ) );
        }
        ClasspathDirective[] cps = (ClasspathDirective[]) list.toArray( new ClasspathDirective[0] );
        return new ClassLoaderDirective( cps );
    }

    private URI[] createURISequence( int category, List visited )
    {
        return createURISequence( category, visited, false );
    }

    private URI[] createURISequence( int category, List visited, boolean flag )
    {
        Definition def = getDefinition();
        ArrayList list = new ArrayList();
        final ResourceRef[] resources =
          def.getResourceRefs( getProject(), Policy.RUNTIME, category, true );
        for( int i=0; i<resources.length; i++ )
        {
            final ResourceRef ref = resources[i];
            final Policy policy = ref.getPolicy();
            if( policy.isRuntimeEnabled() )
            {
                final Resource resource = getIndex().getResource( ref );
                if( resource.getInfo().isa( "jar" ) )
                {
                    URI uri = resource.getArtifactURI( "jar" );
                    if( false == visited.contains( uri ) )
                    {
                        list.add( uri );
                        visited.add( uri );
                    }
                }
            }
        }
        if( flag )
        {
            if( def.getInfo().isa( "jar" ) )
            {
                URI local = def.getArtifactURI( "jar" );
                list.add( local );
            }
        }
        return (URI[]) list.toArray( new URI[0] );
    }

    protected File getPartOutputFile()
    {
        File dir = getContext().getDeliverablesDirectory( Part.ARTIFACT_TYPE );
        String filename = getDefinition().getFilename( Part.ARTIFACT_TYPE );
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
