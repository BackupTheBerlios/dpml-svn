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

package net.dpml.metro.runtime;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.dpml.component.ControlException;

import net.dpml.metro.ComponentContext;
import net.dpml.metro.info.EntryDescriptor;

/**
 * Invoication handler for the Context inner class.  The invocation handler is 
 * responsible for the supply of values based on request invocations applied to 
 * a #Context inner-class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ContextInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component.
    */
    private final DefaultProvider m_provider;
    
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param provider the provider
    */
    ContextInvocationHandler( DefaultProvider provider )
    {
        m_provider = provider;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    private DefaultComponentHandler getDefaultComponentHandler()
    {
        return m_provider.getDefaultComponentHandler();
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
        else if( ComponentContext.class == source )
        {
            DefaultComponentHandler handler = getDefaultComponentHandler();
            return method.invoke( handler, args );
        }
        else
        {
            String name = method.getName();
            if( name.startsWith( "get" ) )
            {
                String key = EntryDescriptor.getEntryKey( method );
                Object value = getContextValue( key );
                if( null != value )
                {
                    return value;
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
            else
            {
                throw new NoSuchMethodException( name );
            }
            /*
            if( ( null != args ) && ( args.length == 1 ) )
            {
                if( "addPropertyChangeListener".equals( name ) )
                {
                    PropertyChangeListener listener = (PropertyChangeListener) args[0];
                    m_provider.addPropertyChangeListener( listener );
                    return null;
                }
                else if( "removePropertyChangeListener".equals( name ) )
                {
                    PropertyChangeListener listener = (PropertyChangeListener) args[0];
                    m_provider.removePropertyChangeListener( listener );
                    return null;
                }
            }
            */
        }
    }
    
    Object getContextValue( String key ) throws ControlException
    {
        DefaultComponentHandler handler = getDefaultComponentHandler();
        return handler.getComponentController().getContextValue( m_provider, key );
    }

}
