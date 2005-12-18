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
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.DefaultPersistenceDelegate;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.part.Directive;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.EntryDescriptor;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;

import net.dpml.part.ActivationPolicy;
import net.dpml.part.Part;
import net.dpml.part.PartBuilder;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Task that handles the construction of a serialized container part.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentBuilderTask extends ClassLoaderBuilderTask implements PartReferenceBuilder
{
    private URI m_uri;
    private String m_key;
    private boolean m_embedded = false;
    private URI m_extends;
    private String m_name;
    private String m_classname;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
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

   /**
    * Set the part key.
    * @param key the key
    */
    public void setKey( String key )
    {
        m_key = key;
    }

   /**
    * Set the extends uri feature.
    * @param uri the uri from which the component extends
    */
    public void setExtends( URI uri )
    {
        m_extends = uri;
    }

   /**
    * Set the embedded component flag.
    * @param flag true if embedded
    */
    public void setEmbedded( boolean flag )
    {
        m_embedded = flag;
    }

   /**
    * Set the component name.
    * @param name the component name
    */
    public void setName( String name )
    {
        m_name = name;
    }
  
   /**
    * Set the component classname.
    * @param classname the component type classname
    */
    public void setType( String classname )
    {
        m_classname = classname;
    }

   /**
    * Set the lifestyle policy vlaue.
    * @param policy the lifestyle policy
    */
    public void setLifestyle( String policy )
    {
        m_lifestyle = LifestylePolicy.parse( policy );
    }

   /**
    * Set the gabage collection policy value.
    * @param policy the collection policy
    */
    public void setCollection( String policy )
    {
        m_collection = CollectionPolicy.parse( policy );
    }

   /**
    * Set the activation policy value.
    * @param policy the activation policy
    */
    public void setActivation( boolean policy )
    {
        m_activation = policy;
    }

   /**
    * Create a new categories data type.
    * @return the categories datatype
    */
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

   /**
    * Create a new context data type.
    * @return the context datatype
    */
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
    * Add a configuration instance to the component.
    * @return the configuration datatype
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

   /**
    * Execute the task.
    */
    public void execute()
    {
        ClassLoader classloader = createClassLoader();
        ClassLoaderDirective cld = constructClassLoaderDirective();
        File file = getOutputFile();
        File parent = file.getParentFile();
        if( !parent.exists() )
        {
            parent.mkdirs();
        }
        createComponent( classloader, cld, file );
    }

   /**
    * Local exception listener implementation.
    */
    private class LocalExceptionListener implements java.beans.ExceptionListener
    {
       /**
        * Catch an encoding exception.
        * @param e the exception
        */
        public void exceptionThrown( Exception e )
        {
            e.printStackTrace();
        }
    }

   /**
    * Create a component directive.
    * @param classloader the classloader
    * @param cld the classloader directive
    * @param file the output file
    * @return the component directive
    */
    public ComponentDirective createComponent( ClassLoader classloader, ClassLoaderDirective cld, File file )
    {
        try
        {
            final ClassLoader current = Thread.currentThread().getContextClassLoader();
            ComponentDirective profile = buildComponentDirective( classloader, cld );
            Thread.currentThread().setContextClassLoader( ComponentDirective.class.getClassLoader() );
            try
            {
                Part part = new Part( PART_HANDLER_URI, new Properties(), profile );
                PartBuilder.write( part, file );
            }
            catch( Exception e )
            {
                throw new BuildException( "Part encoding error.", e );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( current );
            }
            
            /*
            FileOutputStream output = new FileOutputStream( file );
            BufferedOutputStream buffer = new BufferedOutputStream( output );
            XMLEncoder encoder = new XMLEncoder( buffer );
            encoder.setPersistenceDelegate( URI.class, new URIPersistenceDelegate() );
            encoder.setExceptionListener( 
              new ExceptionListener()
              {
                public void exceptionThrown( Exception e )
                {
                    e.printStackTrace();
                    throw new BuildException( "Directive encoding failure.", e );
                }
              }
            );
            try
            {
                encoder.writeObject( profile );
            }
            catch( Exception e )
            {
                e.printStackTrace();
                throw new BuildException( "Directive encoding error.", e );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( current );
                encoder.close();
            }
            */
            
            /*
            URI uri = getResource().getArtifact( Part.ARTIFACT_TYPE ).toURI();
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
            */
            
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

   /**
    * Return the embedded reosurce path.
    * @param classname the component classname
    * @return the resource path
    */
    public String getEmbeddedResourcePath( String classname )
    {
        String path = classname.replace( '.', '/' );
        String filename = path + ".xprofile";
        return filename;
    }

   /**
    * Return the embedded output file.
    * @param filename the filename
    * @return the embedded output file
    */
    public File getEmbeddedOutputFile( String filename )
    {
        File classes = getContext().getTargetClassesMainDirectory();
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

   /**
    * Build the part.
    * @param classloader the classloader
    * @return the part
    * @exception IntrospectionException if an error occurs while introspecting the component class
    * @exception IOException if an I/O error occurs
    * @exception ClassNotFoundException if the component class cannot be found
    */
    public Directive buildDirective( ClassLoader classloader )
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
            final String error = 
              "Missing key attribute for nested part reference.";
            throw new BuildException( error );
        }
        else
        {
            return m_key;
        }
    }

   /**
    * Build a pert reference.
    * @param classloader the classloader
    * @param type the component type
    * @return the part reference
    * @exception IntrospectionException if an error occurs while introspecting the component class
    * @exception IOException if an I/O error occurs
    * @exception ClassNotFoundException if the component class cannot be found
    */
    public PartReference buildPartReference( ClassLoader classloader, Type type )
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String key = getKey();
        Directive directive = buildComponentDirective( type, classloader );
        return new PartReference( key, directive );
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
        String classname = getClassname();
        Type type = loadType( classloader, classname );
        String id = getName( type.getInfo().getName() );
        log( "creating [" + id + "] using [" + classname + "]" );

        if( null == m_extends )
        {
            m_profile = new ComponentDirective( id, classname );
        }
        else
        {
            try
            {
                Directive part = getController().loadDirective( m_extends );
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

        LifestylePolicy lifestyle = getLifestylePolicy( type ); 
        CollectionPolicy collection = getCollectionPolicy( type );
        ActivationPolicy activation = getActivationPolicy();
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
            Type type = Type.decode( getClass().getClassLoader(), c );
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

   /**
    * Return the component name.
    * @param typeName the component type name (used as a default)
    * @return the name
    */
    protected String getName( String typeName )
    {
        if( null == m_name )
        {
            if( null != m_key )
            {
                return m_key;
            }
            else
            {
                return typeName;
            }
        }
        else
        {
            return m_name;
        }
    }

   /**
    * Return the component classname.
    * @return the classname
    */
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
    * @param type the component type
    * @return the lifestyle policy
    */
    public LifestylePolicy getLifestylePolicy( Type type )
    {
        if( null == m_lifestyle )
        {
             return type.getInfo().getLifestyle();
        }
        else
        {
            return m_lifestyle;
        }
    }

   /**
    * Return the collection policy.
    * @param type the component type from which the default collection 
    *   policy can be resolved if needed
    * @return the collection policy
    */
    public CollectionPolicy getCollectionPolicy( Type type )
    {
        if( null == m_collection )
        {
             return type.getInfo().getCollectionPolicy();
        }
        else
        {
            return m_collection;
        }
    }

   /**
    * Return the activation policy.
    * @return the component activation policy
    */
    public ActivationPolicy getActivationPolicy()
    {
        if( m_activation )
        {
            return ActivationPolicy.STARTUP;
        }
        else
        {
            return m_profile.getActivationPolicy();
        }
    }

   /**
    * Return the context directive.
    * @param classloader the classloader to use
    * @param type the component type
    * @return the context directive
    * @exception IntrospectionException if a class introspection error occurs
    * @exception IOException if an I/O error occurs
    * @exception ClassNotFoundException if a component context class cannont be found
    */
    private ContextDirective getContextDirective( ClassLoader classloader, Type type ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String name = getName( type.getInfo().getName() );
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

        EntryDescriptor[] entries = type.getContextDescriptor().getEntryDescriptors();
        for( int i=0; i<entries.length; i++ )
        {
            EntryDescriptor entry = entries[i];
            String key = entry.getKey();

            Directive part = context.getPartDirective( key );
            if( entry.isRequired() && ( null == part ) )
            {
                final String error = 
                  "The component model ["
                  + name 
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

   /**
    * Utility class used to handle uri persistence.
    */
    public static class URIPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Return an expressio to create a uri.
        * @param old the old value
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            URI uri = (URI) old;
            String spec = uri.toString();
            Object[] args = new Object[]{spec};
            return new Expression( old, old.getClass(), "new", args );
        }
    }
}
