/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.activation.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import org.apache.avalon.activation.ApplianceException;
import org.apache.avalon.activation.TransientApplianceException;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.TransientRuntimeException;
import org.apache.avalon.composition.model.Reclaimer;

import org.apache.avalon.framework.logger.Logger;

/**
 * This makes a dynamic proxy for an object.  The object can be represented
 * by one, some or all of it's interfaces.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ApplianceInvocationHandler.java 45862 2004-09-11 03:16:39Z niclas $
 */
public final class ApplianceInvocationHandler 
  implements InvocationHandler, Reclaimer
{
    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final DefaultAppliance m_appliance;
    private final Logger m_logger;
    private final ComponentModel m_model;
    private final boolean m_secure;

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private Object m_instance;
    private boolean m_destroyed = false;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a proxy invocation handler.
    *
    * @param appliance the runtime appliance
    * @param logger the assigned logging channel 
    */
    ApplianceInvocationHandler( 
      DefaultAppliance appliance, Logger logger, boolean secure )
    {
        assertNotNull( appliance, "appliance" ); 
        assertNotNull( logger, "logger" ); 
        
        m_appliance = appliance;
        m_logger = logger;
        m_secure = secure;
        m_model = m_appliance.getComponentModel();
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
    public Object invoke( final Object proxy,
      final Method method,
      final Object[] args )
      throws Throwable
    {
        if( proxy == null ) 
          throw new NullPointerException( "proxy" );
        if( method == null ) 
          throw new NullPointerException( "method" );
        if( m_destroyed ) 
          throw new IllegalStateException( "destroyed" );

        try
        {
            final Object instance = getInstance();
           
            if( m_secure )
            {
                return AccessController.doPrivileged( 
                  new PrivilegedExceptionAction()
                  {
                      public Object run() throws Exception
                      {
                         return method.invoke( instance, args );
                      }
                  }, 
                  m_model.getAccessControlContext() );
            }
            else
            {
                return method.invoke( instance, args );
            }
        }
        catch( Throwable e )
        {
            throw handleInvocationThrowable( e );
        }
    }

    //-------------------------------------------------------------------
    // Reclaimer
    //-------------------------------------------------------------------

    public void release()
    {
        if( !m_destroyed )
        {
            m_destroyed = true;
            final String message = 
              "Releasing component [" 
              + System.identityHashCode( m_instance ) + "] (" 
              + m_appliance.toString()
              + ").";
            getLogger().debug( message );
            m_appliance.release( m_instance );
        }
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    protected void finalize() throws Throwable
    {
        if( !m_destroyed && ( null != m_instance ) )
        {
            final String message = 
              "Finalizing proxy [" 
              + System.identityHashCode( m_instance ) + "] (" 
              + m_appliance.toString()
              + ").";
            getLogger().debug( message );
            release();
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private Object getInstance() throws Exception
    {
        if( m_instance == null )
          m_instance = m_appliance.resolve( false );
        return m_instance;
    }

    private Throwable handleInvocationThrowable( Throwable e )
    {
        final String error = 
          "Delegation error raised by component: " 
          + m_appliance.toString();
        while( true )
        {
            if( e instanceof TransientApplianceException )
            {
                TransientApplianceException t = 
                  (TransientApplianceException) e;
                return new TransientRuntimeException( 
                  t.getMessage(), t.getDelay() );
            }
            else if( e instanceof UndeclaredThrowableException )
            {
                Throwable cause = 
                  ((UndeclaredThrowableException) e).getUndeclaredThrowable();
                if( cause == null )
                  return new ApplianceException( error, e );
                e = cause;
            }
            else if( e instanceof InvocationTargetException )
            {
                Throwable cause = 
                  ((InvocationTargetException) e).getTargetException();
                if( cause == null )
                  return new ApplianceException( error, e );
                e = cause;
            }
            else if( e instanceof PrivilegedActionException )
            {
                Throwable cause = 
                  ((PrivilegedActionException) e).getException();
                if( cause == null )
                    return new ApplianceException( error, e );
                e = cause;
            }
            else
            {
                break;
            }
        }
        return e;
    }

    private void assertNotNull( Object object, String key )
    {
        if( null == object )
        {
            throw new NullPointerException( key );
        }
    }
}
