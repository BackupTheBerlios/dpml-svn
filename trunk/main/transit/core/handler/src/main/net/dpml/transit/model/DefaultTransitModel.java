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

package net.dpml.transit.model;

import java.rmi.RemoteException;

import net.dpml.transit.TransitError;
import net.dpml.transit.adapter.LoggingAdapter;
import net.dpml.transit.model.DefaultProxyModel;
import net.dpml.transit.model.DefaultLayoutRegistryModel;
import net.dpml.transit.model.DefaultContentRegistryModel;
import net.dpml.transit.model.DefaultRepositoryModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.RepositoryModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.store.TransitStorage;
import net.dpml.transit.store.CodeBaseStorage;
import net.dpml.transit.store.LayoutRegistryHome;
import net.dpml.transit.store.ContentRegistryHome;
import net.dpml.transit.store.ProxyStorage;
import net.dpml.transit.store.CacheHome;
import net.dpml.transit.store.Removable;
import net.dpml.transit.unit.TransitStorageUnit;

/**
 * The DefaultTransitModel class maintains an active configuration of the 
 * Transit system.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultTransitModel extends DefaultModel implements TransitModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final TransitStorage m_store;
    private final ProxyModel m_proxy;
    private final LayoutRegistryModel m_layouts;
    private final CacheModel m_cache;
    private final ContentRegistryModel m_content;
    private final RepositoryModel m_repository;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultTransitModel() throws RemoteException
    {
        this( new LoggingAdapter( "transit" ) );
    }

    public DefaultTransitModel( Logger logger ) throws RemoteException
    {
        this( logger, new TransitStorageUnit() );
    }

    public DefaultTransitModel( TransitStorage store ) throws RemoteException
    {
        this( new LoggingAdapter( "transit" ), store );
    }

    public DefaultTransitModel( Logger logger, TransitStorage store ) throws RemoteException
    {
        super( logger );

        if( null == store )
        {
            throw new NullPointerException( "store" );
        }
        m_store = store;

        m_proxy = createProxyModel();
        m_layouts = createLayoutRegistryModel();
        m_cache = createCacheModel( m_layouts );
        m_content = createContentRegistryModel();
        m_repository = createRepositoryModel();
    }

    // ------------------------------------------------------------------------
    // TransitModel
    // ------------------------------------------------------------------------

   /**
    * Return the model identifier.
    * @return the model id
    */
    public String getID()
    { 
        return m_store.getID();
    }

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
    * Return the content registry model.
    *
    * @return the content Model
    */
    public ContentRegistryModel getContentRegistryModel()
    {
        return m_content;
    }

   /**
    * Return the repository model.
    *
    * @return the repository director
    */
    public RepositoryModel getRepositoryModel()
    {
        return m_repository;
    }

    // ------------------------------------------------------------------------
    // Disposable
    // ------------------------------------------------------------------------

   /**
    * Add a disposal listener to the model.
    * @param listener the listener to add
    */
    public void addDisposalListener( DisposalListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a disposal listener from the model.
    * @param listener the listener to remove
    */
    public void removeDisposalListener( DisposalListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }


    public void dispose() throws RemoteException
    {
        VetoableDisposalEvent veto = new VetoableDisposalEvent( this );
        enqueueEvent( veto, false );
        DisposalEvent disposal = new DisposalEvent( this );
        enqueueEvent( disposal, false );
        if( ( null != m_store ) && ( m_store instanceof Removable ) )
        {
            Removable store = (Removable) m_store;
            store.remove();
        }
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    private ProxyModel createProxyModel()
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

    private LayoutRegistryModel createLayoutRegistryModel()
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

    private CacheModel createCacheModel( LayoutRegistryModel layouts )
    {
        try
        {
            CacheHome store = m_store.getCacheHome();
            Logger logger = getLogger().getChildLogger( "cache" );
            return new DefaultCacheModel( logger, store, layouts );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the cache storage unit.";
            throw new TransitError( error, e );
        }
    }

    private ContentRegistryModel createContentRegistryModel()
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

    private RepositoryModel createRepositoryModel()
    {
        try
        {
            CodeBaseStorage store = m_store.getRepositoryStorage();
            Logger logger = getLogger().getChildLogger( "repository" );
            return new DefaultRepositoryModel( logger, store );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the repository model.";
            throw new TransitError( error, e );
        }
    }

    private static class VetoableDisposalEvent extends DisposalEvent
    {
        public VetoableDisposalEvent( TransitModel source )
        {
            super( source );
        }
    }
}

