/*
 * Copyright 2006 Stephen J. McConnell.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.dpml.annotation.Services;

import dpml.lang.Disposable;

/**
* Invocation handler supporting instance isolation relative to services 
* declared by a target implementation class.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
*/
class ComponentInvocationHandler implements InvocationHandler
{
    private Object m_instance;
    
   /**
    * Creation of a new component invocation handler.
    * @param instance the instance to be isolated
    */
    protected ComponentInvocationHandler( Object instance )
    {
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
        return method.invoke( m_instance, args );
    }
    
    protected Object getInstance()
    {
        return m_instance;
    }
}
