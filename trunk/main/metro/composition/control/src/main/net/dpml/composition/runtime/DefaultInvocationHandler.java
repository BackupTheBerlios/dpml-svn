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

package net.dpml.composition.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Method;

/**
 * The ConfigurationInvocationHandler class handles the isolation of a configuration
 * implementation behind the Configuration class.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
public final class DefaultInvocationHandler
  implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

    private final Object m_instance;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a proxy invocation handler.
    *
    * @param instance the instance
    */
    public DefaultInvocationHandler( Object instance )
    {
        assertNotNull( instance, "instance" );
        m_instance = instance;
    }

    Object getInstance()
    {
        return m_instance;
    }

    //-------------------------------------------------------------------
    // InvocationHandler
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
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable
    {
        try
        {
            return method.invoke( m_instance, args );
        }
        catch( Throwable e )
        {
            throw handleInvocationThrowable( method, e );
        }
    }

    private Throwable handleInvocationThrowable( Method method, Throwable e )
    {
        final String name = method.getName();
        final String error =
          "Delegation error while attempting to invoke the operation [" + name + "].";

        while( true )
        {
            if( e instanceof UndeclaredThrowableException )
            {
                UndeclaredThrowableException utException = (UndeclaredThrowableException) e;
                Throwable cause = utException.getUndeclaredThrowable();
                if( cause == null )
                {
                    return new DelegationRuntimeException( error, e );
                }
                else
                {
                    e = cause;
                }
            }
            else if( e instanceof InvocationTargetException )
            {
                final InvocationTargetException targetException = (InvocationTargetException) e;
                final Throwable cause = targetException.getTargetException();
                if( cause == null )
                {
                    return new DelegationRuntimeException( error, e );
                }
                else
                {
                    e = cause;
                }
            }
            else
            {
                break;
            }
        }
        return e;
    }

    private void assertNotNull( Object object, String key )
        throws NullPointerException
    {
        if( null == object )
        {
            throw new NullPointerException( key );
        }
    }
}
