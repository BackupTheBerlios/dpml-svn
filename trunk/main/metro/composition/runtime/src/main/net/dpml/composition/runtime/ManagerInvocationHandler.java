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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import net.dpml.composition.controller.CompositionController;

import net.dpml.state.State;
import net.dpml.state.impl.DefaultState;

/**
 * This makes a dynamic proxy for an component manager.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
final class ManagerInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

    private final ComponentHandler m_component;
    private final Object m_manager;
    private final Class m_class;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a new state manager invocation handler.
    *
    * @param component the component
    * @param manager the component's manager
    */
    ManagerInvocationHandler( ComponentHandler component, Object manager )
    {
        m_component = component;
        m_manager = manager;
        m_class = m_manager.getClass();
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
    * @exception NullPointerException if any one of the proxy or method arguments
    *            is null, or if the object instance has been destroyed earlier.
    */
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable
    {
        String name = method.getName();
        Class source = method.getDeclaringClass();
        if( Object.class == method.getDeclaringClass() )
        {
            return method.invoke( m_manager, args );
        }

         // TODO: remove initialize and terminate
        if( name.equals( "resolve" ) || name.equals( "initialize" ) || name.equals( "terminate" ) ) 
        {
            try
            {
                Method target = m_class.getMethod( name, new Class[0] );
                return target.invoke( m_manager, args );
            }
            catch( NoSuchMethodException e )
            {
                return null;
            }
            catch( Throwable e )
            {
                throw handleInvocationThrowable( method, e );
            }
        }
        else if( name.equals( "getState" ) )
        {
            try
            {
                Method target = m_class.getMethod( name, new Class[0] );
                return method.invoke( m_manager, args );
            }
            catch( NoSuchMethodException e )
            {
                return TERMINAL_STATE;
            }
            catch( Throwable e )
            {
                throw handleInvocationThrowable( method, e );
            }
        }
        else if( "apply".equals( name ) || "getInstance".equals( name ) || "execute".equals( name ) )
        {
            try
            {
                Method target = m_class.getMethod( name, new Class[]{ String.class } );
                return method.invoke( m_manager, args );
            }
            catch( NoSuchMethodException e )
            {
                final String error = 
                  "The "
                  + name 
                  + " method is not implemented."
                  + "\nOperation: " + name
                  + "\nInstance: " + m_component.getLocalURI();
                throw new UnsupportedOperationException( error );
            }
            catch( Throwable e )
            {
                throw handleInvocationThrowable( method, e );
            }
        }
        else
        {
            final String error = 
              "The requested operation is not recognized."
              + "\nOperation: " + name
              + "\nInstance: " + m_component.getLocalURI();
            throw new UnsupportedOperationException( error );
        }
    }

    private Throwable handleInvocationThrowable( Method method, Throwable e )
    {
        final String name = method.getName();
        final String error =
          "Unexpected error while attempting to invoke a state manager operation."
          + "\nMethod: " + name
          + "\nInstance: " + m_component.getLocalURI();

        while( true )
        {
            if( e instanceof UndeclaredThrowableException )
            {
                Throwable cause = 
                  ((UndeclaredThrowableException)e).getUndeclaredThrowable();
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
                Throwable cause =
                  ((InvocationTargetException)e).getTargetException();
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

    private static final State TERMINAL_STATE = new DefaultState( "" );

}
