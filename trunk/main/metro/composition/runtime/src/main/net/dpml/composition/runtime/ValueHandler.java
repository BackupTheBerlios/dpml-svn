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
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.composition.controller.CompositionController;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.info;.ServiceDescriptor;

import net.dpml.logging.Logger;

import net.dpml.part.Part;
import net.dpml.part.Control;

import net.dpml.component.control.Controller;
import net.dpml.component.control.ControllerContext;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.ClassLoadingContext;
import net.dpml.component.runtime.ComponentException;
import net.dpml.component.runtime.ComponentRuntimeException;
import net.dpml.component.runtime.Service;
import net.dpml.component.runtime.AvailabilityException;

import net.dpml.state.State;
import net.dpml.component.info;.ActivationPolicy;

import net.dpml.transit.util.PropertyResolver;

/**
 * Default implementation of a value component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ValueHandler extends UnicastRemoteObject implements Component, ClassLoadingContext
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final String m_name;
    private final URI m_uri;
    private final ValueDirective m_directive;
    private final ClassLoader m_classloader;
    private final ValueController m_manager;
    private final Component m_parent;
    private final CompositionController m_controller;
    private final ServiceDescriptor[] m_services;
    private final Logger m_logger;
    private final Map m_map;

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
        URI uri, String name, ValueDirective part, Component parent ) throws RemoteException
    {
        super();

        if( controller == null )
        {
            throw new NullPointerException( "controller" );
        }
        if( logger == null )
        {
            throw new NullPointerException( "logger" );
        }
        if( null == part )
        {
            throw new NullPointerException( "part" );
        }
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }

        m_name = name;
        m_logger = logger;
        m_parent = parent;
        m_controller = controller;
        m_directive = part;
        m_classloader = classloader;
        m_uri = uri;
        m_manager = controller.getValueController();

        //String classname = getReturnTypeClassname();
        //m_services = new ServiceDescriptor[]{ new ServiceDescriptor( classname ) };
        m_services = new ServiceDescriptor[0];
        
        m_map = new Hashtable();
        ControllerContext context = controller.getControllerContext();
        m_map.put( "urn:system:context.uri", m_controller.getURI() );
        m_map.put( "urn:system:work.dir", context.getWorkingDirectory() );
        m_map.put( "urn:system:work.dir", context.getTempDirectory() );
        m_map.put( "urn:component:name", getParentName() );
        m_map.put( "urn:component:uri", getParentURI() );
    }

   /**
    * Get the activation policy for the control.
    *
    * @return the activation policy
    * @see ActivationPolicy#SYSTEM
    */
    public ActivationPolicy getActivationPolicy()
    {
        return ActivationPolicy.SYSTEM;
    }

    protected Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the part that defines this component.
    * @return the component part definition
    */
    public Part getDefinition()
    { 
        return m_directive;
    }

   /**
    * Return an array of service descriptors corresponding to 
    * the service contracts that the service publishes.
    * @return the service descriptor array
    */
    public ServiceDescriptor[] getDescriptors() throws RemoteException
    {
        return m_services;
    }

    public ClassLoader getClassLoader()
    {
        return m_classloader;
    }

    public String getName() throws RemoteException
    {
        return m_name;
    }

    public ValueController getValueController()
    {
        return m_manager;
    }

   /**
    * Return an instance of the component.
    * @return the resolved instance
    */
    public Object resolve() throws Exception
    {
        return getValue();
    }

   /**
    * Return an instance of the component using a supplied isolation policy.
    * If the isolation policy is TRUE an implementation shall make best efforts to isolate
    * implementation concerns under the object that is returned.  Typically isolation 
    * involves the creation of a proxy of a component implementation instance that 
    * exposes a component's service interfaces to a client.  If the isolation policy if
    * FALSE the implementation shall return the component implementation instance.
    * 
    * @param policy the isolation policy
    * @return the resolved instance
    */
    public Object resolve( boolean policy ) throws Exception
    {
        return getValue();
    }

   /**
    * Release a reference to an object managed by the instance.
    * 
    * @param instance the instance to release
    */
    public void release( Object instance ) throws RemoteException
    {
        // nothing to do
    }

    public URI getURI() throws RemoteException
    {
        return getLocalURI();
    }

    URI getLocalURI()
    {
        return m_uri;
    }

    public Object getInstance()
    {
        return getValue();
    }

   /**
    * Return the context entry value.
    *
    * @return the context entry value
    */
    public Object getValue()
    {
        if( null == m_value )
        {
            try
            {
                m_value = m_directive.resolve( m_map, m_classloader, false );
            }
            catch( Throwable e )
            {
                final String error =
                "Cannot establish a constructed value  ["
                + getLocalURI()
                + "].";
                throw new ComponentRuntimeException( error, e );
            }
        }
        
        return m_value;
            
       /*
        try
        {            
            String local = m_directive.getLocalValue();
            String argument = expandSymbols( local );
            Object object = checkForURNReference( argument );
            if( null == object )
            {
                String classname = getReturnTypeClassname();
                ValueDirective[] params = m_directive.getValues();
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
              + getLocalURI()
              + "].";
            throw new ComponentRuntimeException( error, e );
        }
        */
    }
    
    private String getParentName()
    {
        if( null == m_parent )
        {
            return null;
        }
        try
        {
            return m_parent.getName();
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception raised by parent component when requesting name."
              + "\nParent component: " + m_parent.getClass().getName()
              + "\nComponent: " + getLocalURI();
            throw new ComponentRuntimeException( error, e );
        }
    }
    
    private URI getParentURI()
    {
        if( null == m_parent )
        {
            return null;
        }
        try
        {
            return m_parent.getURI();
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote exception raised by parent component when requesting uri."
              + "\nParent component: " + m_parent.getClass().getName()
              + "\nComponent: " + getLocalURI();
            throw new ComponentRuntimeException( error, e );
        }
    }

   /*
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
    */
    
   /*
    private String getReturnTypeClassname()
    {
        return m_directive.getClassname();
    }
    */

   /**
    * Return the context entry value.
    *
    * @return the context entry value
    */
    /*
    public Object getValue( ValueDirective p )
        throws ComponentException
    {
        String local = p.getLocalValue();
        String argument = expandSymbols( local );
        Object object = checkForURNReference( argument );
        if( null != object )
        {
            return object;
        }
        String classname = p.getClassname();
        ValueDirective[] params = p.getValues();
        Class clazz = getValueClass( classname );
        return getValue( clazz, argument, params );
    }
    */
        
        
    
    /**
     * Return the derived parameter value.
     * @param clazz the constructor class
     * @param argument a single string constructor argument
     * @param parameters an alternative sequence of arguments
     * @return the value
     * @exception ComponentException if the parameter value cannot be resolved
     */
    /*
    public Object getValue( Class clazz, String argument, ValueDirective[] parameters )
        throws ComponentException
    {
        if( null != argument )
        {
            return getSingleArgumentConstructorValue( clazz, argument );
        }
        else
        {
            if( parameters.length == 0 )
            {
                return getNullArgumentConstructorValue( clazz );
            }
            else
            {
                return getMultiArgumentConstructorValue( clazz, parameters );
            }
        }
    }
    */

   /*
    private Object getMultiArgumentConstructorValue( Class clazz, ValueDirective[] parameters )
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
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access null constructor for the class ["
                  + clazz.getName()
                  + "] withing the value type ["
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
        }
        else
        {
            Class[] params = new Class[ parameters.length ];
            for ( int i = 0; i < parameters.length; i++ )
            {
                ValueDirective value = parameters[i];
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
                      + getLocalURI()
                      + "].";
                    throw new ComponentException( error, e );
                }
            }

            Object[] values = new Object[ parameters.length ];
            for ( int i = 0; i < parameters.length; i++ )
            {
                ValueDirective p = parameters[i];
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
                      + getLocalURI()
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
                  "Parameters class ["
                  + clazz.getName()
                  + "] withing the value type ["
                  + getLocalURI()
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
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access multi-parameter constructor for the class ["
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( Throwable e )
            {
                final String error =
                  "Unexpected error while attempting to instantiate a multi-parameter constructor for the class [" 
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
        }
    }
    */

   /*
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
              + getLocalURI()
              + "].";
            throw new ComponentException( error, e );
        }
        catch ( IllegalAccessException e )
        {
            final String error =
              "Cannot access null parameter constructor for the class ["
              + clazz.getName()
              + "] withing the value type ["
              + getLocalURI()
              + "].";
            throw new ComponentException( error, e );
        }
        catch ( Throwable e )
        {
            final String error =
              "Unexpected exception while creating the class ["
              + clazz.getName()
              + "] withing the value type ["
              + getLocalURI()
              + "].";
            throw new ComponentException( error, e );
        }
    }
    */
    /*
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
                  + getLocalURI()
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
                  + "] within the value type ["
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access single string parameter constructor for the class: ["
                  + clazz.getName() 
                  + "] within the value type ["
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
            catch ( Throwable e )
            {
                final String error =
                  "Unexpected exception while creating a single string parameter value for the class ["
                  + clazz.getName() 
                  + "] withing the value type ["
                  + getLocalURI()
                  + "].";
                throw new ComponentException( error, e );
            }
        }
    }
    */
   /*
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
              + getLocalURI()
              + "] is not supported.";
            throw new ComponentException( error );
        }
    }
    */

    /**
     * Return the classname of the parameter implementation to use.
     * @return the parameter class
     * @exception ComponentException if the parameter class cannot be resolved
     */
     /*
    Class getValueClass( ValueDirective value ) throws ComponentException
    {
        String v = value.getLocalValue();
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
    */

    /**
     * Return the classname of the parameter implementation to use.
     * @return the parameter class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    /*
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
                  + getLocalURI()
                  + "].";
               throw new ComponentException( error, e );
            }
        }
    }
    */
    
    /*
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
                return m_controller.getURI();
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
                try
                {
                    return m_parent.getName();
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Remote exception raised by parent component when requesting name."
                      + "\nParent component: " + m_parent.getClass().getName()
                      + "\nComponent: " + getLocalURI();
                    throw new ComponentRuntimeException( error, e );
                }
            }
            else if( "uri".equals( key ) )
            {
                try
                {
                    return m_parent.getURI();
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Remote exception raised by parent component when requesting uri."
                      + "\nParent component: " + m_parent.getClass().getName()
                      + "\nComponent: " + getLocalURI();
                    throw new ComponentRuntimeException( error, e );
                }
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
    */
}
