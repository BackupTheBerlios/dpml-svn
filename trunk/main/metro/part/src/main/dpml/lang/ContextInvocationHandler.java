/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import java.util.Map;
import java.beans.Introspector;
import java.beans.Expression;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.dpml.annotation.Context;

import dpml.lang.Value;

/**
 * Invocation handler utility for a Context class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ContextInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------
    
    private static final String GET = "get";

   /**
    * Test if the supplied class is recognized as a context class. If
    * the supplied policy is true then evaluation is based on the presence
    * of the <tt>net.dpml.lang.Context</tt> annotation and resulting 
    * value of annotation evaluation.  If no context annnotation is present
    * and strict processing is disabled then evaluation will return true if 
    * the classname ends with the string <tt>$Context</tt>.
    *
    * @param c the candidate class
    * @param policy the strict evaluation policy
    * @return true if this class is recognized as a context class
    */
    public static boolean isaContext( final Class<?> c, final boolean policy )
    {
        boolean flag = c.isAnnotationPresent( Context.class );
        if( flag )
        {
            return c.getAnnotation( Context.class ).value();
        }
        else if( policy )
        {
            return false;
        }
        else
        {
            return c.getName().endsWith( "$Context" );
        }
    }
    
   /**
    * Validate that the supplied map properly fulfills the required  
    * context entry.  Specifically, for all non-optional methods declared
    * by the context class, the validation process checks the the supplied
    * map has a corresponding key and associated non-null value.
    *
    * @param clazz the context class
    * @param map a map of context entry values
    * @exception IllegalArgumentException if validation fails
    */
    public static void validate( Class clazz, Map<String,Object> map ) 
      throws IllegalArgumentException
    {
        for( Method method : clazz.getMethods() )
        {
            String key = getKeyForMethod( method );
            boolean optional = isOptionalEntry( method );
            if( !optional )
            {
                if( !map.containsKey( key ) )
                {
                    final String error = 
                      "Missing required key ["
                      + key
                      + "]";
                    throw new IllegalArgumentException( error );
                }
                else if( null == map.get( key ) )
                {
                    final String error = 
                      "Missing value for the required key ["
                      + key
                      + "]";
                    throw new IllegalArgumentException( error );
                }
            }
        }
    }
    
   /**
    * Construct a new context instance implementing the supplied class
    * backed by entries in the supplied map.
    *
    * @param clazz the context class
    * @param map a map of context entry keys and values
    * @return the constructed context instance
    */
    public static <T>T getProxiedInstance( final Class<T> clazz, final Map<String,Object> map )
    {
        ClassLoader classloader = clazz.getClassLoader();
        ContextInvocationHandler handler = 
          new ContextInvocationHandler( map );
        final Object instance = 
          Proxy.newProxyInstance( classloader, new Class[]{clazz}, handler );
        return clazz.cast( instance );
    }
    
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * A map containing key values.
    */
    private final Map<String,Object> m_map;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param map a map containing context key/value pairs.
    */
    private ContextInvocationHandler( final Map<String,Object> map )
    {
        if( null == map )
        {
            throw new NullPointerException( "map" );
        }
        m_map = map;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

   /**
    * Invoke the specified method on underlying object.
    * This is called by the proxy object.
    *
    * @param proxy the proxy object
    * @param method the method invoked on proxy object
    * @param args the arguments supplied to method
    * @return the return value of method
    * @throws Throwable if an error occurs
    */
    @SuppressWarnings( "unchecked" )
    public Object invoke( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        Class source = method.getDeclaringClass();
        if( Object.class == source )
        {
            return method.invoke( this, args );
        }
        else
        {
            String name = method.getName();
            if( name.startsWith( "get" ) )
            {
                String key = Introspector.decapitalize( name.substring( 3 ) );
                Object value = m_map.get( key );
                if( null != value )
                {
                    Class<?> clazz = method.getReturnType();
                    if( isAssignableFrom( clazz, value.getClass() ) )
                    {
                        return value;
                    }
                    else if( isaContext( clazz, STRICT_PROCESSING ) )
                    {
                        if( value instanceof Map )
                        {
                            Map m = (Map) value;
                            return getProxiedInstance( clazz, m );
                        }
                        else
                        {
                            final String error = 
                              "Supplied context solution value [" 
                              + value.getClass().getName() 
                              + "] is not an instance of java.util.Map.";
                            throw new IllegalStateException( error );
                        }
                    }
                    else if( value instanceof Value )
                    {
                        Value v = (Value) value;
                        return v.resolve( clazz, (Map) null );
                    }
                    else
                    {
                        Expression expression = new Expression( clazz, "new", new Object[]{value} );
                        return expression.getValue();
                    }
                }
                else if( ( null != args ) && args.length > 0 )
                {
                    return args[0];
                }
                else
                {
                    final String error = 
                      "Unable to resolve a context entry value for the key [" 
                      + key
                      + "]";
                    throw new IllegalStateException( error );
                }
            }
            else
            {
                throw new UnsupportedOperationException( name );
            }
        }
    }
    
    private static boolean isAssignableFrom( Class<?> clazz, Class<?> c )
    {
        if( clazz.isPrimitive() )
        {
            if( Integer.TYPE == clazz )
            {
                return Integer.class.isAssignableFrom( c );
            }
            else if( Boolean.TYPE == clazz )
            {
                return Boolean.class.isAssignableFrom( c );
            }
            else if( Byte.TYPE == clazz )
            {
                return Byte.class.isAssignableFrom( c );
            }
            else if( Short.TYPE == clazz )
            {
                return Short.class.isAssignableFrom( c );
            }
            else if( Long.TYPE == clazz )
            {
                return Long.class.isAssignableFrom( c );
            }
            else if( Float.TYPE == clazz )
            {
                return Float.class.isAssignableFrom( c );
            }
            else if( Double.TYPE == clazz )
            {
                return Double.class.isAssignableFrom( c );
            }
            else
            {
                final String error =
                  "Primitive type ["
                  + c.getName()
                  + "] not supported.";
                throw new RuntimeException( error );
            }
        }
        else
        {
            return clazz.isAssignableFrom( c );
        }
    }
    
    private static boolean isOptionalEntry( Method method )
    {
        return method.getParameterTypes().length == 1;
    }
    
    private static String getKeyForMethod( Method method )
    {
        String name = method.getName();
        if( name.startsWith( GET ) )
        {
            String remainder = name.substring( GET.length() );
            return Introspector.decapitalize( remainder );
        }
        else
        {
            final String error = 
              "Method name is not a recognized pattern: " 
              + name;
            throw new IllegalArgumentException( error );
        }
    }
    
    private static final String STRICT_PROCESSING_KEY = "dpml.lang.context.policy";
    private static final String STRICT_PROCESSING_KEYWORD = "strict";
    private static final boolean STRICT_PROCESSING = getStrictProcessingPolicy();
    
    private static boolean getStrictProcessingPolicy()
    {
        return STRICT_PROCESSING_KEYWORD.equalsIgnoreCase( 
          System.getProperty( 
            STRICT_PROCESSING_KEY, 
            STRICT_PROCESSING_KEYWORD ) );
    }
}
