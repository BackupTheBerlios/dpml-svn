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
import java.net.URISyntaxException;

import net.dpml.component.Directive;
import net.dpml.component.ActivationPolicy;

import net.dpml.library.info.Scope;
import net.dpml.library.Resource;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.builder.ComponentTypeDecoder;
import net.dpml.metro.data.DefaultComposition;

import net.dpml.lang.Classpath;
import net.dpml.lang.Part;
import net.dpml.lang.Info;

import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.tools.tasks.PartTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;

/**
 * Task that handles the construction of a serialized container part.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentBuilderTask extends PartTask implements PartReferenceBuilder
{
    private static final String NAMESPACE = "@COMPONENT-XSD-URI@";
    
    private static final ComponentTypeDecoder COMPONENT_TYPE_DECODER = 
      new ComponentTypeDecoder();
      
    private URI m_uri;
    private String m_key;
    private boolean m_embedded = false;
    private String m_name;
    private String m_classname;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
    private ActivationPolicy m_activation = ActivationPolicy.SYSTEM;
    private CategoriesDataType m_categories;
    private ContextDataType m_context;
    private PartsDataType m_parts;
    private File m_output;
    private Type m_type;
    private URI m_extends;
    private boolean m_alias = false;
    
   /**
    * Set the part key.
    * @param key the key
    */
    public void setKey( String key )
    {
        m_key = key;
    }

   /**
    * Set the alias production flag value.
    * @param alias true if alias production is requested
    */
    public void setAlias( boolean alias )
    {
        m_alias = alias;
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
    //public void setEmbedded( boolean flag )
    //{
    //    m_embedded = flag;
    //}

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
    public void setActivation( String policy )
    {
        m_activation = ActivationPolicy.parse( policy );
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
    * Create a new part datatype.
    * @return a new part datatype
    */
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
    
   /**
    * Build the plugin definition.
    * @param resource the project resource definition
    * @return the part definition
    */
    protected Part build( Resource resource )
    {
        try
        {
            Info info = getInfo( resource );
            Classpath classpath = getClasspath( resource );
            ClassLoader classloader = createClassLoader();
            ComponentDirective profile = buildComponentDirective( classloader );
            return new DefaultComposition( 
                new LoggingAdapter( "depot" ),
                info, classpath, null, profile );
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
    * Return the runtime classloader.
    * @return the classloader
    */
    protected ClassLoader createClassLoader()
    {
        Project project = getProject();
        Path path = getContext().getPath( Scope.RUNTIME );
        File classes = getContext().getTargetClassesMainDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader parentClassLoader = getClass().getClassLoader();
        return new AntClassLoader( parentClassLoader, project, path, true );
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
    * Build a part reference.
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
        return buildComponentDirective( classloader );
    }

    private ComponentDirective buildComponentDirective( ClassLoader classloader ) 
      throws IntrospectionException, IOException, ClassNotFoundException
    {
        String classname = getClassname();
        Type type = loadType( classloader, classname );
        String id = getName( type.getInfo().getName() );
        log( "creating [" + id + "] using [" + classname + "]" );
        
        LifestylePolicy lifestyle = getLifestylePolicy(); 
        CollectionPolicy collection = getCollectionPolicy( type );
        ActivationPolicy activation = getActivationPolicy();
        CategoriesDirective categories = getCategoriesDirective();
        ContextDirective context = getContextDirective( classloader, type );
        PartReference[] parts = getParts( classloader );
        URI base = getBaseURI();
        
        //
        // return the component profile
        //

        return new ComponentDirective( 
          id, activation, collection, lifestyle, classname, categories, 
          context, parts, base );
    }
    
    private URI getBaseURI()
    {
        return m_extends;
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
            Resource resource = getResource();
            Class c = classloader.loadClass( classname );
            return COMPONENT_TYPE_DECODER.loadType( c, resource );
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
    * a component are 'transient', 'thread' or 'singleton'.  If 'transient' is supplied
    * the assigned lefestyle policy is InfoDescriptor.TRANSIENT resulting in the 
    * creation of a new instance per request.  If 'thread' is declared the assigned
    * lifestyle policy shall be InfoDescriptor.THREAD in which case a supplied 
    * instance will be reused for all requests within the same thread of execution.
    * If the supplied policy is 'singleton' then the established instance will be 
    * shared across consumers referencing the component.
    *
    * @return the lifestyle policy
    */
    public LifestylePolicy getLifestylePolicy()
    {
        return m_lifestyle;
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
        return m_activation;
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
           // return m_profile.getContextDirective();
           return null;
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
            //return m_profile.getCategoriesDirective();
            return null;
        }
        else
        {
            return m_categories.getCategoriesDirective();
        }
    }

    private PartReference[] getParts( ClassLoader classloader ) 
      throws IntrospectionException, IOException
    {
        if( null != m_parts )
        {
            try
            {
                return m_parts.getParts( classloader, null );
            }
            catch( ClassNotFoundException cnfe )
            {
                final String error = 
                  "Unable to load a class referenced by a nested part within a component type.";
                throw new BuildException( error, cnfe );
            }
        }
        else
        {
            return new PartReference[0];
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

   /**
    * Constant controller uri.
    */
    public static final URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

   /**
    * Constant strategy builder uri.
    */
    public static final URI STRATEGY_BUILDER_URI = setupURI( "@STRATEGY-BUILDER-URI@" );

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
