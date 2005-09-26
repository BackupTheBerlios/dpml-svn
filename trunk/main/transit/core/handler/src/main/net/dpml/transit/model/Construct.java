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

import java.beans.Expression;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;

import net.dpml.transit.util.PropertyResolver;

/**
 * A object resolvable from primative arguments.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Construct implements Value
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Utility operation that consolidates an array of values and supplimentary
    * arguments to an array of objects.
    * 
    * @param map a map of keys and values used in symbolic target resolution
    * @param params the value array
    * @param args supplimentary arguments
    * @return the consolidated argument array
    */
    public static Object[] getArgs( Map map, Value[] params, Object[] args )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i < params.length; i++ )
        {
            Value value = params[i];
            Object object = value.resolve( map, null );
            list.add( object );
        }
        for( int i=0; i < args.length; i++ )
        {
            Object value = args[i];
            list.add( value );
        }
        return list.toArray();
    }
    
    private final String m_method;
    private final String m_target;
    private final String m_value;
    private final Value[] m_args;
    
    private final transient boolean m_simple;

   /**
    * Create a new construct using the default java.lang.String class as the base type.
    * @param value the construct value
    */
    public Construct( String value )
    {
        this( String.class.getName(), value );
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it resolved relative to a context map supplied by the 
    * application resolving construct values.
    *
    * @param target a classname or symbolic reference
    * @param value the construct value
    */
    public Construct( String target, String value )
    {
        this( target, "new", value );
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values.  If the construct value is symbolic
    * the implementation will attempt to expand the reference relative to a context
    * map (if supplied) otherwise the implementation will attempt to expand the value 
    * using system properties.
    *
    * @param target a classname or symbolic reference
    * @param method the method to invoke on the target
    * @param value the construct value
    */
    public Construct( String target, String method, String value )
    {
        if( null == target )
        {
            throw new NullPointerException( "target" );
        }
        if( null == method )
        {
            throw new NullPointerException( "method" );
        }
        m_target = target;
        m_method = method;
        m_value = value;
        m_args = new Value[0];
        m_simple = true;
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values. Instance values resolved from the 
    * supplied Value[] will be used as constructor arguments when resolving the target.
    *
    * @param target the construct classname
    * @param args an array of unresolved parameter values
    */
    public Construct( String target, Value[] args )
    {
        this( target, "new", args );
    }
    
   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values. Instance values resolved from the 
    * supplied Value[] will be used as method arguments when resolving the target.
    *
    * @param target the construct classname
    * @param method the method to invoke on the target
    * @param args an array of unresolved parameter values
    */
    public Construct( String target, String method, Value[] args )
    {
        if( null ==target )
        {
            throw new NullPointerException( "target" );
        }
        if( null == method )
        {
            throw new NullPointerException( "method" );
        }
        if( null == args )
        {
            throw new NullPointerException( "args" );
        }
        m_target = target;
        m_method = method;
        m_args = args;
        m_value = null;
        m_simple = false;
    }

   /**
    * Return the method name to be applied to the target object.
    * @return the method name
    */
    public String getMethodName()
    {
        return m_method;
    }

   /**
    * Return the set of nested values within this value.
    * @return the nested values array
    */
    public Value[] getValues()
    {
        return m_args;
    }

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    public String getBaseValue()
    {
        return m_value;
    }

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    public String getTargetExpression()
    {
        return m_target;
    }

   /**
    * Resolve an instance from the value using the context classloader.
    * @return the resolved instance
    */
    public Object resolve()
    {
        return resolve( null );
    }

   /**
    * Resolve an instance from the value.
    * @return the resolved instance
    */
    public Object resolve( ClassLoader classloader )
    {
        return resolve( null, classloader );
    }
    
   /**
    * Resolve an instance from the value.
    * @return the resolved instance
    */
    public Object resolve( Map map, ClassLoader classloader )
    {
        ClassLoader loader = resolveClassLoader( classloader );
        try
        {
            Expression expression = buildExpression( map, loader );    
            return expression.getValue();
        }
        catch ( Throwable e )
        {
            final String error =
              "Unable to construct a value from the base class ["
              + m_target
              + "].";
            throw new ValueRuntimeException( error, e );
        }
    }
    
    private Expression buildExpression( Map map, ClassLoader classloader )
    {
        if( m_simple )
        {
            return buildSimpleExpression( map, classloader );
        }
        else
        {
            return buildCompoundExpression( map, classloader );
        }
    }

    private Expression buildCompoundExpression( Map map, ClassLoader classloader )
    {
        Value[] args = getValues();
        Object target = getTargetObject( map, classloader );
        Object[] instances = getInstanceValues( map, classloader, args );
        String method = getMethodName();
        return new Expression( target, method, instances );
    }
    
    private Object[] getInstanceValues( Map map, ClassLoader classloader, Value[] args )
    {
        Object[] instances = new Object[ args.length ];
        for( int i=0; i < args.length; i++ )
        {
            Value value = args[i];
            instances[i] = value.resolve( map, classloader );
        }
        return instances;
    }

    private Expression buildSimpleExpression( Map map, ClassLoader classloader )
    {
        Object target = getTargetObject( map, classloader );
        final String method = getMethodName();
        if( m_value == null )
        {
            return new Expression( target, method, new Object[0] );
        }
        else
        {
            Object value = expandSymbols( map, m_value );
            return new Expression( target, method, new Object[]{ value } );
        }
    }
    
    private Object expandSymbols( Map map, String value )
    {
        if( null == value )
        {
            return null;
        }
        else
        {
            Object object = parseSymbolicValue( map, value );
            if( null != object )
            {
                return object;
            }
            else
            {
                return PropertyResolver.resolve( value );
            }
        }
    }
    
    private Object parseSymbolicValue( Map map, String value )
    {
        if( null == map )
        {
            return null;
        }
        if( value.startsWith( "${" ) && value.endsWith( "}" ) )
        {
            String pre = value.substring( 2 );
            String key = pre.substring( 0, pre.length() -1 );
            if( map.containsKey( key ) )
            {
                return map.get( key );
            }
        }
        return null;
    }

    /**
     * Return the instance class using the context classloader.
     * @return the class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    public Object getTargetObject( Map map, ClassLoader loader )
    {
        if( m_target.startsWith( "${" ) )
        {
            if( null != map )
            {
                String pre = m_target.substring( 2 );
                String key = pre.substring( 0, pre.length() -1 );
                if( map.containsKey( key ) )
                {
                    return map.get( key );
                }
            }
            final String error = 
              "Unresolvable target symbolic expression ["
              + m_target
              + "].";
            throw new ValueRuntimeException( error );
        }
        else
        {
            return resolveClass( loader, m_target );
        }
    }

    /**
     * Return the instance class using the context classloader.
     * @return the class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    public Class resolveClass( ClassLoader loader, String classname )
    {
        try
        {
            return loader.loadClass( classname );
        }
        catch( final ClassNotFoundException e )
        {
            if( classname.equals( "int" ) )
            {
                return Integer.class;
            }
            else if( classname.equals( "short" ) )
            {
                return Short.class;
            }
            else if( classname.equals( "long" ) )
            {
                return Long.class;
            }
            else if( classname.equals( "byte" ) )
            {
                return Byte.class;
            }
            else if( classname.equals( "double" ) )
            {
                return Double.class;
            }
            else if( classname.equals( "float" ) )
            {
                return Float.class;
            }
            else if( classname.equals( "char" ) )
            {
                return Character.class;
            }
            else if( classname.equals( "char" ) )
            {
                return char.class;
            }
            else if( classname.equals( "boolean" ) )
            {
                return Boolean.class;
            }
            else
            {
                final String error =
                  "Class not found ["
                  + classname 
                  + "].";
               throw new ValueRuntimeException( error, e );
            }
        }
    }

    private ClassLoader resolveClassLoader( ClassLoader classloader )
    {
        if( null != classloader )
        {
            return classloader;
        }
        else
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
    }
}
