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

package net.dpml.util;

import java.util.Map;
import java.beans.Introspector;
import java.beans.Expression;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Invoication handler utility for a Context inner-class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ContextInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------
    
   /**
    * Construct a new context instance implementing the supplied class
    * and backed by entries in the supplied map.
    *
    * @param clazz the context inner class
    * @param map a map of context entry keys to values
    * @return the proxied context instance
    */
    public static Object getProxiedInstance( Class clazz, Map map )
    {
        ClassLoader classloader = clazz.getClassLoader();
        ContextInvocationHandler handler = new ContextInvocationHandler( map );
        return Proxy.newProxyInstance( classloader, new Class[]{clazz}, handler );
    }
    
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * A map containing key values.
    */
    private final Map m_map;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param map a map containing context key/value pairs.
    */
    private ContextInvocationHandler( Map map )
    {
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
                    Class clazz = method.getReturnType();
                    if( isAssignableFrom( clazz, value.getClass() ) )
                    {
                        return value;
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
                      "Unable to resolve a context entry value for the key [" + key + "].";
                    throw new IllegalStateException( error );
                }
            }
            throw new UnsupportedOperationException( name );
        }
    }
    
    private static boolean isAssignableFrom( Class clazz, Class c )
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
}
