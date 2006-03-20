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
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.dpml.metro.builder.ComponentTypeEncoder;
import net.dpml.metro.info.CategoryDescriptor;
import net.dpml.metro.info.ContextDescriptor;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.ServiceDescriptor;
import net.dpml.metro.info.ThreadSafePolicy;

import net.dpml.state.State;
import net.dpml.state.impl.DefaultState;
import net.dpml.state.impl.StateDecoder;

import net.dpml.library.info.Scope;

import net.dpml.tools.tasks.GenericTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;


/**
 * The TypeTask creates a serialized descriptor of a component type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypeBuilderTask extends GenericTask implements TypeBuilder
{
    private static final StateDecoder STATE_DECODER = new StateDecoder();
    private static final ComponentTypeEncoder COMPONENT_TYPE_ENCODER = 
      new ComponentTypeEncoder();
      
    //---------------------------------------------------------------
    // state
    //---------------------------------------------------------------

    private String m_name;
    private String m_classname;
    private Class m_class;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
    private ThreadSafePolicy m_threadsafe = ThreadSafePolicy.UNKNOWN;
    private PartsDataType m_parts;
    private StateDataType m_state;
    private ServicesDataType m_services;
    private CategoriesDescriptorDataType m_categories;

    //---------------------------------------------------------------
    // setters
    //---------------------------------------------------------------

   /**
    * Set the name of the type.
    * @param name the component name
    */
    public void setName( String name )
    {
        m_name = name;
    }

   /**
    * Set the classname of the type.
    * @param classname the component type classname
    */
    public void setClass( String classname )
    {
        m_classname = classname;
    }

   /**
    * Set the threadsafe flag.
    * @param flag true if the component type is threadsafe
    */
    public void setThreadsafe( boolean flag )
    {
        if( flag )
        {
            m_threadsafe = ThreadSafePolicy.TRUE;
        }
        else
        {
            m_threadsafe = ThreadSafePolicy.FALSE;
        }
    }

   /**
    * Set the type collection policy.
    * @param value the collection policy value
    */
    public void setCollection( String value )
    {
        m_collection = CollectionPolicy.parse( value );
    }

   /**
    * Set the type lifestyle policy.
    * @param value the lifestyle policy value
    */
    public void setLifestyle( String value )
    {
        m_lifestyle = LifestylePolicy.parse( value );
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
    * Create a new services datatype.
    * @return a new services datatype
    */
    public ServicesDataType createServices()
    {
        if( m_services == null )
        {
            m_services = new ServicesDataType();
            return m_services;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate services element.";
             throw new BuildException( error, getLocation() );
        }
    }
    
   /**
    * Create a new services datatype.
    * @return a new services datatype
    */
    public CategoriesDescriptorDataType createCategories()
    {
        if( m_categories == null )
        {
            m_categories = new CategoriesDescriptorDataType();
            return m_categories;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate categories element.";
             throw new BuildException( error, getLocation() );
        }
    }
    
   /**
    * Create a state descriptor for the component.
    * @return a state graph descriptor
    */
    public StateDataType createState()
    {
        if( m_state == null )
        {
            m_state = new StateDataType( this, true );
            return m_state;
        }
        else
        {
             final String error =
              "Illegal attempt to create a duplicate state element.";
             throw new BuildException( error, getLocation() );
        }
    }
    
    //---------------------------------------------------------------
    // Builder
    //---------------------------------------------------------------

   /**
    * Return a urn identitifying the component type strategy that this 
    * builder is supporting.
    *
    * @return a uri identifiying the type strategy
    */
    public URI getTypeHandlerURI()
    {
        return TYPE_HANDLER_URI;
    }

   /**
    * Return a uri identitifying the builder.
    *
    * @return a uri identifiying the type builder
    */
    public URI getBuilderURI()
    {
        return COMPONENT_TYPE_DECODER_URI;
    }
    
    //---------------------------------------------------------------
    // TypeBuilder
    //---------------------------------------------------------------

   /**
    * Build the type.
    * @param classloader the classloader to use for type creation
    * @return the component type
    * @exception IntrospectionException if a class introspection error occurs
    * @exception IOException if an I/O error occurs
    */
    public Type buildType( ClassLoader classloader ) 
       throws IntrospectionException, IOException
    {
        Class subject = loadSubjectClass( classloader );
        return buildType( subject );
    }

   /**
    * Build the type.
    * @param subject the implementation class
    * @return the component type
    * @exception IntrospectionException if a class introspection error occurs
    * @exception IOException if an I/O error occurs
    */
    public Type buildType( Class subject )
       throws IntrospectionException, IOException
    {
        log( "creating [" + subject.getName() + "]" );

        InfoDescriptor info = createInfoDescriptor( subject );
        ServiceDescriptor[] services = createServiceDescriptors( subject );
        CategoryDescriptor[] categories = createCategoryDescriptors();
        ContextDescriptor context = createContextDescriptor( subject );
        PartReference[] parts = getPartReferences( subject.getClassLoader() );
        State graph = getStateGraph( subject );
        return new Type( info, categories, context, services, parts, graph );
    }

    private File getReportDestination( File dir, Type type )
    {
        final String classname = type.getInfo().getClassname();
        String path = classname.replace( '.', '/' );
        String filename = path + ".xml";
        return new File( dir, filename );
    }

    //---------------------------------------------------------------
    // Task
    //---------------------------------------------------------------

   /**
    * Execute the task.
    */
    public void execute()
    {
        Project proj = getProject();
        
        Path path = getContext().getPath( Scope.RUNTIME );
        File classes = getContext().getTargetClassesMainDirectory();
        path.createPathElement().setLocation( classes );
        ClassLoader classloader = new AntClassLoader( proj, path );
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try
        {
            final Type type = buildType( classloader );
            OutputStream output = getOutputStream( type );
            try
            {
                COMPONENT_TYPE_ENCODER.export( type, output );
            }
            finally
            {
                try
                {
                    output.close();
                }
                catch( IOException ioe )
                {
                    ioe.printStackTrace();
                }
            }
        }
        catch( IntrospectionException e )
        {
            final String error = e.getMessage();
            throw new BuildException( error, e, getLocation() );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to build types."
              + "\nCause: " + e.getClass().getName()
              + "\nMessage: " + e.getMessage();
            throw new BuildException( error, e, getLocation() );
        }
    }

    private OutputStream getOutputStream( Type type ) throws IOException
    {
        final String classname = type.getInfo().getClassname();
        final String resource = getEmbeddedResourcePath( classname );
        final File file = getEmbeddedOutputFile( resource );
        file.getParentFile().mkdirs();
        return new FileOutputStream( file );
    }

    private String getEmbeddedResourcePath( String classname )
    {
        String path = classname.replace( '.', '/' );
        String filename = path + ".type";
        return filename;
    }

    private File getEmbeddedOutputFile( String filename )
    {
        File classes = getContext().getTargetClassesMainDirectory();
        File destination = new File( classes, filename );
        return destination;
    }

    //---------------------------------------------------------------
    // internals
    //---------------------------------------------------------------

   /**
    * Return the type name.
    * @return the name
    */
    protected String getName()
    {
        if( null == m_name )
        {
            return "untitled";
        }
        return m_name;
    }

   /**
    * Return the type classname.
    * @return the classname
    */
    protected String getClassname()
    {
        if( null == m_classname )
        {
            final String error = 
              "Component type does not declare a classname.";
            throw new BuildException( error, getLocation() );
        }
        return m_classname;
    }
    
    private InfoDescriptor createInfoDescriptor( Class subject ) 
      throws IntrospectionException
    {
        String name = getName();
        String classname = subject.getName();
        ThreadSafePolicy threadsafe = getThreadSafeCapability( subject );
        Properties properties = getTypeProperties( subject );
        return new InfoDescriptor( 
          name, classname, null, m_lifestyle, m_collection, threadsafe, properties );
    }

    private ThreadSafePolicy getThreadSafeCapability( Class subject ) 
      throws IntrospectionException
    {
        return m_threadsafe;
    }

    private Properties getTypeProperties( Class subject ) 
      throws IntrospectionException
    {
        String path = subject.getClass().getName().replace( '.', '/' ) + ".properties";
        URL url = subject.getResource( path );
        if( null == url )
        {
            return null;
        }
        else
        {
            try
            {
                Properties properties = new Properties();
                InputStream input = url.openStream();
                try
                {
                    properties.load( input );
                    return properties;
                }
                finally
                {
                    input.close();
                }
            }
            catch( IOException e )
            {
                final String error = 
                  "Unable to load the property file for the path: "
                  + path;
                throw new BuildException( error, e );
            }
        }
    }
    
    private ServiceDescriptor[] createServiceDescriptors( Class subject )
    {
        if( null == m_services )
        {
            ArrayList list = new ArrayList();
            return createServiceDescriptors( subject, list );
        }
        else
        {
            return m_services.getServiceDescriptors();
        }
    }
    
    private CategoryDescriptor[] createCategoryDescriptors()
    {
        if( null == m_categories )
        {
           return new CategoryDescriptor[0];
        }
        else
        {
            return m_categories.getCategoryDescriptors();
        }
    }
    
    private ServiceDescriptor[] createServiceDescriptors( Class subject, List list )
    {
        Class[] interfaces = subject.getInterfaces();
        for( int i=0; i<interfaces.length; i++ )
        {
            Class service = interfaces[i];
            ServiceDescriptor descriptor = createServiceDescriptor( subject, service );
            if( null != descriptor )
            {
                if( !list.contains( descriptor ) )
                {
                    list.add( descriptor );
                }
            }
        }
        Class superclass = subject.getSuperclass();
        if( null != superclass )
        {
            return createServiceDescriptors( superclass, list );
        }
        else
        {
            return (ServiceDescriptor[]) list.toArray( new ServiceDescriptor[0] );
        }
    }

    private ServiceDescriptor createServiceDescriptor( Class type, Class subject )
    {
        String classname = subject.getName();
        Class parent = subject.getDeclaringClass();
        if( classname.startsWith( "java." ) )
        {
            return null; // ignore java.* interfaces 
        }
        else if( classname.startsWith( "net.dpml.activity." ) )
        {
            return null;
        }
        else if( type == parent )
        {
            return null; // ignore immediate inner interfaces
        }
        else
        {
            return new ServiceDescriptor( classname );
        }
    }

    private PartReference[] getPartReferences( ClassLoader classloader ) 
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

    private ContextDescriptor createContextDescriptor( Class subject ) 
      throws IntrospectionException
    {
        EntryDescriptor[] entries = createEntryDescriptors( subject );
        return new ContextDescriptor( entries );
    }

    private EntryDescriptor[] createEntryDescriptors( Class subject ) 
       throws IntrospectionException
    {
        String classname = subject.getName();
        Class[] classes = subject.getClasses();
        Class param = locateClass( "$Context", classes );
        if( null == param )
        {
            return new EntryDescriptor[0];
        }
        else
        {
            //
            // For each method in the Context inner-interface we construct a 
            // descriptor that establishes the part key, type, and required status.
            //

            Method[] methods = param.getMethods();
            ArrayList list = new ArrayList();
            for( int i=0; i<methods.length; i++ )
            {
                Method method = methods[i];
                String name = method.getName();
                if( name.startsWith( "get" ) )
                {
                    EntryDescriptor descriptor = createEntryDescriptor( method );
                    list.add( descriptor );
                }
            }
            return (EntryDescriptor[]) list.toArray( new EntryDescriptor[0] );
        }
    }

   /**
    * Creation of a new parameter descriptor using a supplied method.
    * The method is the method used by the component implementation to get the parameter 
    * instance. 
    */
    private EntryDescriptor createEntryDescriptor( Method method ) 
      throws IntrospectionException
    {
        validateMethodName( method );
        validateNoExceptions( method );

        String key = EntryDescriptor.getEntryKey( method );

        Class returnType = method.getReturnType();
        if( method.getParameterTypes().length == 0 )
        {
            //
            // required context entry
            //

            validateNonNullReturnType( method );
            //validateNonArrayReturnType( method, returnType );
            String type = returnType.getName();
            return new EntryDescriptor( key, type, EntryDescriptor.REQUIRED );
        }
        else if( method.getParameterTypes().length == 1 )
        {
            Class[] params = method.getParameterTypes();
            Class param = params[0];
            if( returnType.isAssignableFrom( param ) )
            {
                String type = param.getName();
                return new EntryDescriptor( key, type, EntryDescriptor.OPTIONAL );
            }
            else
            {
                final String error = 
                  "Context entry assessor declares an optional default parameter class ["
                  + param.getName()
                  + "] which is not assignable to the return type ["
                  + returnType.getName()
                  + "]";
                throw new IntrospectionException( error );
            }
        }
        else
        {
            final String error =
              "Unable to establish a required or optional context entry method pattern on ["
              + method.getName()
              + "]";
            throw new IntrospectionException( error );
        }
    }
    
    private State getStateGraph( Class subject )
    {
        if( null == m_state )
        {
            try
            {
                Class c = subject.getClassLoader().loadClass( "net.dpml.activity.Startable" );
                if( c.isAssignableFrom( subject ) )
                {
                    return loadStateFromResource( c );
                }
            }
            catch( ClassNotFoundException e )
            {
                boolean ignorable = true;
            }
            
            try
            {
                Class c = subject.getClassLoader().loadClass( "net.dpml.activity.Executable" );
                if( c.isAssignableFrom( subject ) )
                {
                    return loadStateFromResource( c );
                }
            }
            catch( ClassNotFoundException e )
            {
                boolean ignorable = true;
            }
            
            return new DefaultState( "" );
        }
        else
        {
            return m_state.getState();
        }
    }
    
    private State loadStateFromResource( Class subject )
    {
        String resource = subject.getName().replace( '.', '/' ) + ".xgraph";
        try
        {
            URL url = subject.getClassLoader().getResource( resource );
            if( null == url )
            {
                return null;
            }
            else
            {
                URI uri = new URI( url.toString() );
                return STATE_DECODER.loadState( uri );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to load component state graph resource [" 
              + resource 
              + "].";
            throw new BuildException( error, e );
        }
    }

    private String formatKey( String key, int offset )
    {
        String k = key.substring( offset );
        return formatKey( k );
    }

    private String formatKey( String key )
    {
        if( key.length() < 1 ) 
        {
            throw new IllegalArgumentException( "key" );
        }
        String first = key.substring( 0, 1 ).toLowerCase();
        String remainder = key.substring( 1 );
        return first + remainder;
    }

    private Class locateClass( String postfix, Class[] classes )
    {
        for( int i=0; i<classes.length; i++ )
        {
            Class inner = classes[i];
            String name = inner.getName();
            if( name.endsWith( postfix ) )
            {
                return inner;
            }
        }
        return null;
    }

    private void validateMapReturnType( Method method )
       throws IntrospectionException
    {
        Class returnType = method.getReturnType();
        if( Map.class != returnType )
        {
            String name = method.getName();
            final String error = 
              "The method ["
              + name
              + "] does not declare java.util.Map as a return type.";
            throw new IntrospectionException( error );
        }
    }
    
    private void validateEntryReturnType( Method method )
       throws IntrospectionException
    {
        Class returnType = method.getReturnType();
        if( Map.Entry.class != returnType )
        {
            String name = method.getName();
            final String error = 
              "The method ["
              + name
              + "] does not declare java.util.Map.Entry as a return type.";
            throw new IntrospectionException( error );
        }
    }
    
    private void validateReturnTypeIsAssignable( Method method, Class type ) 
      throws IntrospectionException
    {
        Class c = method.getReturnType();
        if( !type.isAssignableFrom( c ) )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares a return type ["
              + c.getName()
              + "] that is not assignable from the class ["
              + type.getName()
              + "].";
            throw new IntrospectionException( error );
        }
    }

    private void validateNonNullParameter( Method method, Class type ) 
       throws IntrospectionException
    {
        if( Void.TYPE.equals( type ) ) 
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares a null parameter.";
            throw new IntrospectionException( error );
        }
    }


    private void validateNonNullReturnType( Method method ) 
       throws IntrospectionException
    {
        Class returnType = method.getReturnType();
        if( Void.TYPE.equals( returnType ) ) 
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] does not declare a return type.";
            throw new IntrospectionException( error );
        }
    }

    private void validateNullReturnType( Method method, Class returnType ) 
       throws IntrospectionException
    {
        if( !Void.TYPE.equals( returnType ) ) 
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] does not declare a null return type.";
            throw new IntrospectionException( error );
        }
    }

    private void validateNonArrayReturnType( Method method, Class returnType )
        throws IntrospectionException
    {
        if( null != returnType.getComponentType() )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares an array return type.";
            throw new IntrospectionException( error );
        }
    }

    private void validateNonArrayType( Method method, Class type )
       throws IntrospectionException
    {
        if( null != type.getComponentType() )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares an array type.";
            throw new IntrospectionException( error );
        }
    }

    private void validateInterfaceReturnType( Method method, Class returnType )
      throws IntrospectionException
    {
        if( !returnType.isInterface() )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares a return type ["
              + returnType.getName()
              + "] that is not an interface.";
            throw new IntrospectionException( error );
        }
    }

    private void validateMethodName( Method method ) 
      throws IntrospectionException
    {
        if( !method.getName().startsWith( "get" ) )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] does not start with 'get'.";
            throw new IntrospectionException( error );
        }
    }

    private void validateNoExceptions( Method method ) 
      throws IntrospectionException
    {
        Class[] exceptionTypes = method.getExceptionTypes();
        if( exceptionTypes.length > 0 )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares one or more exceptions.";
            throw new IntrospectionException( error );
        }
    }

    private void validateNoParameters( Method method ) 
      throws IntrospectionException
    {
        Class[] parameterTypes = method.getParameterTypes();
        if( parameterTypes.length > 0 )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares one or more parameters.";
            throw new IntrospectionException( error );
        }
    }

    private void validateAtMostOneParameter( Method method ) 
      throws IntrospectionException
    {
        Class[] parameterTypes = method.getParameterTypes();
        if( parameterTypes.length > 1 )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares more than one parameters.";
            throw new IntrospectionException( error );
        }
    }

    private Class validateSingleParameter( Method method ) 
      throws IntrospectionException
    {
        Class[] parameterTypes = method.getParameterTypes();
        if( parameterTypes.length != 1 )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] does not declare a single parameter argument type.";
            throw new IntrospectionException( error );
        }
        return parameterTypes[0];
    }

    private void validateNonArrayParameter( Method method, Class type ) 
      throws IntrospectionException
    {
        if( null != type.getComponentType() )
        {
            final String error = 
              "Method ["
              + method.getName()
              + "] declares an array parameter type.";
            throw new IntrospectionException( error );
        }
    }

    private void validateSelectPattern( Class subject, Method method ) 
      throws IntrospectionException
    {
        Class[] parameterTypes = method.getParameterTypes();
        int n = parameterTypes.length;
        if( n == 1 )
        {
            Class b = parameterTypes[0];
            if( !Boolean.TYPE.isAssignableFrom( b ) )
            {
                String name = method.getName();
                final String error = 
                  "Part accessor ["
                  + subject.getName() + "#" + name
                  + "] is declaring an illegal non-boolean parameter.";
                throw new IntrospectionException( error );
            }
        }
    }

    private Class loadSubjectClass( ClassLoader classloader )
    {
        if( null == m_classname )
        {
            final String error = 
              "Missing component descriptor class attribute.";
            throw new IllegalStateException( error );
        }
        try
        {
            return classloader.loadClass( m_classname );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              "Cannot build a component type because the class ["
              + m_classname 
              + "] is not present in the project path.";
            throw new BuildException( error );
        }
    }

    private static final URI TYPE_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static final URI COMPONENT_TYPE_DECODER_URI = setupURI( "@PART-BUILDER-URI@" );

   /**
    * Internal utility to create a static uri.
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
