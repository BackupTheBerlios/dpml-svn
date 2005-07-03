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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import net.dpml.activity.Startable;
import net.dpml.activity.Executable;

import net.dpml.composition.info.CategoryDescriptor;
import net.dpml.composition.info.ContextDescriptor;
import net.dpml.composition.info.DependencyDescriptor;
import net.dpml.composition.info.EntryDescriptor;
import net.dpml.composition.info.InfoDescriptor;
import net.dpml.composition.info.PartDescriptor;
import net.dpml.composition.info.ServiceDescriptor;
import net.dpml.composition.info.Type;
import net.dpml.composition.info.PartDescriptor.Operation;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.DefaultConfigurationBuilder;

import net.dpml.magic.tasks.ProjectTask;

import net.dpml.part.state.State;

import org.apache.tools.ant.BuildException;

/**
 * The TypeTask creates a serialized descriptor of a component type.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class TypeBuilderTask extends ProjectTask implements TypeBuilder
{
    //---------------------------------------------------------------
    // state
    //---------------------------------------------------------------

    private String m_name;
    private String m_classname;
    private Class m_class;
    private String m_lifestyle;
    private String m_collection;
    private boolean m_threadsafe = false;

    //---------------------------------------------------------------
    // setters
    //---------------------------------------------------------------

    public void setName( String name )
    {
        m_name = name;
    }

    public void setClass( String classname )
    {
        m_classname = classname;
    }

    public void setThreadsafe( boolean value )
    {
        m_threadsafe = value;
    }

    public void setCollection( String value )
    {
        if( InfoDescriptor.HARD.equalsIgnoreCase( value ) )
        {
            m_collection = InfoDescriptor.HARD;
        }
        else if( InfoDescriptor.WEAK.equalsIgnoreCase( value ) )
        {
            m_collection = InfoDescriptor.WEAK;
        }
        else if( InfoDescriptor.SOFT.equalsIgnoreCase( value ) )
        {
            m_collection = InfoDescriptor.SOFT;
        }
        else
        {
            final String error = 
              "Unsupported collection policy '" + value + "'.";
            throw new BuildException( error );
        }
    }

    public void setLifestyle( String value )
    {
        if( "request".equalsIgnoreCase( value ) || "transient".equalsIgnoreCase( value ) )
        {
            m_lifestyle = InfoDescriptor.TRANSIENT;
        }
        else if( "singleton".equalsIgnoreCase( value ) || "shared".equalsIgnoreCase( value ) )
        {
            m_lifestyle = InfoDescriptor.SINGLETON;
        }
        else
        {
            final String error = 
              "Unsupported lifestyle policy '" + value + "'.";
            throw new BuildException( error );
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
        return TYPE_BUILDER_URI;
    }

    //---------------------------------------------------------------
    // TypeBuilder
    //---------------------------------------------------------------

    public Type buildType( ClassLoader classloader ) 
       throws IntrospectionException, IOException
    {
        Class subject = loadSubjectClass( classloader );
        return buildType( subject );
    }

    public Type buildType( Class subject )
       throws IntrospectionException, IOException
    {
        log( "creating: " + subject.getName() );

        InfoDescriptor info = createInfoDescriptor( subject );
        ServiceDescriptor[] services = createServiceDescriptors( subject );
        CategoryDescriptor[] categories = new CategoryDescriptor[0];
        ContextDescriptor context = createContextDescriptor( subject );
        PartDescriptor[] parts = createPartDescriptors( subject );
        Configuration config = createDefaultConfiguration( subject );
        State graph = resolveStateGraph( subject );

        // TODO: add state model

        return new Type( graph, info, categories, context, services, config, parts );
    }

    //---------------------------------------------------------------
    // Task
    //---------------------------------------------------------------

    public void execute()
    {
        final String error = 
          "The type builder task does not support standalone operation. "
          + "Please use the 'types' task instead.";
        throw new UnsupportedOperationException( error );
    }

    //---------------------------------------------------------------
    // internals
    //---------------------------------------------------------------

    protected String getName()
    {
        if( null == m_name )
        {
            return "untitled";
        }
        return m_name;
    }

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

    private InfoDescriptor createInfoDescriptor( Class subject ) throws IntrospectionException
    {
        String name = getName();
        String classname = subject.getName();
        boolean threadsafe = getThreadSafeCapability( subject );
        String lifestyle = getLifestylePreference( subject, threadsafe );
        String collection = getCollectionPolicyPreference( subject );
        String schema = getConfigurationSchema( subject );
        Properties properties = getTypeProperties( subject );
        return new InfoDescriptor( 
          name, classname, null, lifestyle, collection, schema, threadsafe, true, properties );
    }

    private boolean getThreadSafeCapability( Class subject ) throws IntrospectionException
    {
        return m_threadsafe;
    }

    private String getLifestylePreference( Class subject, boolean threadsafe ) throws IntrospectionException
    {
        if( null != m_lifestyle )
        {
            return m_lifestyle;
        }
        else
        {
            if( m_threadsafe )
            {
                return InfoDescriptor.SINGLETON;
            }
            else
            {
                return InfoDescriptor.TRANSIENT;
            }
        }
    }

    private String getCollectionPolicyPreference( Class subject ) throws IntrospectionException
    {
        return m_collection;
    }

    private String getConfigurationSchema( Class subject ) throws IntrospectionException
    {
        // TODO: change this to a namespaced attribute on the configuration
        try
        {
            Field field = subject.getDeclaredField( "TYPE_CONFIGURATION_SCHEMA" );
            if( Modifier.isStatic( field.getModifiers() ) )
            {
                if( String.class.isAssignableFrom( field.getType() ) )
                {
                    return (String) field.get( null );
                }
                else
                {
                    final String error =
                      "The component type [" 
                      + subject.getName()
                      + "] declares an invalid static field TYPE_CONFIGURATION_SCHEMA declaration. "
                      + "The declared type is not assignable to a string.";
                    throw new IntrospectionException( error );
                }
            }
            else
            {
                return null;
            }
        }
        catch( NoSuchFieldException e )
        {
            return null;
        }
        catch( IllegalArgumentException e )
        {
            final String error =
              "The component type [" 
              + subject.getName()
              + "] declares an invalid static field TYPE_CONFIGURATION_SCHEMA declaration. "
              + "Could not convert the value to an string.";
            throw new IntrospectionException( error );
        }
        catch( IntrospectionException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error =
              "An unexpected error occured while resolving the static "
              + "TYPE_CONFIGURATION_SCHEMA field on the type [" 
              + subject.getName()
              + "].";
            throw new IntrospectionException( error );
        }
    }

    private Configuration createDefaultConfiguration( Class subject )
    {
        final String classname = subject.getName();
        final ClassLoader classloader = subject.getClassLoader();
        final String xdefaults = classname.replace( '.', '/' ) + ".xconfig";
        final InputStream input = classloader.getResourceAsStream( xdefaults );
        if( null == input )
        {
            return null;
        }
        else
        {
            try
            {
                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                return builder.build( input );
            }
            catch( Exception e )
            {
                final String error =
                  "An unexpected error occured while resolving the static configuration [" 
                  + xdefaults 
                  + "].";
                throw new BuildException( error, e, getLocation() );
            }
        }
    }

    private Properties getTypeProperties( Class subject ) throws IntrospectionException
    {
        // TODO: change this to a .xproperties
        try
        {
            Field field = subject.getDeclaredField( "TYPE_INFO_PROPERTIES" );
            if( Modifier.isStatic( field.getModifiers() ) )
            {
                if( Properties.class.isAssignableFrom( field.getType() ) )
                {
                    return (Properties) field.get( null );
                }
                else
                {
                    final String error =
                      "The component type [" 
                      + subject.getName()
                      + "] declares an invalid static field TYPE_INFO_PROPERTIES declaration. "
                      + "The declared type is not assignable to a java.util.Properties value.";
                    throw new IntrospectionException( error );
                }
            }
            else
            {
                return null;
            }
        }
        catch( NoSuchFieldException e )
        {
            return null;
        }
        catch( IllegalArgumentException e )
        {
            final String error =
              "The component type [" 
              + subject.getName()
              + "] declares an invalid static field TYPE_INFO_PROPERTIES declaration. "
              + "Could not convert the value to an instance of java.util.Properties.";
            throw new IntrospectionException( error );
        }
        catch( IntrospectionException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error =
              "An unexpected error occured while resolving the static "
              + "TYPE_INFO_PROPERTIES field on the type [" 
              + subject.getName()
              + "].";
            throw new IntrospectionException( error );
        }
    }

    private ServiceDescriptor[] createServiceDescriptors( Class subject )
    {
        ArrayList list = new ArrayList();
        Class[] interfaces = subject.getInterfaces();
        for( int i=0; i<interfaces.length; i++ )
        {
            Class service = interfaces[i];
            ServiceDescriptor descriptor = createServiceDescriptor( subject, service );
            if( null != descriptor )
            {
                list.add( descriptor );
            }
        }
        return (ServiceDescriptor[]) list.toArray( new ServiceDescriptor[0] );
    }

    public ServiceDescriptor createServiceDescriptor( Class type, Class subject )
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

    private PartDescriptor[] createPartDescriptors( Class subject ) 
       throws IntrospectionException
    {
        String classname = subject.getName();
        Class[] classes = subject.getClasses();
        Class parts = locateClass( "$Parts", classes );
        if( null == parts )
        {
            return new PartDescriptor[0];
        }
        else
        {
            //
            // Based on the method declarations in the parts inner class
            // we collect a list of the unique keys where each key represents a 
            // unique part.
            //

            Method[] methods = parts.getMethods();
            ArrayList list = new ArrayList();
            for( int i=0; i<methods.length; i++ )
            {
                Method method = methods[i];
                String key = PartDescriptor.getPartKey( method );
                if( false == list.contains( key ) )
                {
                    list.add( key );
                }
            }

            String[] keys = (String[]) list.toArray( new String[0] );
            PartDescriptor[] descriptors = new PartDescriptor[ keys.length ];
            for( int i=0; i<keys.length; i++ )
            {
                String key = keys[i];
                Operation[] operations = getOperations( subject, key, methods );
                PartDescriptor descriptor = new PartDescriptor( key, operations );
                descriptors[i] = descriptor;
            }
  
            return descriptors;
        }
    }

    private Operation[] getOperations( Class subject, String key, Method[] methods ) throws IntrospectionException
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<methods.length; i++ )
        {
            Method method = methods[i];
            if( key.equals( PartDescriptor.getPartKey( method ) ) )
            {
                Operation operation = getOperation( subject, key, method );
                list.add( operation );
            }
        }
        return (Operation[]) list.toArray( new Operation[0] );
    }

    private Operation getOperation( Class subject, String key, Method method ) throws IntrospectionException
    {
        final String name = method.getName();
        final int semantic = PartDescriptor.getPartSemantic( method );
        if( semantic == PartDescriptor.GET )
        {
            validateNoExceptions( method );
            Class returnType = method.getReturnType();
            validateNonNullReturnType( method );
            validateNonArrayReturnType( method, returnType );
            String type = returnType.getName();
            String postfix = PartDescriptor.getPartPostfix( method );
            if( PartDescriptor.COMPONENT_KEY.equals( postfix ) )
            {
                validateNonNullReturnType( method );
                validateAtMostOneParameter( method );
            }
            else
            {
                validateSelectPattern( subject, method );
            }
            return new Operation( PartDescriptor.GET, postfix, type );
        }
        else if( semantic == PartDescriptor.RELEASE )
        {
            validateNoExceptions( method );
            Class param = validateSingleParameter( method );
            validateNonArrayParameter( method, param );
            validateNonNullParameter( method, param );
            Class returnType = method.getReturnType();
            validateNullReturnType( method, returnType );
            String classname = param.getName();
            return new Operation( PartDescriptor.RELEASE, classname );
        }
        else
        {
            final String error = 
              "Unrecognized part accessor method signature ["
              + name 
              + "] for the parts key ["
              + key 
              + "].";
            throw new IllegalArgumentException( error );
        }
    }

    private ContextDescriptor createContextDescriptor( Class subject ) throws IntrospectionException
    {
        Class[] classes = subject.getClasses();
        Class param = locateClass( "$Context", classes );
        if( null == param )
        {
            return new ContextDescriptor( null, new EntryDescriptor[0] );
        }
        else
        {
            EntryDescriptor[] entries = createEntryDescriptors( subject );
            String classname = param.getName();
            return new ContextDescriptor( classname, entries );
        }
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
            validateNonArrayReturnType( method, returnType );
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
              "Unable to establish a required or optional context entry method pattern on the method ["
              + method.getName()
              + "]";
            throw new IntrospectionException( error );
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
        if( false == type.isAssignableFrom( c ) )
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
        if( false == Void.TYPE.equals( returnType ) ) 
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
        if( returnType.isInterface() == false )
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
        if( method.getName().startsWith( "get" ) == false )
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
            if( false == Boolean.TYPE.isAssignableFrom( b ) )
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

    private State resolveStateGraph( Class subject )
    {
        final String classname = subject.getName();
        final ClassLoader classloader = subject.getClassLoader();
        final String xgraph = classname.replace( '.', '/' ) + ".xgraph";
        final InputStream input = classloader.getResourceAsStream( xgraph );
        if( null == input )
        {
            return null;
        }
        else
        {
            try
            {
                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                Configuration config = builder.build( input );
                StateBuilder stateBuilder = new StateBuilder( this );
                return stateBuilder.build( config );
            }
            catch( Exception e )
            {
                final String error =
                  "An unexpected error occured while resolving the state model [" 
                  + xgraph 
                  + "]: " + e.toString();
                throw new BuildException( error, e, getLocation() );
            }
        }
    }

    private static URI TYPE_HANDLER_URI = setupURI( "@TYPE-HANDLER-URI@" );
    private static URI TYPE_BUILDER_URI = setupURI( "@TYPE-BUILDER-URI@" );

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
