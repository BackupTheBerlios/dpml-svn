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

package net.dpml.runtime;

import java.util.Map;
import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.dpml.annotation.Context;
import net.dpml.runtime.Directive.Resolvable;

import org.w3c.dom.Element;

/**
 * Invocation handler utility for a Context class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class ContextInvocationHandler implements InvocationHandler
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
    public static <T>T getProxiedInstance( 
      final Class<T> clazz, final ComponentStrategy strategy, final ContextModel model )
    {
        ClassLoader classloader = clazz.getClassLoader();
        ContextInvocationHandler handler = 
          new ContextInvocationHandler( strategy, model );
        final Object instance = 
          Proxy.newProxyInstance( classloader, new Class[]{clazz}, handler );
        return clazz.cast( instance );
    }
    
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * Context datatype.
    */
    private final ContextModel m_model;
    
   /**
    * Component strategy handler.
    */
    private final ComponentStrategy m_strategy;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param map a map containing context key/value pairs.
    */
    private ContextInvocationHandler( final ComponentStrategy strategy, final ContextModel model )
    {
        m_model = model;
        m_strategy = strategy;
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
                Class<?> clazz = method.getReturnType();
                String key = Introspector.decapitalize( name.substring( 3 ) );
                Resolvable entry = m_model.getResolvable( key );
                if( null != entry )
                {
                    Class nc = normalize( clazz );
                    Object v = resolve( entry, key, nc );
                    if( nc.isAssignableFrom( v.getClass() ) )
                    {
                        return v;
                    }
                    else
                    {
                        final String error = 
                          "Cannot cast [" 
                          + v.getClass().getName()
                          + "] to the return type ["
                          + clazz.getName()
                          + "] on the context entry ["
                          + key
                          + "]";
                        Element element = entry.getElement();
                        throw new ComponentException( error, null, element );
                    }
                }
                if( ( null != args ) && ( args.length > 0 ) )
                {
                    return args[0];
                }
                else
                {
                    // should not happen
                    
                    final String error = 
                      "Unresolvable context entry [" 
                      + key
                      + "] in the component model ["
                      + m_strategy.getComponentPath()
                      + "].";
                    throw new ComponentError( error );
                }
            }
            else
            {
                throw new UnsupportedOperationException( name );
            }
        }
    }
    
    private Object resolve( Resolvable entry, String key, Class<?> c ) throws ComponentException
    {
        try
        {
            return entry.resolve( m_strategy, c );
        }
        catch( ClassCastException e )
        {
            final String error = 
              "Failed to resolve the value for the context entry ["
              + key
              + "] in the component ["
              + m_strategy.getComponentPath()
              + "] for the return type ["
              + c.getName()
              + "]";
            throw new ComponentException( error, e, entry.getElement() );
        }
        catch( Exception e )
        {
            final String error = 
              "Failed to resolve the value for the context entry ["
              + key
              + "] in the component ["
              + m_strategy.getComponentPath()
              + "]";
            throw new ComponentException( error, e, entry.getElement() );
        }
    }
    
    private static boolean isAssignableFrom( Class<?> clazz, Class<?> c )
    {
        Class<?> normalized = normalize( clazz );
        return normalized.isAssignableFrom( c );
    }
    
    private static Class<?> normalize( Class<?> clazz )
    {
        if( clazz.isPrimitive() )
        {
            if( Integer.TYPE == clazz )
            {
                return Integer.class;
            }
            else if( Boolean.TYPE == clazz )
            {
                return Boolean.class;
            }
            else if( Byte.TYPE == clazz )
            {
                return Byte.class;
            }
            else if( Short.TYPE == clazz )
            {
                return Short.class;
            }
            else if( Long.TYPE == clazz )
            {
                return Long.class;
            }
            else if( Float.TYPE == clazz )
            {
                return Float.class;
            }
            else if( Character.TYPE == clazz )
            {
                return Character.class;
            }
            else if( Double.TYPE == clazz )
            {
                return Double.class;
            }
            else
            {
                final String error =
                  "Primitive type ["
                  + clazz.getName()
                  + "] not supported.";
                throw new RuntimeException( error );
            }
        }
        else
        {
            return clazz;
        }
    }
    
    private static boolean isOptionalEntry( Method method )
    {
        return method.getParameterTypes().length == 1;
    }
    
    static String getKeyForMethod( Method method )
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
