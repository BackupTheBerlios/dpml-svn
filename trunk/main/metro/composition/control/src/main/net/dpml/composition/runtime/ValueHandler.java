/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.composition.runtime;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.rmi.RemoteException;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.data.ValueDirective;
import net.dpml.composition.data.ValueDirective.Value;

import net.dpml.logging.Logger;

import net.dpml.part.control.Controller;
import net.dpml.part.control.ControllerContext;
import net.dpml.part.control.Component;
import net.dpml.part.control.ClassLoadingContext;
import net.dpml.part.control.ComponentException;
import net.dpml.part.control.ComponentRuntimeException;
import net.dpml.part.service.Service;
import net.dpml.part.service.ServiceDescriptor;
import net.dpml.part.service.AvailabilityException;
import net.dpml.part.state.State;

import net.dpml.transit.util.PropertyResolver;

/**
 * Default implementation of a value component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ValueHandler extends AbstractHandler implements Component, ClassLoadingContext
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final URI m_uri;
    private final ValueDirective m_directive;
    private final ClassLoader m_classloader;
    private final Manager m_manager;
    private final Component m_parent;
    private final CompositionController m_controller;
    private final ServiceDescriptor[] m_services;

    private Object m_key;
    private Object m_value;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new value handler.
    *
    * @param logger the logging channel
    * @param controller the composition controller
    * @param classloader the assigned classloader
    * @param uri the uri identifying the component
    * @param part the value directive
    * @param parent the enclosing component (possibly null if this is a top-level component)
    * @exception NullPointerException if a null value is supplied for any argument
    *     other than the enclosing component
    */
    public ValueHandler(
        Logger logger, CompositionController controller, ClassLoader classloader, 
        URI uri, ValueDirective part, Component parent )
    {
        super( logger );

        if( controller == null )
        {
            throw new NullPointerException( "controller" );
        }
        if( null == part )
        {
            throw new NullPointerException( "part" );
        }

        m_parent = parent;
        m_controller = controller;
        m_directive = part;
        m_classloader = classloader;
        m_uri = uri;
        m_manager = controller.getValueController();

        String classname = getReturnTypeClassname();
        m_services = new ServiceDescriptor[]{ new ServiceDescriptor( classname ) };
    }

   /**
    * Return an array of service descriptors corresponding to 
    * the service contracts that the service publishes.
    * @return the service descriptor array
    */
    public ServiceDescriptor[] getDescriptors()
    {
        return m_services;
    }

    public ClassLoader getClassLoader()
    {
        return m_classloader;
    }

    public String getName()
    {
        return m_directive.getKey();
    }

    public Manager getManager()
    {
        return m_manager;
    }

   /**
    * Return the availability status of the component.
    * @return the availability status
    */
    public boolean isOperational()
    {
        return true;
    }

    public URI getURI()
    {
        return m_uri;
    }

    public Object getInstance()
    {
        return getValue();
    }

   /**
    * Issue a request to the service to prepare for operations.
    * @exception AvailabilityException if the service cannot be made available
    */
    public void prepare() throws AvailabilityException
    {
        m_manager.prepare( this );
    }

   /**
    * Initialize the component.  
    */
    public void initialize() throws Exception
    {
        m_manager.initialize( this );
    }

   /**
    * Termination of the component.
    */
    public void terminate()
    {
        m_manager.terminate( this );
    }

   /**
    * Return the context entry value.
    *
    * @return the context entry value
    */
    public Object getValue()
    {
        if( m_value != null )
        {
            return m_value;
        }

        try
        {
            String local = m_directive.getLocalValue();
            String argument = expandSymbols( local );
            Object object = checkForURNReference( argument );
            if( null == object )
            {
                String classname = getReturnTypeClassname();
                Value[] params = m_directive.getValues();
                Class clazz = getValueClass( classname );
                object = getValue( clazz, argument, params );
            }
            //if( !m_descriptor.isVolatile() )
            //{
            //    m_value = object;
            //}
            return object;
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot establish a constructed value  ["
              + getURI()
              + "].";
            throw new ComponentRuntimeException( error, e );
        }
    }

    private String expandSymbols( String value )
    {
        if( null == value )
        {
            return null;
        }
        else
        {
            return PropertyResolver.resolve( value );
        }
    }

    private String getReturnTypeClassname()
    {
        return m_directive.getClassname();
    }

   /**
    * Return the context entry value.
    *
    * @return the context entry value
    */
    public Object getValue( Value p )
        throws ComponentException
    {
        String local = p.getArgument();
        String argument = expandSymbols( local );
        Object object = checkForURNReference( argument );
        if( null != object )
        {
            return object;
        }
        String classname = p.getClassname();
        Value[] params = p.getValueDirectives();
        Class clazz = getValueClass( classname );
        return getValue( clazz, argument, params );
    }

    /**
     * Return the derived parameter value.
     * @param clazz the constructor class
     * @param argument a single string constructor argument
     * @param parameters an alternative sequence of arguments
     * @return the value
     * @exception ComponentException if the parameter value cannot be resolved
     */
    public Object getValue( Class clazz, String argument, Value[] parameters )
        throws ComponentException
    {
        if( parameters.length == 0 )
        {
            if( argument == null )
            {
                return getNullArgumentConstructorValue( clazz );
            }
            else
            {
                return getSingleArgumentConstructorValue( clazz, argument );
            }
        }
        else
        {
             return getMultiArgumentConstructorValue( clazz, parameters );
        }
    }

    private Object getMultiArgumentConstructorValue( Class clazz, Value[] parameters )
      throws ComponentException
    {
        //
        // getting here means we are dealing with 0..n types parameter constructor where the
        // parameters are defined by the nested parameter definitions
        //

        if ( parameters.length == 0 )
        {
            try
            {
                return clazz.newInstance();
            }
            catch ( InstantiationException e )
            {
                final String error =
                  "Unable to instantiate instance of class [" 
                  + clazz.getName()
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access null constructor for the class ["
                  + clazz.getName()
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
        }
        else
        {
            Class[] params = new Class[ parameters.length ];
            for ( int i = 0; i < parameters.length; i++ )
            {
                Value value = parameters[i];
                try
                {
                    params[i] = getValueClass( value );
                }
                catch ( Throwable e )
                {
                    final String error =
                      "Unable to resolve sub-parameter class ["
                      + value.getClassname()
                      + "] for the parameter [" 
                      + clazz.getName()
                      + "] withing the value type ["
                      + getURI()
                      + "].";
                    throw new ComponentException( error, e );
                }
            }

            Object[] values = new Object[ parameters.length ];
            for ( int i = 0; i < parameters.length; i++ )
            {
                Value p = parameters[i];
                String classname = p.getClassname();
                try
                {
                    values[i] = getValue( p );
                }
                catch ( Throwable e )
                {
                    final String error =
                      "Unable to instantiate sub-parameter for value ["
                      + classname
                      + "] inside the parameter [" 
                      + clazz.getName()
                      + "] withing the value type ["
                      + getURI()
                      + "].";
                    throw new ComponentException( error, e );
                }
            }
            Constructor constructor = null;
            try
            {
                constructor = clazz.getConstructor( params );
            }
            catch ( NoSuchMethodException e )
            {
                final String error =
                  "Supplied parameters class ["
                  + clazz.getName()
                  + "] withing the value type ["
                  + getURI()
                  + "] does not match the available class constructors.";
                throw new ComponentException( error, e );
            }

            try
            {
                return constructor.newInstance( values );
            }
            catch ( InstantiationException e )
            {
                final String error =
                  "Unable to instantiate an instance of a multi-parameter constructor for class ["
                  + clazz.getName()
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access multi-parameter constructor for the class ["
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( Throwable e )
            {
                final String error =
                  "Unexpected error while attempting to instantiate a multi-parameter constructor for the class [" 
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
        }
    }

    private Object getNullArgumentConstructorValue( Class clazz )
      throws ComponentException
    {
        try
        {
            return clazz.newInstance();
        }
        catch ( InstantiationException e )
        {
            final String error =
              "Unable to instantiate instance of class [" 
              + clazz.getName()
              + "] withing the value type ["
              + getURI()
              + "].";
            throw new ComponentException( error, e );
        }
        catch ( IllegalAccessException e )
        {
            final String error =
              "Cannot access null parameter constructor for the class ["
              + clazz.getName()
              + "] withing the value type ["
              + getURI()
              + "].";
            throw new ComponentException( error, e );
        }
        catch ( Throwable e )
        {
            final String error =
              "Unexpected exception while creating the class ["
              + clazz.getName()
              + "] withing the value type ["
              + getURI()
              + "].";
            throw new ComponentException( error, e );
        }
    }

    private Object getSingleArgumentConstructorValue( Class clazz, String argument )
      throws ComponentException
    {
        Object object = checkForURNReference( argument );
        if( null != object )
        {
            return object;
        }
        else
        {
            //
            // the argument is a simple type that takes a single String value
            // as a constructor argument
            //

            if( clazz.isPrimitive() )
            {
                return getPrimitiveValue( clazz, argument );
            }

            try
            {
                final Class[] params = new Class[]{ String.class };
                Constructor constructor = clazz.getConstructor( params );
                final Object[] values = new Object[]{ argument };
                return constructor.newInstance( values );
            }
            catch ( NoSuchMethodException e )
            {
                final String error =
                  "Class: [" 
                  + clazz.getName()
                  + "] referenced withing the value type ["
                  + getURI()
                  + "] does not implement a single string argument constructor.";
                throw new ComponentException( error );
            }
            catch ( InstantiationException e )
            {
                final String error =
                  "Unable to instantiate instance of class [" 
                  + clazz.getName()
                  + "] with the single argument ["
                  + argument 
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access single string parameter constructor for the class: ["
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( Throwable e )
            {
                final String error =
                  "Unexpected exception while creating a single string parameter value for the class ["
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getURI()
                  + "].";
                throw new ComponentException( error, e );
            }
        }
    }

    private Object getPrimitiveValue( Class clazz, String argument ) throws ComponentException
    {
        if( Integer.TYPE == clazz )
        {
            return Integer.valueOf( argument );
        }
        else if( Boolean.TYPE == clazz )
        {
            return Boolean.valueOf( argument );
        }
        else if( Byte.TYPE == clazz )
        {
            return Byte.valueOf( argument );
        }
        else if( Short.TYPE == clazz )
        {
            return Short.valueOf( argument );
        }
        else if( Long.TYPE == clazz )
        {
            return Long.valueOf( argument );
        }
        else if( Float.TYPE == clazz )
        {
            return Float.valueOf( argument );
        }
        else if( Double.TYPE == clazz )
        {
            return Double.valueOf( argument );
        }
        else
        {
            final String error =
              "The primitive return type ["
              + clazz.getName()
              + "] withing the value type ["
              + getURI()
              + "] is not supported.";
            throw new ComponentException( error );
        }
    }

    /**
     * Return the classname of the parameter implementation to use.
     * @return the parameter class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    Class getValueClass( Value value ) throws ComponentException
    {
        String v = value.getArgument();
        Object object = checkForURNReference( v );
        if( null != object )
        {
            return object.getClass();
        }
        else
        {
            String classname = value.getClassname();
            return getValueClass( classname );
        }
    }

    /**
     * Return the classname of the parameter implementation to use.
     * @return the parameter class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    Class getValueClass( String classname ) throws ComponentException
    {
        try
        {
            return m_classloader.loadClass( classname );
        }
        catch ( final ClassNotFoundException e )
        {
            if ( classname.equals( "int" ) )
            {
                return int.class;
            }
            else if ( classname.equals( "short" ) )
            {
                return short.class;
            }
            else if ( classname.equals( "long" ) )
            {
                return long.class;
            }
            else if ( classname.equals( "byte" ) )
            {
                return byte.class;
            }
            else if ( classname.equals( "double" ) )
            {
                return double.class;
            }
            else if ( classname.equals( "byte" ) )
            {
                return byte.class;
            }
            else if ( classname.equals( "float" ) )
            {
                return float.class;
            }
            else if ( classname.equals( "char" ) )
            {
                return char.class;
            }
            else if ( classname.equals( "char" ) )
            {
                return char.class;
            }
            else if ( classname.equals( "boolean" ) )
            {
                return boolean.class;
            }
            else
            {
                final String error =
                  "Could not locate the implementation for class ["
                  + classname 
                  + "] withing the value type ["
                  + getURI()
                  + "].";
               throw new ComponentException( error, e );
            }
        }
    }

    private Object checkForURNReference( String argument )
    {
        if( null == argument )
        {
            return null;
        }
        else if ( argument.startsWith( "urn:system:" ) )
        {
            ControllerContext context = m_controller.getControllerContext();
            String key = argument.substring( 11 );
            if( "context.uri".equals( key ) )
            {
                return context.getURI();
            }
            else if( "work.dir".equals( key ) )
            {
                return context.getWorkingDirectory();
            }
            else if( "temp.dir".equals( key ) )
            {
                return context.getTempDirectory();
            }
            else
            {
                final String error = 
                  "Requested system resource urn ["
                  + key 
                  + "] is not recognized.";
                throw new IllegalArgumentException( error );
            }
        }
        else if ( argument.startsWith( "urn:component:" ) )
        {
            if( null == m_parent )
            {
                return null;
            }
            String key = argument.substring( 14 );
            if( "name".equals( key ) )
            {
                return m_parent.getName();
            }
            else if( "uri".equals( key ) )
            {
                return m_parent.getURI();
            }
            else
            {
                final String error = 
                  "Requested component resource urn ["
                  + key 
                  + "] is not recognized.";
                throw new IllegalArgumentException( error );
            }
        }
        else
        {
            return null;
        }
    }
}
