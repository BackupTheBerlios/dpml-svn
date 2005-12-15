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

package net.dpml.metro.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class that supports instance isolation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class InstanceInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

    private final DefaultProvider m_instance;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a proxy invocation handler.
    *
    * @param instance the instance
    */
    public InstanceInvocationHandler( DefaultProvider instance )
    {
        if( null == instance )
        {
            throw new NullPointerException( "instance" );
        }
        m_instance = instance;
    }
    
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
    public Object invoke( final Object proxy, final Method method, final Object[] args ) 
      throws InvocationTargetException, IllegalAccessException
    {
        DefaultProvider instance = getProvider();
        if( instance.isAvailable() )
        {
            Object target = instance.getValue( false );
            return method.invoke( target, args );
        }
        else
        {
            final String error = 
              "Resource unavailable: " + instance;
            throw new IllegalStateException( error );
        }
    }

    protected DefaultProvider getProvider()
    {
        return m_instance;
    }
}
