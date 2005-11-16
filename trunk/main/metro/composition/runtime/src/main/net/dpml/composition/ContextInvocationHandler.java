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

package net.dpml.composition;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.dpml.component.info.EntryDescriptor;

/**
 * Invoication handler for the Context inner class.  The invocation handler is 
 * responsible for the supply of values based on request invocations applied to 
 * a #Context inner-class.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
class ContextInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component.
    */
    private final ComponentHandler m_handler;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param handler the component handler
    */
    ContextInvocationHandler( ComponentHandler handler )
    {
        m_handler = handler;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    private ComponentHandler getComponentHandler()
    {
        return m_handler;
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
    public Object invoke( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        Class source = method.getDeclaringClass();
        if( Object.class == source )
        {
            return method.invoke( this, args );
        }
        else
        {
            ComponentHandler handler = getComponentHandler();
            String name = method.getName();
            if( name.startsWith( "get" ) )
            {
                String key = EntryDescriptor.getEntryKey( method );
                Object value = handler.getContextValue( key );
                if( null != value )
                {
                    return value;
                }
                else if( args.length > 0 )
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
            /*
            if( ( null != args ) && ( args.length == 1 ) )
            {
                if( "addPropertyChangeListener".equals( name ) )
                {
                    PropertyChangeListener listener = (PropertyChangeListener) args[0];
                    handler.addPropertyChangeListener( listener );
                    return null;
                }
                else if( "removePropertyChangeListener".equals( name ) )
                {
                    PropertyChangeListener listener = (PropertyChangeListener) args[0];
                    handler.removePropertyChangeListener( listener );
                    return null;
                }
            }
            */
            throw new NoSuchMethodException( name );
        }
    }
}
