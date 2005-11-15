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

package net.dpml.composition.engine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class that supports instance isolation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
final class InstanceInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

    private final DefaultInstance m_instance;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a proxy invocation handler.
    *
    * @param instance the instance
    */
    public InstanceInvocationHandler( DefaultInstance instance )
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
        DefaultInstance instance = getInstance();
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

    protected DefaultInstance getInstance()
    {
        return m_instance;
    }
}
