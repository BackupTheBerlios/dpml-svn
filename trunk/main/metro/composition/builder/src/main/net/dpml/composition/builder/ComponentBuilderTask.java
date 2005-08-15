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
import java.util.ArrayList;
import java.util.Properties;

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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Task that handles the construction of a serialized container part.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ComponentBuilderTask extends ClassLoaderBuilderTask implements PartReferenceBuilder
{
    private URI m_uri;
    private String m_key;
    private boolean m_embedded = false;
    private URI m_extends;

    private String m_name;
    private String m_classname;
    private String m_lifestyle;
    private String m_collection;
    private boolean m_activation = true;
    private CategoriesDataType m_categories;
    private ContextDataType m_context;
    private ParametersDataType m_parameters;
    private ConfigurationDataType m_configuration;

    private File m_output;
    private Type m_type;

    private ComponentDirective m_profile;

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

    public void setExtends( URI uri )
    {
        m_extends = uri;
    }

    public void setEmbedded( boolean flag )
    {
        m_embedded = flag;
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
            Project project = getProject();
            m_parameters = new ParametersDataType( project );
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
            Project project = getProject();
            m_configuration = new ConfigurationDataType( project );
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
        if( null == m_name )
        {
            final String error =
              "Missing name attribute.";
            throw new BuildException( error, getLocation() );
        }

        ClassLoader classloader = createClassLoader();
        ClassLoaderDirective cld = constructClassLoaderDirective();

        File file = getOutputFile();
        File parent = file.getParentFile();
        if( !parent.exists() )
        {
            parent.mkdirs();
        }


        ComponentDirective profile = createComponent( classloader, cld, file );

        File target = getContext().getTargetDirectory();
        File reports = new File( target, "reports/parts" );
        reports.mkdirs();
        if( m_embedded )
        {
            reports = new File( reports, "embedded" );
            reports.mkdirs();
        }

        File report = new File( reports, m_name + ".xml" );

        try
        {
            XStream XStream = new XStream( new DomDriver() );
            XStream.alias( "component", ComponentDirective.class );
            XStream.toXML( profile, new FileWriter( report ) );
            log( "Created report in " + report );
        }
        catch( Throwable e )
        {
            log( "XML reporting failed due to: " + e.toString() );
        }
    }

    public ComponentDirective createComponent( ClassLoader classloader, ClassLoaderDirective cld, File file )
    {
        try
        {
            ComponentDirective profile = buildComponentDirective( classloader, cld );
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
            return profile;
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
        if( m_embedded ) 
        {
            String classname = getClassname();
            String path = getEmbeddedResourcePath( classname );
            return getEmbeddedOutputFile( path );
        }
        else
        {
            return getPartOutputFile();
        }
    }

    public String getEmbeddedResourcePath( String classname )
    {
        String path = classname.replace( '.', '/' );
        String filename = path + ".xprofile";
        return filename;
    }

    public File getEmbeddedOutputFile( String filename )
    {
        File classes = getContext().getClassesDirectory();
        File destination = new File( classes, filename );
        return destination;
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
        return buildComponentDirective( type, classloader );
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
        Part part = buildComponentDirective( type, classloader );
        return new PartReference( key, part );
    }

    //---------------------------------------------------------------------
    // impl
    //---------------------------------------------------------------------

    private ComponentDirective buildComponentDirective( Type type, ClassLoader classloader ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        return buildComponentDirective( classloader, null );
    }

    private ComponentDirective buildComponentDirective( ClassLoader classloader, ClassLoaderDirective cld ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String id = getName();
        String classname = getClassname();
        log( "creating [" + id + "] using [" + classname + "]" );

        if( null == m_extends )
        {
            m_profile = new ComponentDirective( id, classname );
        }
        else
        {
            try
            {
                Part part = getController().loadPart( m_extends );
                if( part instanceof ComponentDirective )
                {
                    m_profile = (ComponentDirective) part;
                }
                else
                {
                    final String error = 
                      "Super-part is not an instance of "
                      + ComponentDirective.class.getName();
                    throw new BuildException( error );
                }
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unable to resolve component super-part ["
                  + m_extends
                  + "] due to: "
                  + e.getMessage();
                throw new BuildException( error, e, getLocation() );
            }
        }

        Type type = loadType( classloader, classname );
        String lifestyle = getLifestylePolicy( type ); 
        int collection = getCollectionPolicy( type );
        int activation = getActivationPolicy();
        CategoriesDirective categories = getCategoriesDirective();
        ContextDirective context = getContextDirective( classloader, type );
        Parameters parameters = getParameters();
        Configuration configuration = getConfiguration();

        //
        // return the component profile
        //

        return new ComponentDirective( 
          id, activation, collection, lifestyle, classname, categories, context, 
          parameters, configuration, cld );
    }

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
            Type type = Type.loadType( c );
            if( null != type )
            {
                return type;
            }
            else
            {
                final String error = 
                  "Component type [" + classname + "] is unknown or undefined.";
                throw new BuildException( error );
            }
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
            return Object.class.getName();
        }
        else
        {
            return m_classname;
        }
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
            return DeploymentDirective.ENABLED;
        }
        else
        {
            return m_profile.getActivationPolicy();
        }
    }

    private ContextDirective getContextDirective( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String classname = type.getInfo().getClassname();
        ContextDirective context = createContextDirective( classloader, type );
        if( null == context )
        {
            return m_profile.getContextDirective();
        }

        //
        // validate that the context directives are declared
        // and if not - throw an exception
        //

        EntryDescriptor[] entries = type.getContext().getEntries();
        for( int i=0; i<entries.length; i++ )
        {
            EntryDescriptor entry = entries[i];
            String key = entry.getKey();

            PartReference reference = context.getDirective( key );

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
            return null;
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
              return m_profile.getCategoriesDirective();
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
              return m_profile.getParameters();
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
              return m_profile.getConfiguration();
         }
         else
         {
              return m_configuration.getConfiguration();
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
