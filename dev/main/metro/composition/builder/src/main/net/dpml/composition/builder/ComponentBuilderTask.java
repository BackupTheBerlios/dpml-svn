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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import net.dpml.part.part.Part;
import net.dpml.part.part.PartHolder;
import net.dpml.part.part.PartReference;

import net.dpml.composition.builder.datatypes.CategoriesDataType;
import net.dpml.composition.builder.datatypes.ConfigurationDataType;
import net.dpml.composition.builder.datatypes.ContextDataType;
import net.dpml.composition.builder.datatypes.ParametersDataType;
import net.dpml.composition.builder.datatypes.PartsDataType;
import net.dpml.composition.data.ClassLoaderDirective;
import net.dpml.composition.data.ClasspathDirective;
import net.dpml.composition.data.ComponentProfile;
import net.dpml.composition.data.ContextDirective;
import net.dpml.composition.data.DeploymentProfile;
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
public class ComponentBuilderTask extends ProjectTask implements PartReferenceBuilder
{
    private URI m_uri;
    private String m_key;

    private String m_name;
    private String m_classname;
    private String m_lifestyle;
    private String m_collection;
    private boolean m_activation = true;
    private CategoriesDataType m_categories;
    private ContextDataType m_context;
    private ParametersDataType m_parameters;
    private ConfigurationDataType m_configuration;
    private PartsDataType m_parts;

    private File m_output;
    private Type m_type;

   /**
    * Override the default output destination.
    *
    * @param file the overriding destination
    */
    public void setDest( File file )
    {
        m_output = file;
    }

    public void setKey( String key )
    {
        m_key = key;
    }

    public void setName( String name )
    {
        m_name = name;
    }

    public void setType( String classname )
    {
        m_classname = classname;
    }

    public void setLifestyle( String policy )
    {
        m_lifestyle = policy;
    }

    public void setCollection( String policy )
    {
        m_collection = policy;
    }

    public void setActivation( boolean policy )
    {
        m_activation = policy;
    }

    public CategoriesDataType createCategories()
    {
        if( m_categories == null )
        {
            m_categories = new CategoriesDataType();
            return m_categories;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate categories declaration.";
             throw new BuildException( error, getLocation() );
        }
    }

    public PartsDataType createParts()
    {
        if( m_parts == null )
        {
            m_parts = new PartsDataType( this );
            return m_parts;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate parts element.";
             throw new BuildException( error, getLocation() );
        }
    }

    public ContextDataType createContext()
    {
        if( null == m_context )
        {
             m_context = new ContextDataType();
             return m_context;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate context declaration.";
             throw new BuildException( error, getLocation() );
        }
    }

   /**
    * Add a parameters instance to the component.
    * @return the parameters datatype
    */
    public ParametersDataType createParameters()
    {
        if( null == m_parameters )
        {
            Project t_project = getProject();
            m_parameters = new ParametersDataType( t_project );
            return m_parameters;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate parameters declaration.";
             throw new BuildException( error, getLocation() );
        }
    }

   /**
    * Add a parameters instance to the component.
    * @return the parameters datatype
    */
    public ConfigurationDataType createConfiguration()
    {
        if( null == m_configuration )
        {
            Project t_project = getProject();
            m_configuration = new ConfigurationDataType( t_project );
            return m_configuration;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate configuration.";
             throw new BuildException( error, getLocation() );
        }
    }

    public void execute()
    {
        Project t_project = getProject();
        Path path = getDefinition().getPath( t_project, Policy.RUNTIME );
        File classes = getContext().getClassesDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader classloader = new AntClassLoader( t_project, path );

        File file = getOutputFile();
        File parent = file.getParentFile();
        if( !parent.exists() )
        {
            parent.mkdirs();
        }

        try
        {
            ClassLoaderDirective cld = constructClassLoaderDirective();
            ComponentProfile profile = buildComponentProfile( classloader, cld );

            // ?? ASSEMBLY ??

            URI uri = getDefinition().getArtifactURI( Part.ARTIFACT_TYPE );
            if( null == m_output )
            {
                log( "saving part to: " + uri );
            }
            else
            {
                log( "saving part to: " + m_output );
            }
            URI handler = getPartHandlerURI();
            byte[] bytes = SerializableObjectHelper.writeToByteArray( profile );
            PartHolder holder = new PartHolder( handler, bytes );
            SerializableObjectHelper.write( holder, file );
        }
        catch( ConstructionException e )
        {
            throw e;
        }
        catch( IntrospectionException e )
        {
            final String error = 
              "Introspection error. "
              + e.getMessage();
            throw new BuildException( error, e, getLocation() );
        }
        catch( IOException e )
        {
            final String error = 
              "Internal error while attempting to write component part to file ["
              + file 
              + "]";
            throw new BuildException( error, e, getLocation() );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to build the part.";
            throw new BuildException( error, e, getLocation() );
        }
    }

    private ClassLoaderDirective constructClassLoaderDirective()
    {
        ArrayList list = new ArrayList();
        URI[] uris = createURISequence( ResourceRef.API );
        if( uris.length > 0 )
        {
            list.add( new ClasspathDirective( "api", uris ) );
        }
        uris = createURISequence( ResourceRef.SPI );
        if( uris.length > 0 )
        {
            list.add( new ClasspathDirective( "spi", uris ) );
        }
        uris = createURISequence( ResourceRef.IMPL );
        if( uris.length > 0 )
        {
            list.add( new ClasspathDirective( "impl", uris ) );
        }
        ClasspathDirective[] cps = (ClasspathDirective[]) list.toArray( new ClasspathDirective[0] );
        return new ClassLoaderDirective( cps );
    }

    private URI[] createURISequence( int category )
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
                if( "jar".equals( resource.getInfo().getType() ) )
                {
                    URI uri = resource.getArtifactURI( "jar" );
                    list.add( uri );
                }
            }
        }
        if( def.getInfo().isa( "jar" ) )
        {
            URI local = def.getArtifactURI( "jar" );
            list.add( local );
        }
        return (URI[]) list.toArray( new URI[0] );
    }

    private File getOutputFile()
    {
        if( null != m_output )
        {
            return m_output;
        }
        else
        {
            return getDefaultOutputFile();
        }
    }

    private File getDefaultOutputFile()
    {
        File dir = getContext().getDeliverablesDirectory( Part.ARTIFACT_TYPE );
        String filename = getDefinition().getFilename( Part.ARTIFACT_TYPE );
        return new File( dir, filename );
    }


    //---------------------------------------------------------------------
    // Builder
    //---------------------------------------------------------------------

   /**
    * Return a uri identitifying the builder.
    *
    * @return the builder uri
    */
    public URI getBuilderURI()
    {
        return PART_BUILDER_URI;
    }

    //---------------------------------------------------------------------
    // PartBuilder
    //---------------------------------------------------------------------

   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return a strategy uri
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

    public Part buildPart( ClassLoader classloader )
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String classname = getClassname();
        Type type = loadType( classloader, classname );
        return buildComponentProfile( type, classloader );
    }

    //---------------------------------------------------------------------
    // PartReferenceBuilder
    //---------------------------------------------------------------------

   /**
    * Return the part key.
    *
    * @return the key
    */
    public String getKey()
    {
        if( null == m_key )
        {
            final String message = 
              "Missing key attribute for nested part reference. Using part name instead.";
            log( message, Project.MSG_VERBOSE );
            return m_name;
        }
        return m_key;
    }

    public PartReference buildPartReference( ClassLoader classloader, Type type )
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String key = getKey();
        Part part = buildComponentProfile( type, classloader );
        return new PartReference( key, part );
    }

    //---------------------------------------------------------------------
    // impl
    //---------------------------------------------------------------------

    private ComponentProfile buildComponentProfile( Type type, ClassLoader classloader ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        return buildComponentProfile( classloader, null );
    }

    private ComponentProfile buildComponentProfile( ClassLoader classloader, ClassLoaderDirective cld ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String id = getName();        
        log( "building component: " + id );
        String classname = getClassname();
        Type type = loadType( classloader, classname );
        String lifestyle = getLifestylePolicy( type ); 
        int collection = getCollectionPolicy( type );
        int activation = getActivationPolicy();
        CategoriesDirective categories = getCategoriesDirective();
        ContextDirective context = getContextDirective( classloader, type );
        PartReference[] parts = getParts( classloader, type );
        Parameters parameters = getParameters();
        Configuration configuration = getConfiguration();

        //
        // return the component profile
        //

        return new ComponentProfile( 
          id, activation, collection, lifestyle, classname, categories, context, 
          parts, parameters, configuration, cld );
    }

   /**
    * TODO: move this to a part controller
    */
    private Type loadType( ClassLoader classloader, String classname )
    {
        if( null == classloader )
        {
             throw new NullPointerException( "classloader" );
        }
        if( null == classname )
        {
             throw new NullPointerException( "classname" );
        }
        try
        {
            Class c = classloader.loadClass( classname );
            return Type.loadType( c );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error occured while attempting to load type ["
              + classname
              + "]";
            throw new BuildException( error, e, getLocation() );
        }
    }

    protected String getName()
    {
        if( null == m_name )
        {
            if( null != m_key )
            {
                return m_key;
            }
            else
            {
                final String error = 
                  "The component profile name attribute is not defined.";
                throw new BuildException( error, getLocation() );
            }
        }
        return m_name;
    }

    protected String getClassname()
    {
        if( null == m_classname )
        {
            final String error = 
              "Missing component 'type' attribute.";
            throw new BuildException( error, getLocation() );
        }
        return m_classname;
    }

   /**
    * Return the lifestyle policy declared relative to usage.
    * If undefined then default to the lifestyle declared by the component type.
    * Lifestyle policies that may be declared under the 'lifestyle' attribute of 
    * a component are 'request', 'thread' or 'shared'.  If 'request' is supplied
    * the assigned lefestyle policy is InfoDescriptor.TRANSIENT resulting in the 
    * creation of a new instance per request.  If 'thread' is declared the assigned
    * lifestyle policy shall be InfoDescriptor.THREAD in which case a supplied 
    * instance will be reused for all requests within the same thread of execution.
    * If the supplied policy is 'shared' then the established instance will be 
    * shared across consumers referencing the component.
    *
    * TODO - delcare in a component implementation class if the implementation
    * is thread-safe or not.  If not thread-safe then a request for a shared
    * usage policy should raise an error.
    *
    * @param type the component type
    * @return the lifestyle policy
    */
    public String getLifestylePolicy( Type type )
    {
        if( null == m_lifestyle )
        {
             return type.getInfo().getLifestyle();
        }
        else
        {
            String spec = m_lifestyle.toLowerCase();
            if( "request".equals( spec ) )
            {
                return InfoDescriptor.TRANSIENT;
            }
            else if( "thread".equals( spec ) )
            {
                return InfoDescriptor.THREAD;
            }
            else if( "shared".equals( spec ) )
            {
                return InfoDescriptor.SINGLETON;
            }
            else
            {
                final String error = 
                  "Lifestyle policy [" 
                  + spec 
                  + "] not regignized. Valid policies include 'request', 'thread' and 'shared'.";
                throw new BuildException( error, getLocation() );
            }
        }
    }

    public int getCollectionPolicy( Type type )
    {
        if( null == m_collection )
        {
             return type.getInfo().getCollectionPolicy();
        }
        else
        {
            String spec = m_collection.toLowerCase();
            if( "hard".equals( spec ) )
            {
                return InfoDescriptor.HARD_COLLECTION;
            }
            else if( "soft".equals( spec ) )
            {
                return InfoDescriptor.SOFT_COLLECTION;
            }
            else if( "weak".equals( spec ) )
            {
                return InfoDescriptor.WEAK_COLLECTION;
            }
            else
            {
                final String error = 
                  "Collection policy [" 
                  + spec 
                  + "] not recognized. Valid policies include 'hard', 'soft' and 'weak'.";
                throw new BuildException( error, getLocation() );
            }
        }
    }

    public int getActivationPolicy()
    {
        if( m_activation )
        {
            return DeploymentProfile.ENABLED;
        }
        else
        {
            return DeploymentProfile.DISABLED;
        }
    }

    private ContextDirective getContextDirective( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String classname = type.getInfo().getClassname();
        ContextDirective context = createContextDirective( classloader, type );

        //
        // validate that the context directives are declared
        // and if not - throw an exception
        //

        EntryDescriptor[] entries = type.getContext().getEntries();
        for( int i=0; i<entries.length; i++ )
        {
            EntryDescriptor entry = entries[i];
            String key = entry.getKey();

            PartReference reference = context.getPartReference( key );

            if( entry.isRequired() && ( null == reference ) )
            {
                final String error = 
                  "The component model ["
                  + getName() 
                  + "] referencing the component type ["
                  + classname 
                  + "] does not declare a context entry for the non-optional entry ["
                  + key 
                  + "].";
                throw new ConstructionException( error, getLocation() );
            }
        }

        //
        // we are ship-shape
        // 

        return context;
    }

    private ContextDirective createContextDirective( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        if( null == m_context )
        {
            return new ContextDirective();
        }
        else
        {
            return m_context.getContextDirective( classloader, type );
        }
    }

    private CategoriesDirective getCategoriesDirective()
    {
         if( null == m_categories )
         {
              return new CategoriesDirective();
         }
         else
         {
              return m_categories.getCategoriesDirective();
         }
    }

    private Parameters getParameters()
    {
         if( null == m_parameters )
         {
              return DefaultParameters.EMPTY_PARAMETERS;
         }
         else
         {
              return m_parameters.getParameters();
         }
    }

    private Configuration getConfiguration()
    {
         if( null == m_configuration )
         {
              return null;
         }
         else
         {
              return m_configuration.getConfiguration();
         }
    }

    private PartReference[] getParts( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        if( null == m_parts )
        {
            return new PartReference[0];
        }
        else
        {
            return m_parts.getParts( classloader, type );
        }
    }

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

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
