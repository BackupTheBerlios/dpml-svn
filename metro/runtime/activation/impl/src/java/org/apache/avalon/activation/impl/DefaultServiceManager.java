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

import java.util.Map;
import java.util.Hashtable;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.DependencyModel;
import org.apache.avalon.composition.model.FatalServiceException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Default implementation of the framework {@link ServiceManager} interface.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class DefaultServiceManager implements ServiceManager
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultServiceManager.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ComponentModel m_model;

    private final Logger m_logger;

    /**
     * A table of identity hashcode integers of established objects 
     * that map to the lookup key that was uased to establish the object.
     * Used to track which model is providing an object when we 
     * handle release of objects.
     */
    private final Hashtable m_table = new Hashtable();

    /**
     * A table of dependency models keyed by lookup key.
     */
    private final Map m_map;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    /**
     * Construct a new ServiceManager.
     * @param model component model of the component that is 
     *   to be serviced.
     */
    public DefaultServiceManager( ComponentModel model )
    {
        if( model == null )
        {
            throw new NullPointerException( "model" );
        }

        m_model = model;

        m_logger = model.getLogger();

        m_map = new Hashtable();
        DependencyModel[] dependencies = model.getDependencyModels();
        for( int i=0; i < dependencies.length; i++ )
        {
            final DependencyModel dependency = dependencies[i];
            
            DeploymentModel provider = dependency.getProvider();
            if( provider != null )
            {
                final String key = dependency.getDependency().getKey();
                m_map.put( key, dependency );
            }
        }
    }

    //-------------------------------------------------------------------
    // ServiceManager
    //-------------------------------------------------------------------

    /**
     * Returns true if a provider exists for the supplied role.
     * @param key the service identifier key
     * @return boolean TRUE if the service is available else FALSE
     */
    public boolean hasService( String key )
    {
        if( key == null )
        {
            return false;
        }
        return ( m_map.get( key ) != null );
    }

    /**
     * Retrieve Object by key.
     * @param key the role
     * @return the Object
     * @throws ServiceException if an error occurs
     * @throws NullPointerException if the supplied key is null
     */
    public Object lookup( String key ) throws ServiceException
    {
        return lookup( key, -1 );
    }

    /**
     * Retrieve Object by key.
     * @param key the role
     * @return the Object or null if the Object doesn't exist
     * @throws ServiceException if an error occurs
     * @throws NullPointerException if the supplied key is null
     */
    public Object lookup( String key, long timeout ) throws ServiceException
    {
        if( key == null )
        {
            throw new NullPointerException( "key" );
        }

        if( !hasService( key ) )
        {
            final String error = 
              "Unknown key: " + key;
            throw new ServiceException( key, error );
        }

        //
        // locate the provider model that is prividing components
        // for this dependency
        //

        DependencyModel dependency = (DependencyModel) m_map.get( key );
        final DeploymentModel provider = dependency.getProvider();
        
        if( null == provider )
        {
            final String error = 
                REZ.getString( "service.error.null-provider", key );
            throw new IllegalStateException( error );
        }

        //
        // get a proxy to the service from the provider
        // (note that it is up to a provider to determine if
        // a proxy if generated based on its service export 
        // parameters)
        //

        try
        {
            Object instance = provider.resolve();
            if( Proxy.isProxyClass( instance.getClass() ) )
            {
                return instance;
            }

            //
            // otherwise we need to hold a reference linking the 
            // object with the source provider
            //

            String id = "" + System.identityHashCode( instance );
            m_table.put( id, key );
            if( getLogger().isDebugEnabled() )
            {
                final String message = 
                  "resolved service [" 
                  + id 
                  + "] for the key [" 
                  + key 
                  + "].";
                getLogger().debug( message );
            }

            return instance;
        }
        /*
        catch( TransientApplianceException e )
        {
            long delay = e.getDelay();
            if(( timeout == -1 ) || (( delay < timeout ) && ( delay > 0 )) )
            {
                try
                {
                    Thread.currentThread().sleep( delay );
                }
                catch( Throwable interrupted )
                {
                    // ignore
                }
                return lookup( key, delay );
            }
            else
            {
                final String error = 
                  "Requested service is not responding.";
                throw new TransientServiceException( key, error, delay );
            }
        }
        */
        catch( Throwable e )
        {
            //
            // TODO: framework states that ServiceException is thrown
            // if the service is not found - and in this case that isn't 
            // the issue - in effect we have a good key, but we simply
            // have not been able to go from key to instance -
            // should look into some more concrete subtypes of 
            // ServiceException

            final String error = 
              "Unexpected runtime error while attempting to resolve service for key: " 
              + key;
            throw new FatalServiceException( key, error, e );
        }
    }

    /**
     * Release a service back to the manager.
     * @param object the object to release
     */
    public void release( Object instance )
    {
        if( instance == null ) return;

        if( Proxy.isProxyClass( instance.getClass() ) )
        {
            InvocationHandler handler = 
                Proxy.getInvocationHandler( instance );
            if( handler instanceof ApplianceInvocationHandler )
            { 
                ApplianceInvocationHandler aic = 
                  (ApplianceInvocationHandler) handler;
                aic.release();
                return;
            }
            else if( handler instanceof BlockInvocationHandler )
            {
                return;
            }
        }

        //
        // otherwise we need to locate the source ourselves
        //

        String id = "" + System.identityHashCode( instance );
        final String key = (String) m_table.get( id );
        if( key == null )
        {
            if( getLogger().isWarnEnabled() )
            {
                final String warning = 
                  "Unrecognized object identity [" 
                  + id 
                  + "]. "
                  + "Either this object was not provided by this service manager "
                  + "or it has already been released.";
                getLogger().warn( warning );
            }
            return;
        }

        DependencyModel dependency = (DependencyModel) m_map.get( key );
        final DeploymentModel provider = dependency.getProvider();
        if( provider == null )
        {
            if( getLogger().isErrorEnabled() )
            {
                final String error = 
                  "Unable to release component as no provider could be found for the key ["
                  + key
                  + "].";
                getLogger().warn( error );
            }
            return;
        }

        provider.release( instance );
        if( getLogger().isDebugEnabled() )
        {
            final String message = 
              "released service [" 
              + id 
              + "] from the key [" 
              + key 
              + "].";
            getLogger().debug( message );
        }

        m_table.remove( id );
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

