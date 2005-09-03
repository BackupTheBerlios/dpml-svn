/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.model;

import java.lang.reflect.Constructor;

import net.dpml.transit.util.PropertyResolver;

/**
 * A object resolvable from primative arguments.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Construct implements Value
{
    private String m_type;
    private String m_value;
    private Value[] m_args;

   /**
    * Create a new construct using the default java.lang.String class as the base type.
    * @param value the construct value
    */
    public Construct( String value )
    {
        this( String.class.getName(), value );
    }

   /**
    * Create a new construct using a supplied base type.
    * @param type the construct classname
    * @param value the construct value
    */
    public Construct( String type, String value )
    {
        m_type = type;
        m_value = value;
        m_args = null;
    }

   /**
    * Create a new composit construct using a supplied base type and value array.
    * @param type the construct classname
    * @param args an array of constructor values
    */
    public Construct( String type, Value[] args )
    {
        m_type = type;
        m_args = args;
        m_value = null;
    }

   /**
    * Resolve an instance from the value.
    * @return the resolved instance
    */
    public Object resolve()
    {
        ClassLoader loader = resolveClassLoader();
        return resolve( loader );
    }

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    public String getBaseClassname()
    {
        return m_type;
    }

   /**
    * Resolve an instance from the value.
    * @return the resolved instance
    */
    public Object resolve( ClassLoader classloader )
    {
        if( ( null == m_args ) || ( m_args.length == 0 ) )
        {
            return resolveSimpleConstruct( classloader );
        }
        else
        {
            return resolveCompoundConstruct( classloader, m_args );
        }
    }

    private Object resolveCompoundConstruct( ClassLoader classloader, Value[] args )
    {
        Class clazz = getBaseClass( classloader );

        //
        // create an object array based on the spplied args
        // to be uased as a constructor argument
        //

        Object[] instances = getInstanceValues( classloader, args );
        Class[] classes = getClassArray( classloader, args );

        //
        // locate the constructor
        //

        try
        {
            Constructor constructor = clazz.getConstructor( classes );
            return constructor.newInstance( instances );
        }
        catch( NoSuchMethodException e )
        {
            final String error =
              "Construct class ["
              + m_type
              + "] does not declare a matching public constructor.";
            throw new ValueRuntimeException( error, e );
        }
        catch( InstantiationException e )
        {
            final String error =
              "Unable to instantiate a multi-parameter construct ["
              + clazz.getName()
              + "].";
            throw new ValueRuntimeException( error, e );
        }
        catch( IllegalAccessException e )
        {
            final String error =
              "Cannot access multi-parameter construct ["
              + clazz.getName() 
              + "].";
            throw new ValueRuntimeException( error, e );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to instantiate a multi-parameter construct [" 
              + clazz.getName() 
              + "].";
            throw new ValueRuntimeException( error, e );
        }
    }

    private Object[] getInstanceValues( ClassLoader classloader, Value[] args )
    {
        Object[] instances = new Object[ args.length ];
        for( int i=0; i < args.length; i++ )
        {
            Value value = args[i];
            instances[i] = value.resolve( classloader );
        }
        return instances;
    }

    private Class[] getClassArray( ClassLoader classloader, Value[] args )
    {
        Class[] classes = new Class[ args.length ];
        for( int i=0; i < args.length; i++ )
        {
            Value value = args[i];
            classes[i] = value.getBaseClass( classloader );
        }
        return classes;
    }

    private Object resolveSimpleConstruct( ClassLoader classloader )
    {
        Class clazz = getBaseClass( classloader );
        if( m_value == null )
        {
            try
            {
                return clazz.newInstance();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unable to instantiate an instance of ["
                  + m_type
                  + "] using a null argument constructor.";
                throw new ValueRuntimeException( error );
            }
        }
        else
        {
            if( clazz.isPrimitive() )
            {
                return getPrimitiveValue( clazz );
            }
            else
            {
                try
                {
                    final Class[] params = new Class[]{String.class};
                    Constructor constructor = clazz.getConstructor( params );
                    final Object[] values = new Object[]{m_value};
                    return constructor.newInstance( values );
                }
                catch ( NoSuchMethodException e )
                {
                    final String error =
                      "Construct class: [" 
                      + clazz.getName()
                      + "] does not implement a single String argument constructor.";
                    throw new ValueRuntimeException( error );
                }
                catch ( InstantiationException e )
                {
                    final String error =
                      "Unable to instantiate instance of class [" 
                      + clazz.getName()
                      + "] with the single argument ["
                      + m_value 
                      + "].";
                    throw new ValueRuntimeException( error, e );
                }
                catch ( IllegalAccessException e )
                {
                    final String error =
                      "Cannot access single string parameter constructor for the class: ["
                      + clazz.getName() 
                      + "].";
                    throw new ValueRuntimeException( error, e );
                }
                catch ( Throwable e )
                {
                    final String error =
                      "Unexpected exception while creating a single string parameter value for the class ["
                      + clazz.getName() 
                      + "].";
                    throw new ValueRuntimeException( error, e );
                }
            }
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

    /**
     * Return the instance class using the context classloader.
     * @return the class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    public Class getBaseClass( ClassLoader loader )
    {
        try
        {
            return loader.loadClass( m_type );
        }
        catch( final ClassNotFoundException e )
        {
            if( m_type.equals( "int" ) )
            {
                return int.class;
            }
            else if( m_type.equals( "short" ) )
            {
                return short.class;
            }
            else if( m_type.equals( "long" ) )
            {
                return long.class;
            }
            else if( m_type.equals( "byte" ) )
            {
                return byte.class;
            }
            else if( m_type.equals( "double" ) )
            {
                return double.class;
            }
            else if( m_type.equals( "float" ) )
            {
                return float.class;
            }
            else if( m_type.equals( "char" ) )
            {
                return char.class;
            }
            else if( m_type.equals( "char" ) )
            {
                return char.class;
            }
            else if( m_type.equals( "boolean" ) )
            {
                return boolean.class;
            }
            else
            {
                final String error =
                  "Primitive class not found ["
                  + m_type 
                  + "].";
               throw new ValueRuntimeException( error, e );
            }
        }
    }

    private ClassLoader resolveClassLoader()
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if( null == loader )
        {
            return Construct.class.getClassLoader();
        }
        else
        {
            return loader;
        }
    }

    private Object getPrimitiveValue( Class clazz )
    {
        if( Integer.TYPE == clazz )
        {
            return Integer.valueOf( m_value );
        }
        else if( Boolean.TYPE == clazz )
        {
            return Boolean.valueOf( m_value );
        }
        else if( Byte.TYPE == clazz )
        {
            return Byte.valueOf( m_value );
        }
        else if( Short.TYPE == clazz )
        {
            return Short.valueOf( m_value );
        }
        else if( Long.TYPE == clazz )
        {
            return Long.valueOf( m_value );
        }
        else if( Float.TYPE == clazz )
        {
            return Float.valueOf(  m_value );
        }
        else if( Double.TYPE == clazz )
        {
            return Double.valueOf( m_value );
        }
        else
        {
            final String error =
              "The primitive type ["
              + clazz.getName()
              + "] is not recognized.";
            throw new ValueRuntimeException( error );
        }
    }

}
