/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.composition.model.impl;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.avalon.composition.data.ConstructorDirective;
import org.apache.avalon.composition.data.Parameter;
import org.apache.avalon.composition.model.EntryModel;
import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.provider.ComponentContext;

import org.apache.avalon.meta.info.EntryDescriptor;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;


/**
 * Default implementation of a the context entry constructor model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultConstructorModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultConstructorModel extends DefaultEntryModel
{
    //==============================================================
    // static
    //==============================================================

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultConstructorModel.class );

    //==============================================================
    // immutable state
    //==============================================================

    private final ConstructorDirective m_directive;

    private final EntryDescriptor m_descriptor;

    private final ComponentContext m_context;

    private final Map m_map;

    //==============================================================
    // mutable state
    //==============================================================

    private Object m_value;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a new context entry import model.
    *
    * @param descriptor the context entry descriptor
    * @param directive the context entry directive
    * @param context the containment context
    * @param map a map of available context entries
    */
    public DefaultConstructorModel( 
      EntryDescriptor descriptor, ConstructorDirective directive, 
      ComponentContext context, Map map ) throws ModelException
    {
        super( descriptor );

        if( directive == null )
        {
            throw new NullPointerException( "directive" );
        }
        if( context == null )
        {
            throw new NullPointerException( "context" );
        }
        m_descriptor = descriptor;
        m_directive = directive;
        m_context = context;
        m_map = map;

        validate();
    }

    private void validate() throws ModelException
    {
        final String descriptorClassName = m_descriptor.getClassname();
        final String directiveClassName = m_directive.getClassname();
        validatePair( descriptorClassName, directiveClassName );
        Parameter[] params = m_directive.getParameters();

        //
        // TODO:
        // wizz through and validate all of the parameter declarations
        // and make sure that constructors exist that match the sub-parameter
        // delcarations
        //
    }

    private void validatePair( String descriptorClass, String directiveClass )
      throws ModelException
    {
        final String key = m_descriptor.getKey();
        ClassLoader loader = m_context.getClassLoader();

        Class target = null;
        try
        {
            target = loader.loadClass( descriptorClass );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              REZ.getString( "constructor.descriptor.unknown.error", key, descriptorClass );
            throw new ModelException( error ); 
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "constructor.descriptor.load.error", key, descriptorClass );
            throw new ModelException( error, e ); 
        }

        Class source = null;
        try
        {
            source = loader.loadClass( directiveClass );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              REZ.getString( "constructor.directive.unknown.error", key, directiveClass );
            throw new ModelException( error ); 
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "constructor.directive.load.error", key, directiveClass );
            throw new ModelException( error, e ); 
        }

        if( !target.isAssignableFrom( source ) )
        {
            final String error = 
              REZ.getString( 
                "constructor.invalid-model.error", 
                key, descriptorClass, directiveClass );
            throw new ModelException( error ); 
        }
    }


    //==============================================================
    // EntryModel
    //==============================================================

   /**
    * Return the context entry value.
    * 
    * @return the context entry value
    */
    public Object getValue() throws ModelException
    {
        if( m_value != null )
        {
            return m_value;
        }
        
        String target = m_descriptor.getKey();
        Object object = null;
        try
        {
            ClassLoader loader = m_context.getClassLoader();
            String classname = m_directive.getClassname();
            String argument = m_directive.getArgument();
            Parameter[] params = m_directive.getParameters();
            Class clazz = getParameterClass( classname, loader );
            object = getValue( loader, clazz, argument, params );
        }
        catch( Throwable e )
        {
            final String error = 
              "Cannot establish a constructed context entry for the key " + target 
              + " due to a runtime failure.";
            throw new ModelException( error, e );
        }

        if( !m_descriptor.isVolatile() )
        {
            m_value = object;
        }
        
        return object;
    }

   /**
    * Return the context entry value.
    * 
    * @return the context entry value
    */
    public Object getValue( Parameter p ) throws ModelException
    {
        ClassLoader loader = m_context.getClassLoader();
        String classname = p.getClassname();
        String argument = p.getArgument();
        Parameter[] params = p.getParameters();
        Class clazz = getParameterClass( classname, loader );
        return getValue( loader, clazz, argument, params );
    }

    /**
     * Return the derived parameter value.
     * @param loader the classloader to use
     * @param clazz the constructor class
     * @param argument a single string constructor argument
     * @param parameters an alternative sequence of arguments
     * @return the value
     * @exception ModelException if the parameter value cannot be resolved
     */
    public Object getValue( 
       ClassLoader loader, Class clazz, String argument, 
       Parameter[] parameters ) throws ModelException
    {
        //
        // if the parameter contains a text argument then check if its a reference
        // to a map entry (in the form"${<key>}" ), otherwise its a simple constructor
        // case with a single string paremeter
        //

        if( parameters.length == 0 )
        {
            if( argument == null )
            {
                return getNullArgumentConstructorValue( clazz );
            }
            else
            {
                return getSingleArgumentConstructorValue( loader, clazz, argument );
            }
        }
        else
        {
             return getMultiArgumentConstructorValue( loader, clazz, parameters );
        }
    }

    private Object getMultiArgumentConstructorValue( 
      ClassLoader classLoader, Class clazz, Parameter[] parameters )
      throws ModelException
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
                  "Unable to instantiate instance of class: " + clazz.getName();
                throw new ModelException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access null constructor for the class: '"
                  + clazz.getName() + "'.";
                throw new ModelException( error, e );
            }
        }
        else
        {
            Class[] params = new Class[ parameters.length ];
            for ( int i = 0; i < parameters.length; i++ )
            {
                String classname = parameters[i].getClassname();
                try
                {
                    params[i] = classLoader.loadClass( classname );
                }
                catch ( Throwable e )
                {
                    final String error = 
                      "Unable to resolve sub-parameter class: "
                        + classname
                        + " for the parameter " + clazz.getName();
                    throw new ModelException( error, e );
                }
            }

            Object[] values = new Object[ parameters.length ];
            for ( int i = 0; i < parameters.length; i++ )
            {
                Parameter p = parameters[i];
                String classname = p.getClassname();
                try
                {
                    values[i] = getValue( p );
                }
                catch ( Throwable e )
                {
                    final String error = 
                      "Unable to instantiate sub-parameter for value: "
                        + classname
                        + " inside the parameter " + clazz.getName();
                    throw new ModelException( error, e );
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
                  "Supplied parameters for " 
                    + clazz.getName()
                    + " do not match the available class constructors.";
                throw new ModelException( error, e );
            }

            try
            {
                return constructor.newInstance( values );
            }
            catch ( InstantiationException e )
            {
                final String error =
                  "Unable to instantiate an instance of a multi-parameter constructor for class: '"
                  + clazz.getName() + "'.";
                throw new ModelException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access multi-parameter constructor for the class: '"
                  + clazz.getName() + "'.";
                throw new ModelException( error, e );
            }
            catch ( Throwable e )
            {
                final String error =
                  "Unexpected error while attmpting to instantiate a multi-parameter constructor "
                  + "for the class: '" + clazz.getName() + "'.";
                throw new ModelException( error, e );
            }
        }
    }

    private Object getNullArgumentConstructorValue( Class clazz )
      throws ModelException
    {
        try
        {
            return clazz.newInstance();
        }
        catch ( InstantiationException e )
        {
            final String error = 
              "Unable to instantiate instance of class: " + clazz.getName();
            throw new ModelException( error, e );
        }
        catch ( IllegalAccessException e )
        {
            final String error =
              "Cannot access null parameter constructor for the class: '"
              + clazz.getName() + "'.";
            throw new ModelException( error, e );
        }
        catch ( Throwable e )
        {
            final String error =
              "Unexpected exception while creating the class: '"
                + clazz.getName() + "'.";
            throw new ModelException( error, e );
        }
    }

    private Object getSingleArgumentConstructorValue( 
      ClassLoader classLoader, Class clazz, String argument )
      throws ModelException
    {
        if ( argument.startsWith( "${" ) )
        {
            if ( argument.endsWith( "}" ) )
            {
                final String key = argument.substring( 2, argument.length() - 1 );
                Object value = m_map.get( key );
                if ( value != null )
                {
                    if( value instanceof EntryModel )
                    {
                        return ((EntryModel)value).getValue();
                    }
                    else
                    {
                        return value;
                    }
                }
                else
                {
                    final String error = 
                      "Unresolvable primative context value: '" + key + "'.";
                    throw new ModelException( error );
                }
            }
            else
            {
                final String error = 
                  "Illegal format for context reference: '" + argument + "'.";
                throw new ModelException( error );
            }
        }
        else
        {
            //
            // the argument is a simple type that takes a single String value
            // as a constructor argument
            //

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
                  "Class: '" + clazz.getName()
                  + "' does not implement a single string argument constructor.";
                throw new ModelException( error );
            }
            catch ( InstantiationException e )
            {
                final String error = 
                  "Unable to instantiate instance of class: " + clazz.getName()
                  + " with the single argument: '" + argument + "'";
                throw new ModelException( error, e );
            }
            catch ( IllegalAccessException e )
            {
                final String error =
                  "Cannot access single string parameter constructor for the class: '"
                  + clazz.getName() + "'.";
                throw new ModelException( error, e );
            }
            catch ( Throwable e )
            {
                final String error =
                  "Unexpected exception while creating a single string parameter value for the class: '"
                  + clazz.getName() + "'.";
                throw new ModelException( error, e );
            }
        }
    }

    /**
     * Return the classname of the parameter implementation to use.
     * @param loader the classloader to use
     * @return the parameter class
     * @exception ModelException if the parameter class cannot be resolved
     */
    Class getParameterClass( String classname, ClassLoader classLoader ) throws ModelException
    {
        try
        {
            return classLoader.loadClass( classname );
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
                throw new ModelException(
                  "Could not locate the parameter implemetation for class: '"
                     + classname + "'.", e );
            }
        }
    }
}
