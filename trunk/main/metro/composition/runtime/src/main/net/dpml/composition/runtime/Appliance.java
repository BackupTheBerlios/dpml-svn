/*
 * Copyright 2004-2005 Stephen J. McConnell.
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
import java.net.URI;
import java.rmi.RemoteException;

import net.dpml.safe.control.Disposable;
import net.dpml.component.runtime.AvailabilityEvent;
import net.dpml.component.runtime.AvailabilityListener;
import net.dpml.component.runtime.ResourceUnavailableException;

/**
 * An appliance is an invocation handler maintained by a component. It is used
 * during the construction of proxies to the component holding the appliance.
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
final class Appliance implements InvocationHandler, AvailabilityListener, Disposable
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

    private final ComponentHandler m_component;
    private boolean m_available = true;
    private boolean m_disposed = false;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a proxy invocation handler.
    *
    * @param component the component
    */
    Appliance( ComponentHandler component )
    {
        assertNotNull( component, "component" );
        m_component = component;
        m_component.addAvailabilityListener( this );
    }

    //-------------------------------------------------------------------
    // AvailabilityListener
    //-------------------------------------------------------------------

    public void availabilityChanged( AvailabilityEvent event )
    {
        m_available = event.isAvailable();
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
        // check if our availability flag is set to FALSE 

        if( false == m_available )
        {
            final String error = 
              "Component is not currently available."
              + "\nComponent: " + m_component.getLocalURI()
              + "\nClass: " + m_component.getDeploymentClass().getName();
            throw new ResourceUnavailableException( error );
        }

        // otherwise lets' go with an optomistic attitude

        try
        {
            Object instance = m_component.resolve( false );
            return method.invoke( instance, args );
        }
        catch( Throwable e )
        {
            throw handleInvocationThrowable( method, e );
        }
    }

    //-------------------------------------------------------------------
    // ApplianceInvocationHandler
    //-------------------------------------------------------------------

    public URI getURI()
    {
        return m_component.getLocalURI();
    }

   /**
    * Request for the component manager or finalization method to release 
    * references.
    */
    public void dispose() 
    {
        if( !m_disposed )
        {
            m_disposed = true;
            try
            {
                m_component.removeAvailabilityListener( this );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------
    // utilities
    //-------------------------------------------------------------------

    public void finalize()
    {
        dispose();
    }

    private Throwable handleInvocationThrowable( Method method, Throwable e )
    {
        final String name = method.getName();
        final String error =
          "Delegation error raised in [" 
          + getURI()
          + "] while attempting to invoke the operation ["
          + name
          + "].";

        while( true )
        {
            if( e instanceof UndeclaredThrowableException )
            {
                Throwable cause = 
                  ((UndeclaredThrowableException)e).getUndeclaredThrowable();
                if( cause == null )
                {
                    return new ApplianceException( error, e );
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
                    return new ApplianceException( error, e );
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

