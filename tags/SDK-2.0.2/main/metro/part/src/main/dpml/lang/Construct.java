/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.lang;

import dpml.util.PropertyResolver;

import java.beans.Expression;
import java.io.Serializable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import net.dpml.lang.Buffer;

/**
 * A object resolvable from primitive arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Construct implements Value, Serializable
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
    * @exception Exception if an error occurs in argument resolution
    */
    public static Object[] getArgs( Map<String,Object> map, Value[] params, Object[] args ) throws Exception
    {
        ArrayList<Object> list = new ArrayList<Object>();
        if( null != params )
        {
            for( int i=0; i < params.length; i++ )
            {
                Value value = params[i];
                Object object = value.resolve( map );
                if( null != object )
                {
                    list.add( object );
                }
            }
        }
        if( null != args )
        {
            for( int i=0; i < args.length; i++ )
            {
                Object value = args[i];
                if( null != value )
                {
                    list.add( value );
                }
            }
        }
        return list.toArray();
    }
    
    private final String m_method;
    private final String m_target;
    private final String m_value;
    private final Value[] m_args;
    private final boolean m_compound;
    
   /**
    * Create a new construct using the default java.lang.String class as the base type.
    * @param value the construct value
    */
    public Construct( String value )
    {
        this( null, null, value );
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
        this( target, null, value );
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
        m_target = target;
        m_method = method;
        m_value = value;
        m_args = new Value[0];
        m_compound = false;
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
        this( target, null, args );
    }

   /**
    * Create a new construct using a supplied target definition.  The target argument 
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
        if( null == args )
        {
            m_args = new Value[0];
        }
        else
        {
            m_args = args;
        }
        m_value = null;
        m_target = target;
        m_method = method;
        m_compound = true;
    }
    
   /**
    * Encode the construct to XML.
    * @param buffer the buffer to encode to
    * @exception IOException if an IO error occurs
    */
    public void encode( Buffer buffer ) throws IOException
    {
        encode( buffer, "param", null );
    }
    
   /**
    * Encode the construct to XML.
    * @param buffer the buffer to encode to
    * @param label the element name
    * @param key the key associated with the value
    * @exception IOException if an IO error occurs
    */
    public void encode( Buffer buffer, String label, String key ) throws IOException
    {
        buffer.nl( "<" + label );
        if( null != key )
        {
            buffer.write( " key=\"" + key + "\"" );
        }
        if( null != getTargetExpression() )
        {
            buffer.write( " class=\"" + getTargetExpression() + "\"" );
        }
        if( null != getMethodName() )
        {
            buffer.write( " method=\"" + getMethodName()  + "\"" );
        }
        if( isCompound() )
        {
            buffer.write( ">" );
            Value[] values = getValues();
            Buffer b2 = buffer.indent();
            for( Value v : values )
            {
                v.encode( b2 );
            }
            buffer.nl( "</" + label + ">" );
        }
        else
        {
            String v = getBaseValue();
            if( null != v )
            {
                buffer.write( " value=\"" + v  + "\"" );
            }
            buffer.write( "/>" );
        }
    }
    
   /**
    * Return TRUE if this construct is a compound construct else FALSE.
    * @return TRUE if this ia a compound construct
    */
    public boolean isCompound()
    {
        return m_compound;
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
    * @exception Exception if an error occurs during value resolution
    */
    public Object resolve() throws Exception
    {
        return resolve( null );
    }
    
   /**
    * Resolve an instance from the value using a supplied map.
    * @param map the context map
    * @return the resolved instance
    * @exception Exception if an error occurs during value resolution
    */
    public Object resolve( Map<String,Object> map ) throws Exception
    {
        return resolve( null, map );
    }
    
   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param type the implicit target class
    * @param map the context map
    * @return the resolved instance
    * @exception Exception if an error occurs during value resolution
    */
    public Object resolve( Class<?> type, Map<String,Object> map ) throws Exception
    {
        return resolve( type, map, null );
    }

   /**
    * Resolve an instance from the value.
    * @param type the implicit target class
    * @param map the context map
    * @param classloader the classloader
    * @return the resolved instance
    * @exception Exception if an error occurs during value resolution
    */
    private Object resolve( 
      Class type, Map<String,Object> map, ClassLoader classloader ) throws Exception
    {
        ClassLoader loader = resolveClassLoader( classloader );
        Class t = getTargetClass( loader, type );
        if( isCompound() )
        {
            return resolveCompoundExpression( t, map, loader );
        }
        else
        {
            return resolveSimpleExpression( t, map, loader );
        }
    }
    
    private Object resolveSimpleExpression( 
      Class type, Map map, ClassLoader classloader ) throws Exception
    {
        Properties properties = System.getProperties();
        String value = PropertyResolver.resolve( properties, m_value );
        
        if( ( null != value ) && ( null != map ) )
        {
            if( value.startsWith( "${" ) && m_value.endsWith( "}" ) )
            {
                String pre = m_value.substring( 2 );
                String key = pre.substring( 0, pre.length() -1 );
                if( map.containsKey( key ) )
                {
                    return map.get( key );
                }
            }
        }
        
        String method = getMethodName();
        if( null == method )
        {
            method = "new";
        }
        else
        {
            if( null == value ) // check if the method name is a static field
            {
                try
                {
                    Field field = type.getField( method );
                    return field.get( type );
                }
                catch( NoSuchFieldException e )
                {
                    // assume its a method
                }
            }
        }
        
        if( null == value )
        {
            Expression expression = new Expression( type, method, new Object[0] );
            try
            {
                return expression.getValue();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while evalating simple expression using:"
                  + "\n target: " 
                  + m_target 
                  + " (" 
                  + type.getName() 
                  + ")"
                  + "\n method: " 
                  + m_method 
                  + " (" 
                  + method 
                  + ")";
                throw new ValueException( error, e );
            }
        }
        else
        {
            Expression expression =  new Expression( type, method, new Object[]{value} );
            try
            {
                return expression.getValue();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while evaluating expression using:"
                  + "\n target: " + m_target + " (" + type.getName() + ")"
                  + "\n method: " + m_method + " (" + method + ")"
                  + "\n value: " + m_value + " (" + value.getClass().getName() + ")";
                throw new ValueException( error, e );
            }
        }
    }
    
    private Object resolveCompoundExpression( 
      Class<?> type, Map<String,Object> map, ClassLoader classloader ) throws Exception
    {
        Value[] args = getValues();
        Object[] instances = getInstanceValues( type, map, classloader, args );
        String method = getMethodName();
        
        //
        // check if we are dealing with an array class and if so return an 
        // array created from the array of nested values
        //
        
        if( type.isArray() )
        {
            Class<?> t = type.getComponentType();
            if( t.isPrimitive() )
            {
                return buildPrimitiveArray( t, instances );
            }
            else
            {
                Object[] result = 
                  (Object[]) Array.newInstance( t, instances.length );
                for( int i=0; i<instances.length; i++ )
                {
                    Object instance = instances[i];
                    Class<?> clazz = instance.getClass();
                    if( t.isAssignableFrom( clazz ) )
                    {
                        result[i] = instances[i];
                    }
                    else
                    {
                        final String error =
                          "Array [" 
                          + t.getName() 
                          + "] contains an invalid element [" 
                          + instance.getClass().getName() 
                          + "].";
                        throw new ValueException( error );
                    }
                }
                return result;
            }
        }
        
        // otherwise its a regular expression
        
        if( null == method )
        {
            method = "new";
        }
        else
        {
            if( instances.length == 0 )
            {
                // check if the method name is a static field
                try
                {
                    Field field = type.getField( method );
                    return field.get( type );
                }
                catch( NoSuchFieldException e )
                {
                    // assume its a method
                }
            }
        }

        Expression expression = new Expression( type, method, instances );
        try
        {
            return expression.getValue();
        }
        catch( Throwable e )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "Internal error while evaluating compound expression." );
            buffer.append( "\n target: " + m_target + " (" + type.getName() + ")" );
            buffer.append( "\n method: " + m_method + " (" + method + ")" );
            for( int i=0; i<instances.length; i++ )
            {
                buffer.append( 
                  "\n param " 
                  + ( i+1 ) 
                  + ": " 
                  + instances[i].getClass().getName()
                );
            }
            String error = buffer.toString();
            throw new ValueException( error, e );
        }
    }

    private Object buildPrimitiveArray( Class type, Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( type, instances.length );
        if( Integer.TYPE == type )
        {
            return buildIntArray( instances );
        }
        else if( Short.TYPE == type )
        {
            return buildShortArray( instances );
        }
        else if( Long.TYPE == type )
        {
            return buildLongArray( instances );
        }
        else if( Byte.TYPE == type )
        {
            return buildByteArray( instances );
        }
        else if( Double.TYPE == type )
        {
            return buildDoubleArray( instances );
        }
        else if( Float.TYPE == type )
        {
            return buildFloatArray( instances );
        }
        else if( Character.TYPE == type )
        {
            return buildCharacterArray( instances );
        }
        else if( Boolean.TYPE == type )
        {
            return buildBooleanArray( instances );
        }
        else
        {
            final String error = 
              "Primitive array class [" 
              + type.getName() 
              + "] is not recognized.";
            throw new UnsupportedOperationException( error );
        }
    }
    
    private Object buildIntArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Integer.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Integer )
            {
                Integer integer = (Integer) instance;
                int v = integer.intValue();
                Array.setInt( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied int array argument class ["
                  + instance.getClass().getName()
                  + "] is not an Integer.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildShortArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Short.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Short )
            {
                Short primitive = (Short) instance;
                short v = primitive.shortValue();
                Array.setShort( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied short array argument class ["
                  + instance.getClass().getName()
                  + "] is not an Short.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildLongArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Long.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Long )
            {
                Long primitive = (Long) instance;
                long v = primitive.longValue();
                Array.setLong( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied long array argument class ["
                  + instance.getClass().getName()
                  + "] is not an instance of Long.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildByteArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Byte.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Byte )
            {
                Byte primitive = (Byte) instance;
                byte v = primitive.byteValue();
                Array.setByte( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied byte array argument class ["
                  + instance.getClass().getName()
                  + "] is not an instance of Byte.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildDoubleArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Double.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Double )
            {
                Double primitive = (Double) instance;
                double v = primitive.doubleValue();
                Array.setDouble( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied double array argument class ["
                  + instance.getClass().getName()
                  + "] is not an instance of Double.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildFloatArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Float.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Float )
            {
                Float primitive = (Float) instance;
                float v = primitive.floatValue();
                Array.setFloat( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied float array argument class ["
                  + instance.getClass().getName()
                  + "] is not an instance of Float.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildCharacterArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Character.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Character )
            {
                Character primitive = (Character) instance;
                char v = primitive.charValue();
                Array.setChar( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied char array argument class ["
                  + instance.getClass().getName()
                  + "] is not an instance of Character.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object buildBooleanArray( Object[] instances ) throws ValueException
    {
        Object result = Array.newInstance( Boolean.TYPE, instances.length );
        for( int i=0; i<instances.length; i++ )
        {
            Object instance = instances[i];
            if( instance instanceof Boolean )
            {
                Boolean primitive = (Boolean) instance;
                boolean v = primitive.booleanValue();
                Array.setBoolean( result, i, v );
            }
            else
            {
                final String error = 
                  "Supplied boolean array argument class ["
                  + instance.getClass().getName()
                  + "] is not an instance of Boolean.";
                throw new ValueException( error );
            }
        }
        return result;
    }
    
    private Object[] getInstanceValues( 
      Class type, Map<String,Object> map, ClassLoader classloader, Value[] args ) throws Exception
    {
        Object[] instances = new Object[ args.length ];
        for( int i=0; i < args.length; i++ )
        {
            Value value = args[i];
            if( value instanceof Construct )
            {
                Construct construct = (Construct) value;
                if( type.isArray() )
                {
                    Class<?> t = type.getComponentType();
                    instances[i] = construct.resolve( t, map, classloader );
                }
                else
                {
                    instances[i] = construct.resolve( null, map, classloader );
                }
            }
            else
            {
                if( type.isArray() )
                {
                    Class<?> t = type.getComponentType();
                    instances[i] = value.resolve( t, map );
                }
                else
                {
                    instances[i] = value.resolve( null, map );
                }
            }
        }
        return instances;
    }

    private Class getTargetClass( ClassLoader loader, Class type ) 
      throws ValueException
    {
        if( null == m_target )
        {
            if( null == type )
            {
                return String.class;
            }
            else
            {
                if( type.isPrimitive() )
                {
                    if( type == Integer.TYPE )
                    {
                        return Integer.class;
                    }
                    else if( type == Short.TYPE )
                    {
                        return Short.class;
                    }
                    else if( type == Long.TYPE )
                    {
                        return Long.class;
                    }
                    else if( type == Byte.TYPE )
                    {
                        return Byte.class;
                    }
                    else if( type == Double.TYPE )
                    {
                        return Double.class;
                    }
                    else if( type == Float.TYPE )
                    {
                        return Float.class;
                    }
                    else if( type == Character.TYPE )
                    {
                        return Character.class;
                    }
                    else if( type == Boolean.TYPE )
                    {
                        return Boolean.class;
                    }
                    else
                    {
                        return Void.class;
                    }
                }
                else
                {
                    return type;
                }
            }
        }
        else
        {
            if( m_target.endsWith( "[]" ) )
            {
                int n = m_target.length() - 2;
                String componentClassname = m_target.substring( 0, n );
                Class componentClass = resolveType( loader, componentClassname );
                return Array.newInstance( componentClass, 0 ).getClass();
            }
            else
            {
                return resolveClass( loader, m_target );
            }
        }
    }

    /**
     * Return the instance class using the context classloader.
     * @return the class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    private Class resolveClass( ClassLoader loader, String classname ) throws ValueException
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
        else if( classname.equals( "boolean" ) )
        {
            return Boolean.class;
        }
        else
        {
            try
            {
                return Class.forName( classname, false, loader );
            }
            catch( final ClassNotFoundException cnfe )
            {
                final String error =
                  "Value class not found ["
                  + classname 
                  + "].";
                throw new ValueException( error, cnfe );
            }
        }
    }

    /**
     * Return the instance class using the context classloader.
     * @return the class
     * @exception ComponentException if the parameter class cannot be resolved
     */
    private Class resolveType( ClassLoader loader, String classname ) throws ValueException
    {
        if( classname.equals( "int" ) )
        {
            return Integer.TYPE;
        }
        else if( classname.equals( "short" ) )
        {
            return Short.TYPE;
        }
        else if( classname.equals( "long" ) )
        {
            return Long.TYPE;
        }
        else if( classname.equals( "byte" ) )
        {
            return Byte.TYPE;
        }
        else if( classname.equals( "double" ) )
        {
            return Double.TYPE;
        }
        else if( classname.equals( "float" ) )
        {
            return Float.TYPE;
        }
        else if( classname.equals( "char" ) )
        {
            return Character.TYPE;
        }
        else if( classname.equals( "boolean" ) )
        {
            return Boolean.TYPE;
        }
        else
        {
            try
            {
                return loader.loadClass( classname );
            }
            catch( final ClassNotFoundException e )
            {
                final String error =
                  "Class not found ["
                  + classname 
                  + "].";
               throw new ValueException( error, e );
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
            if( null != loader )
            {
                return loader;
            }
            else
            {
                return Construct.class.getClassLoader();
            }
        }
    }
    
   /**
    * Return a string representation of the construct.
    * @return the string value
    */
    public String toString()
    {
        if( !m_compound )
        {
            return "construct "
              + " target: " + m_target 
              + " method: " + m_method 
              + " value: " + m_value;
        }
        else
        {
            return "construct "
              + " target: " + m_target 
              + " method: " + m_method 
              + " values: " + m_args.length;
        }
    }
    
   /**
    * Compare this instance with a supplied object for equality.
    * @param other the other object
    * @return true if the supplied instance is equal to this instance
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        if( other instanceof Construct )
        {
            Construct construct = (Construct) other;
            if( !equals( m_target, construct.m_target ) )
            {
                return false;
            }
            if( m_compound != construct.m_compound )
            {
                return false;
            }
            if( !equals( m_method, construct.m_method ) )
            {
                return false;
            }
            if( m_compound )
            {
                return Arrays.equals( m_args, construct.m_args );
            }
            else
            {
                return equals( m_value, construct.m_value );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the instance hashcode value.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = 0;
        if( null != m_target )
        {
            hash ^= m_target.hashCode();
        }
        if( null != m_method )
        {
            hash ^= m_method.hashCode();
        }
        if( m_compound )
        {
            for( int i=0; i<m_args.length; i++ )
            {
                hash ^= m_args[i].hashCode();
            }
        }
        else
        {
            if( m_value != null )
            {
                hash ^= m_value.hashCode();
            }
        }
        return hash;
    }
    
    private boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return ( null == b );
        }
        else
        {
            return a.equals( b );
        }
    }
}
