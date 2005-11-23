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

package net.dpml.transit;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventObject;

import net.dpml.transit.Logger;
import net.dpml.transit.TransitError;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.store.TransitStorage;
import net.dpml.transit.store.CodeBaseStorage;
import net.dpml.transit.store.LayoutRegistryHome;
import net.dpml.transit.store.ContentRegistryHome;
import net.dpml.transit.store.ProxyStorage;
import net.dpml.transit.store.CacheHome;
import net.dpml.transit.store.Removable;
import net.dpml.transit.store.TransitStorageUnit;
import net.dpml.transit.model.*;

/**
 * The DefaultTransitModel class maintains an active configuration of the 
 * Transit system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultTransitModel extends DefaultModel implements TransitModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final TransitStorage m_store;
    private final DefaultProxyModel m_proxy;
    private final DefaultLayoutRegistryModel m_layouts;
    private final DefaultCacheModel m_cache;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new TransitModel using a default logging channel
    * and preferences based storage solution.
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel() throws RemoteException
    {
        this( new LoggingAdapter( "transit" ) );
    }

   /**
    * Creation of a new TransitModel using preferences based storage.
    * 
    * @param logger the assigned loging channel
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel( Logger logger ) throws RemoteException
    {
        this( logger, new TransitStorageUnit() );
    }

   /**
    * Creation of a new TransitModel using a supplied storage unit
    * and a default logging channel.
    * 
    * @param store the storage unit
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel( TransitStorage store ) throws RemoteException
    {
        this( new LoggingAdapter( "transit" ), store );
    }

   /**
    * Creation of a new TransitModel using a supplied storage unit
    * and logging channel.  The implementation will construct a proxy
    * model, layout registry model, cache modle, content registry model, 
    * and repository codebase model using the supplied storage unit.
    *
    * @param logger the assigned loging channel
    * @param store the storage unit 
    * @exception NullPointerException if the logger or store arguments are null
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel( Logger logger, TransitStorage store ) 
      throws RemoteException, NullPointerException
    {
        super( logger );

        if( null == store )
        {
            throw new NullPointerException( "store" );
        }
        m_store = store;

        m_proxy = createProxyModel();
        m_layouts = createLayoutRegistryModel();
        DefaultContentRegistryModel content = createContentRegistryModel();
        m_cache = createCacheModel( m_layouts, content );
    }

    // ------------------------------------------------------------------------
    // TransitModel
    // ------------------------------------------------------------------------

   /**
    * Return the proxy model.
    * @return the proxy configuration model.
    */
    public ProxyModel getProxyModel()
    {
        return m_proxy;
    }

   /**
    * Return the cache model.
    * @return the cache model 
    */
    public CacheModel getCacheModel()
    {
        return m_cache;
    }

   /**
    * Internal event handler.
    * @param eventObject the event to handle
    */
    protected void processEvent( EventObject eventObject )
    {
        // no event in this object
    }
    
    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

   /**
    * Trigger disposal of the transit model.
    */
    public synchronized void dispose()
    {
        m_cache.dispose();
        m_proxy.dispose();
        try
        {
            UnicastRemoteObject.unexportObject( m_cache, true );
        }
        catch( RemoteException e )
        {
            getLogger().warn( "Remote error during disposal.", e );
        }
        try
        {
            UnicastRemoteObject.unexportObject( m_proxy, true );
        }
        catch( RemoteException e )
        {
            getLogger().warn( "Remote error during disposal.", e );
        }
    }

    private DefaultProxyModel createProxyModel()
    {
        try
        {
            ProxyStorage unit = m_store.getProxyStorage();
            Logger logger = getLogger().getChildLogger( "proxy" );
            return new DefaultProxyModel( logger, unit );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the proxy storage unit.";
            throw new TransitError( error, e );
        }
    }

    private DefaultLayoutRegistryModel createLayoutRegistryModel()
    {
        try
        {
            Logger logger = getLogger().getChildLogger( "cache" ).getChildLogger( "layout" );
            LayoutRegistryHome store = m_store.getLayoutRegistryHome();
            return new DefaultLayoutRegistryModel( logger, store );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the layout storage home.";
            throw new TransitError( error, e );
        }
    }

    private DefaultCacheModel createCacheModel( LayoutRegistryModel layouts, DefaultContentRegistryModel content )
    {
        try
        {
            CacheHome store = m_store.getCacheHome();
            Logger logger = getLogger().getChildLogger( "cache" );
            return new DefaultCacheModel( logger, store, layouts, content );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the cache storage unit.";
            throw new TransitError( error, e );
        }
    }

    private DefaultContentRegistryModel createContentRegistryModel()
    {
        try
        {
            ContentRegistryHome store = m_store.getContentRegistryHome();
            Logger logger = getLogger().getChildLogger( "content" );
            return new DefaultContentRegistryModel( logger, store );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the content registry model.";
            throw new TransitError( error, e );
        }
    }
}

