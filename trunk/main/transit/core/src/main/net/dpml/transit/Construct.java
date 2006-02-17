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

package net.dpml.transit;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import net.dpml.transit.info.ValueDirective;
import net.dpml.transit.util.PropertyResolver;

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
    public static Object[] getArgs( Map map, Value[] params, Object[] args ) throws Exception
    {
        ArrayList list = new ArrayList();
        for( int i=0; i < params.length; i++ )
        {
            Value value = params[i];
            Object object = value.resolve( map );
            if( null != object )
            {
                list.add( object );
            }
        }
        for( int i=0; i < args.length; i++ )
        {
            Object value = args[i];
            if( null != value )
            {
                list.add( value );
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
    * Creation of a new construct using a value directive.
    * @param directive the value directive
    */
    public Construct( ValueDirective directive )
    {
        m_value = directive.getBaseValue();
        m_target = directive.getTargetExpression();
        m_method = directive.getMethodName();
        m_compound = directive.isCompound();
        ValueDirective[] values = directive.getValueDirectives();
        int n = values.length;
        m_args = new Value[ n ];
        for( int i=0; i<n; i++ )
        {
            ValueDirective value = values[i];
            m_args[i] = new Construct( value );
        }
    }
    
   /**
    * Return TRUE if this construct is a compund construct else FALSE.
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
    public Object resolve( Map map ) throws Exception
    {
        return resolve( map, false );
    }
    
   /**
    * Resolve an instance from the value using a supplied isolation policy.
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if an error occurs during value resolution
    */
    public Object resolve( boolean isolate ) throws Exception
    {
        return resolve( null, isolate );
    }
    
   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param map the context map
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    public Object resolve( Map map, boolean isolate ) throws Exception
    {
        return resolve( map, null, isolate );
    }

   /**
    * Resolve an instance from the value.
    * @param map the context map
    * @param classloader the classloader to use
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if an error occurs during value resolution
    */
    public Object resolve( Map map, ClassLoader classloader, boolean isolate ) throws Exception
    {
        ClassLoader loader = resolveClassLoader( classloader );
        Object target = getTargetObject( map, loader );
        if( isCompound() )
        {
            if( null == target )
            {
                throw new NullPointerException( "target" );
            }
            else
            {
                return resolveCompoundExpression( target, map, loader );
            }
        }
        else
        {
            Object value = resolveBaseValue( map );
            if( null == target )
            {
                return value;
            }
            else
            {
                return resolveSimpleExpression( target, map, loader );
            }
        }
    }
    
    private Object resolveBaseValue( Map map )
    {
        return expandSymbols( map, m_value );
    }

    private Object resolveSimpleExpression( Object target, Map map, ClassLoader classloader ) throws Exception
    {
        String method = getMethodName();
        Object value = expandSymbols( map, m_value );
        boolean isaClass = ( target.getClass() == Class.class );
        if( null == method )
        {
            if( isaClass )
            {
                method = "new";
            }
            else
            {
                final String error = 
                  "Target expression '"
                  + m_target
                  + "' resolving to an instance of the class ["
                  + target.getClass() 
                  + "] canot be resolved due to missing method declaration.";
                throw new ValueException( error );
            }
        }
        else
        {
            if( isaClass && ( null == value ) )
            {
                // check if the method name is a static field
                Class c = (Class) target;
                try
                {
                    Field field = c.getField( method );
                    return field.get( c );
                }
                catch( NoSuchFieldException e )
                {
                    // assume its a method
                }
            }
        }
        
        if( value == null )
        {
            Expression expression = new Expression( target, method, new Object[0] );
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
                  + target 
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
            Expression expression =  new Expression( target, method, new Object[]{value} );
            try
            {
                return expression.getValue();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while evaluating expression using:"
                  + "\n target: " + m_target + " (" + target + ")"
                  + "\n method: " + m_method + " (" + method + ")"
                  + "\n value: " + m_value + " (" + value.getClass().getName() + ")";
                throw new ValueException( error, e );
            }
        }
    }
    
    private Object resolveCompoundExpression( Object target, Map map, ClassLoader classloader ) throws Exception
    {
        Value[] args = getValues();
        Object[] instances = getInstanceValues( map, classloader, args );
        String method = getMethodName();
        boolean isaClass = ( target.getClass() == Class.class );
        
        //
        // check if we are dealing with an array class and if so return and 
        // array created from the array of nested values
        //
        
        if( isaClass ) 
        {
            Class c = (Class) target;
            if( c.isArray() )
            {
                Class type = c.getComponentType();
                if( type.isPrimitive() )
                {
                    Object result = Array.newInstance( type, instances.length );
                    if( Integer.TYPE == type )
                    {
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
                    else if( Short.TYPE == type )
                    {
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
                                  + "] is not an instance of Short.";
                                throw new ValueException( error );
                            }
                        }
                        return result;
                    }
                    else if( Long.TYPE == type )
                    {
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
                    else if( Byte.TYPE == type )
                    {
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
                    else if( Double.TYPE == type )
                    {
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
                    else if( Float.TYPE == type )
                    {
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
                    else if( Character.TYPE == type )
                    {
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
                    else if( Boolean.TYPE == type )
                    {
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
                    else
                    {
                        final String error = 
                          "Primitive array class [" 
                          + type.getName() 
                          + "] is not recognized.";
                        throw new UnsupportedOperationException( error );
                    }
                }
                else
                {
                    Object[] result = 
                      (Object[]) Array.newInstance( type, instances.length );
                    for( int i=0; i<instances.length; i++ )
                    {
                        Object instance = instances[i];
                        if( type.isAssignableFrom( instance.getClass() ) )
                        {
                            result[i] = instances[i];
                        }
                        else
                        {
                            final String error =
                              "Array [" 
                              + type.getName() 
                              
                              + "] contains an invalid element [" 
                              + instance.getClass().getName() 
                              + "].";
                            throw new ValueException( error );
                        }
                    }
                    return result;
                }
            }
        }
        
        // otherwise its a regular expression
        
        if( null == method )
        {
            if( isaClass )
            {
                method = "new";
            }
            else
            {
                final String error = 
                  "Missing method declaration in a composite value construct."
                  + "\nTarget: " 
                  + target 
                  + " (" + target.getClass().getName() 
                  + ")";
                throw new ValueException( error );
            }
        }
        else
        {
            if( isaClass && ( instances.length == 0 ) )
            {
                // check if the method name is a static field
                Class c = (Class) target;
                try
                {
                    Field field = c.getField( method );
                    return field.get( c );
                }
                catch( NoSuchFieldException e )
                {
                    // assume its a method
                }
            }
        }
        Expression expression = new Expression( target, method, instances );
        try
        {
            return expression.getValue();
        }
        catch( Throwable e )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "Internal error while evaluating compound expression." );
            buffer.append( "\n target: " + m_target + " (" + target + ")" );
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
    
    private Object[] getInstanceValues( 
      Map map, ClassLoader classloader, Value[] args ) throws Exception
    {
        Object[] instances = new Object[ args.length ];
        for( int i=0; i < args.length; i++ )
        {
            Value value = args[i];
            if( value instanceof Construct )
            {
                Construct construct = (Construct) value;
                instances[i] = construct.resolve( map, classloader, false );
            }
            else
            {
                instances[i] = value.resolve( map );
            }
        }
        return instances;
    }

    private Object expandSymbols( Map map, String value )
    {
        if( null == value )
        {
            return null;
        }
        else
        {
            return parseSymbolicValue( map, value );
        }
    }
    
    private Object parseSymbolicValue( Map map, String value )
    {
        if( null == map )
        {
            return PropertyResolver.resolve( value );
        }
        if( value.startsWith( "${" ) && value.endsWith( "}" ) )
        {
            String pre = value.substring( 2 );
            String key = pre.substring( 0, pre.length() -1 );
            if( map.containsKey( key ) )
            {
                return map.get( key );
            }
            else
            {
                return PropertyResolver.resolve( value );
            }
        }
        else
        {
            return PropertyResolver.resolve( value );
        }
    }
    
    /**
     * Return the instance class using the context classloader.
     * @return the target object or class
     * @exception ValueException if target related error occurs
     */
    private Object getTargetObject( Map map, ClassLoader loader ) throws ValueException
    {
        return getTargetObject( map, loader, m_target );
    }
    
    /**
     * Return the instance class using the context classloader.
     * @return the target object or class
     * @exception ValueException if target related error occurs
     */
    private Object getTargetObject( Map map, ClassLoader loader, String target ) throws ValueException
    {
        if( null == target )
        {
            return null;
        }
        else if( target.startsWith( "${" ) )
        {
            if( null != map )
            {
                String pre = target.substring( 2 );
                String key = pre.substring( 0, pre.length() -1 );
                if( map.containsKey( key ) )
                {
                    return map.get( key );
                }
                else
                {
                    final String error = 
                      "Unresolvable target symbolic expression ["
                      + target
                      + "].";
                    throw new ValueException( error );
                }
            }
            else
            {
                String resolved = PropertyResolver.resolve( target );
                return getTargetObject( map, loader, resolved );
            }
        }
        else
        {
            if( target.endsWith( "[]" ) )
            {
                int n = target.length() - 2;
                String componentClassname = target.substring( 0, n );
                Class componentClass = resolveType( loader, componentClassname );
                return Array.newInstance( componentClass, 0 ).getClass();
            }
            else
            {
                return resolveClass( loader, target );
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
               throw new ValueException( error, e );
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
        try
        {
            return loader.loadClass( classname );
        }
        catch( final ClassNotFoundException e )
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
