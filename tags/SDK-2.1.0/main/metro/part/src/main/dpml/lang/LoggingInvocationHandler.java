/*
 * Copyright 2007 Stephen J. McConnell.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.util.Logger;

/**
 * Invocation handler utility for the legacy net.dpml.util.Logger that
 * redirects to an instance of net.dpml.util.Logger.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LoggingInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------
    
    private static final Map<String,Method> METHODS = getMethodMap();
    
   /**
    * Construct a new context instance implementing the supplied class
    * backed by entries in the supplied map.
    *
    * @param clazz the context class
    * @param logger the logging channel
    * @return the constructed context instance
    */
    public static <T>T getProxiedInstance( final Class<T> clazz, final Logger logger )
    {
        ClassLoader classloader = clazz.getClassLoader();
        LoggingInvocationHandler handler = new LoggingInvocationHandler( clazz, logger );
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
    private final Logger m_logger;
    private final Class m_class;
    private final Map<String,Method> m_methods = new Hashtable<String,Method>();
    
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param map a map containing context key/value pairs.
    */
    private LoggingInvocationHandler( final Class clazz, final Logger logger )
    {
        m_logger = logger;
        m_class = clazz;
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
        Method m = METHODS.get( name );
        if( m.getReturnType() == Logger.class )
        {
            Logger logger = (Logger) m.invoke( m_logger, args );
            return getProxiedInstance( m_class, logger );
        }
        else
        {
            return m.invoke( m_logger, args );
        }
    }
    
    private static Map<String,Method> getMethodMap()
    {
        Hashtable<String,Method> map = new Hashtable<String,Method>();
        Method[] methods = Logger.class.getMethods();
        for( Method method : methods )
        {
            map.put( method.getName(), method );
        }
        return map;
    }
}
