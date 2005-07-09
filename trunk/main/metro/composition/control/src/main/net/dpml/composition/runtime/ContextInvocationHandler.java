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

package net.dpml.composition.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Observer;

import net.dpml.part.control.AvailabilityListener;
import net.dpml.part.state.StateListener;

import net.dpml.composition.control.CompositionController;

/**
 * This makes a dynamic proxy for an object.  The object can be represented
 * by one, some or all of it's interfaces.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
class ContextInvocationHandler
  implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component.
    */
    private final ComponentHandler m_component;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param controller the composition controller
    * @param component the component
    */
    ContextInvocationHandler( ComponentHandler component )
    {
        m_component = component;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    ComponentHandler getComponentHandler()
    {
        return m_component;
    }

    CompositionController getController()
    {
        return m_component.getController();
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
    * @exception NullPointerException if any one of the proxy or method arguments
    *            is null, or if the object instance has been destroyed earlier.
    */
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable, NullPointerException
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
                String key = getKeyFromMethod( method );
                ContextMap map = getComponentHandler().getContextMap();
                return map.getValue( key, args );
            }
            else if( null != args )
            {
                if( args.length > 0 )
                {
                    if( "addAvailabilityListener".equals( name ) )
                    {
                        AvailabilityListener listener = (AvailabilityListener) args[0];
                        getComponentHandler().addAvailabilityListener( listener );
                        return null;
                    }
                    else if( "removeAvailabilityListener".equals( name ) )
                    {
                        AvailabilityListener listener = (AvailabilityListener) args[0];
                        getComponentHandler().removeAvailabilityListener( listener );
                        return null;
                    }
                    else if( "addStateListener".equals( name ) )
                    {
                        StateListener listener = (StateListener) args[0];
                        getComponentHandler().addStateListener( listener );
                        return null;
                    }
                    else if( "removeStateListener".equals( name ) )
                    {
                        StateListener listener = (StateListener) args[0];
                        getComponentHandler().removeStateListener( listener );
                        return null;
                    }
                    else
                    {
                        throw new NoSuchMethodException( name );
                    }
                }
                else
                {
                    throw new NoSuchMethodException( name );
                }
            }
            else
            {
                throw new NoSuchMethodException( name );
            }
        }
    }

    private String getKeyFromMethod( Method method )
    {
        String name = method.getName();
        if( name.startsWith( "get" ) )
        {
            return formatKey( name.substring( 3 ) );
        }
        else
        {
            final String error = 
              "Invalid method accessor ["
              + name 
              + "]";
            throw new IllegalArgumentException( error );
        }
    }

    private String formatKey( String key )
    {
        if( key.length() < 1 ) 
        {
            throw new IllegalArgumentException( "key" );
        }
        String first = key.substring( 0, 1 ).toLowerCase();
        String remainder = key.substring( 1 );
        return first + remainder;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    private void assertNotNull( Object object, String key )
        throws NullPointerException
    {
        if( null == object )
        {
            throw new NullPointerException( key );
        }
    }
}
