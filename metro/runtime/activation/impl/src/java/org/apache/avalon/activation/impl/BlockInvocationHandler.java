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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.avalon.activation.ApplianceException;

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.ServiceModel;
import org.apache.avalon.composition.model.Reclaimer;

import org.apache.avalon.framework.logger.Logger;

/**
 * This makes a dynamic proxy for an object.  The object can be represented
 * by one, some or all of it's interfaces.
 */
final class BlockInvocationHandler implements InvocationHandler
{
    private final DefaultBlock m_block;
    private final Logger m_logger;
    private final Map m_providers = new Hashtable();

   /**
    * Create a proxy invocation handler.
    *
    * @param block the underlying block implementation
    * @exception if an invocation handler establishment error occurs
    */
    protected BlockInvocationHandler( 
      final Logger logger, final DefaultBlock block )
      throws Exception
    {
        if( block == null )
        {
            throw new NullPointerException( "block" );
        }

        m_block = block;
        m_logger = logger;
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
    public Object invoke( final Object proxy,
            final Method method,
            final Object[] args )
            throws Throwable
    {
        if( proxy == null ) throw new NullPointerException( "proxy" );
        if( method == null ) throw new NullPointerException( "method" );

        final ContainmentModel model = m_block.getContainmentModel();
        Class source = method.getDeclaringClass();

        ServiceModel service = model.getServiceModel( source );
        if( null == service )
        {
            final String error = 
             "Unable to resolve an provider for the class ["
             + source.getName() 
             + "].";
            throw new IllegalStateException( error );
        }
        
        DeploymentModel provider = service.getServiceProvider();

        //
        // resolve the service object from the appliance
        // and delegate the invocation to that provider
        //

        try
        {
            Object object = locateServiceInstance( provider );
            //Object object = provider.resolve();
            return method.invoke( object, args );
        }
        catch( UndeclaredThrowableException e )
        {
            Throwable cause = e.getUndeclaredThrowable();
            if( cause != null ) throw cause;
            final String error = 
              "Delegation error raised by: " + m_block;
            throw new ApplianceException( error, e );
        }
        catch( InvocationTargetException e )
        {
            Throwable cause = e.getTargetException();
            if( cause != null ) throw cause;
            final String error = 
              "Delegation error raised by: " + m_block;
            throw new ApplianceException( error, e );
        }
        catch( Throwable e )
        {
            final String error =
              "Composite service resolution failure for the class: '" 
              + method.getDeclaringClass()
              + "' for operation: '" + method.getName()
              + "' in appliance: " + m_block;
            throw new ApplianceException( error, e );
        }
    }

   /**
    * Locate a service reference and cache it locally for the 
    * lifetime of the invocation handler.
    * 
    * @param provider the deployment model 
    * @return a cached service reference
    * @exception Exception if something unexpected happens
    */
    private Object locateServiceInstance( DeploymentModel provider ) throws Exception
    {
        Object source = m_providers.get( provider );
        if( null != source ) return source;
        Object object = provider.resolve();
        m_providers.put( provider, object );
        return object;
    }

   /**
    * Finalization of the invocation handler during which
    * we release any cached service references.
    * 
    * @exception Throwable if an error occurs
    */
    protected void finalize() throws Throwable
    {
        Object[] objects = m_providers.values().toArray();
        for( int i=0; i<objects.length; i++ )
        {
            Object instance = objects[i];
            if( Proxy.isProxyClass( instance.getClass() ) )
            {
                InvocationHandler handler = 
                    Proxy.getInvocationHandler( instance );
                if( handler instanceof Reclaimer )
                { 
                    Reclaimer source = (Reclaimer) handler;
                    source.release();
                }
            }
        }
        m_providers.clear();
    }
}
