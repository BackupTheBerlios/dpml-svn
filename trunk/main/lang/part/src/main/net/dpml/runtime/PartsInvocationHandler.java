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
import java.beans.Expression;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.TypeVariable;

import net.dpml.annotation.Parts;

import net.dpml.lang.Strategy;

/**
 * Invocation handler utility for a Context class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class PartsInvocationHandler implements InvocationHandler
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
    public static boolean isaParts( final Class<?> c, final boolean policy )
    {
        boolean flag = c.isAnnotationPresent( Parts.class );
        if( flag )
        {
            return c.getAnnotation( Parts.class ).value();
        }
        else if( policy )
        {
            return false;
        }
        else
        {
            return c.getName().endsWith( "$Parts" );
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
      final Class<T> clazz, final PartsDirective directive )
    {
        ClassLoader classloader = clazz.getClassLoader();
        PartsInvocationHandler handler = 
          new PartsInvocationHandler( directive );
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
    private final PartsDirective m_directive;
    
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a parts invocation handler.
    *
    * @param directive the parts directive
    */
    private PartsInvocationHandler( final PartsDirective directive )
    {
        m_directive = directive;
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
        String name = method.getName();
        if( name.startsWith( "get" ) )
        {
            Class<?> clazz = method.getReturnType();
            if( clazz.isArray() )
            {
                // build an array of all parts matching the array type
                Class<?> type = clazz.getComponentType();
                if( null == args )
                {
                    return m_directive.select( type );
                }
                else
                {
                    Class<?> c = getClassArgument( args );
                    return m_directive.select( type, c );
                }
            }
            else
            {
                String key = Introspector.decapitalize( name.substring( 3 ) );
                Strategy strategy = m_directive.getStrategy( key );
                if( null != strategy )
                {
                    if( ( null != args ) && ( args.length > 0 ) && ( args[0].getClass() == Class.class ) )
                    {
                        return strategy.getInstance( (Class) args[0] );
                    }
                    else
                    {
                        return strategy.getInstance( clazz );
                    }
                }
                if( ( null != args ) && ( args.length > 0 ) )
                {
                    return args[0];
                }
                else
                {
                    final String error = 
                      "Unable to resolve a parts entry value for the key [" 
                      + key
                      + "]";
                    throw new IllegalStateException( error );
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException( name );
        }
    }
    
    private static final String STRICT_PROCESSING_KEY = "dpml.lang.parts.policy";
    private static final String STRICT_PROCESSING_KEYWORD = "strict";
    private static final boolean STRICT_PROCESSING = getStrictProcessingPolicy();
    
    private static boolean getStrictProcessingPolicy()
    {
        return STRICT_PROCESSING_KEYWORD.equalsIgnoreCase( 
          System.getProperty( 
            STRICT_PROCESSING_KEY, 
            STRICT_PROCESSING_KEYWORD ) );
    }
    
    private Class<?> getClassArgument( Object[] args )
    {
        if( null == args )
        {
            throw new NullPointerException( "args" ); // will not happen
        }
        else if( args.length == 0 )
        {
            throw new IllegalArgumentException( "args" ); // will not happen
        }
        else
        {
            Object arg = args[0];
            if( arg.getClass() == Class.class )
            {
                return (Class) arg;
            }
            else
            {   
                String classname = arg.getClass().getName();
                final String error = 
                  "Expacting a class parameter but found an integer of [" 
                  + classname
                  + "].";
                throw new IllegalArgumentException( error );
            }
        }
    }
}
