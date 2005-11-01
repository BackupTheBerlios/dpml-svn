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

import java.io.File;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Set;
import java.util.TreeSet;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.TransitError;
import net.dpml.transit.store.CacheHome;
import net.dpml.transit.store.HostStorage;
import net.dpml.transit.util.PropertyResolver;

/**
 * Default implementation of the cache model that maintains information 
 * about the current cache directory and the associated hosts.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class DefaultCacheModel extends DisposableCodeBaseModel 
   implements CacheModel, DisposalListener
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final CacheHome m_home;

    private final Set m_list = Collections.synchronizedSortedSet( new TreeSet() );

    private HostModel[] m_sortedHosts;

    private File m_cache;

    private LayoutModel m_layout;

    private LayoutRegistryModel m_registry;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construct a new cache model.  The cache module uses the supplied layout registry
    * model ads the source for the layout model to use for the cache and as the source when
    * resolving layout models for assigned hosts.  Data is supplied via a cache home that
    * is itself responsible for the maintaince of the cache model persistent state.
    *
    * @param logger the assigned logging channel
    * @param home the cache storage home
    * @param registry a registry of layout models
    * @exception DuplicateKeyException if the storage home references duplicate host identities
    * @exception UnknownKeyException if the storage home references a host storage unit that references
    *    an unknown layout model
    * @exception MalformedURLException if the storage home references a host storage unit containing
    *    a malfo9rmed base url
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultCacheModel( 
      Logger logger, CacheHome home, LayoutRegistryModel registry )
      throws DuplicateKeyException, RemoteException, UnknownKeyException, MalformedURLException
    {
        super( logger, home );
        if( null == registry )
        {
            throw new NullPointerException( "registry" );
        }

        m_home = home;
        m_registry = registry;

        String key = home.getLayoutModelKey();
        m_layout = registry.getLayoutModel( key );
        m_layout.addDisposalListener( this );

        String path = home.getCacheDirectoryPath();
        setCacheDirectoryPath( path, false );

        HostStorage[] hosts = home.getInitialHosts();
        m_sortedHosts = sortHosts();
        for( int i=0; i < hosts.length; i++ )
        {
            HostStorage host = hosts[i];
            addHostModel( host, false );
        }
    }

    // ------------------------------------------------------------------------
    // DisposalListener (listen to modification to the assigned layout)
    // ------------------------------------------------------------------------

   /**
    * The cache model listens for fisposal events of the layout model it 
    * is using and will raise a VetoDisposalException automatically.
    * @param event the layout predisposal disposal event
    * @exception VetoDisposalException it veto the disposal
    */
    public void disposing( DisposalEvent event ) throws VetoDisposalException
    {
        final String message = "Layout currently assigned to cache.";
        throw new VetoDisposalException( this, message );
    }

   /**
    * Notify the listener of the disposal of a manager. If the cache model 
    * receives this event a TransitError will be raised as this signals the 
    * unexpected condition that disposal event veto was not respected (which should 
    * not happen).
    */
    public void disposed( DisposalEvent event )
    {
        final String error = 
          "Unexpected notification of disposal of an assigned cache layout.";
        throw new TransitError( error );
    }

    // ------------------------------------------------------------------------
    // CacheModel
    // ------------------------------------------------------------------------

   /**
    * Return the cache layout strategy model used by the cache implementation.
    * @return the layout model
    * @exception RemoteException if a remote exception occurs
    */
    public LayoutModel getLayoutModel()
    {
        return m_layout;
    }

   /**
    * Update the value the local cache directory path.
    *
    * @param path the cache directory path
    * @exception RemoteException if a remote exception occurs
    */
    public void setCacheDirectoryPath( final String path )
    {
        setCacheDirectoryPath( path, true );
    }

   /**
    * Update the value the local cache directory path.
    *
    * @param path the cache directory path
    * @param notify if true then notify listeners
    * @exception RemoteException if a remote exception occurs
    */
    protected void setCacheDirectoryPath( final String path, boolean notify )
    {
        synchronized( getLock() )
        {
            if( null == path )
            {
                throw new NullPointerException( "path" );
            }

            String resolved = PropertyResolver.resolve( path );
            File cache = new File( resolved );
            if( !cache.isAbsolute() )
            {
                File anchor = getAnchorDirectory();
                cache = new File( anchor, resolved );
                cache.mkdirs();
            }
            if( null == m_cache )
            {
                getLogger().debug( "setting cache: " + cache ); 
            }
            else
            {
                getLogger().debug( "updating cache: " + cache ); 
            }
            m_cache = cache;
            if( notify )
            {
                m_home.setCacheDirectoryPath( path );
                CacheDirectoryChangeEvent event = new CacheDirectoryChangeEvent( this, path );
                enqueueEvent( event );
            }
        }
    }

   /**
    * Return the layout registry model.
    * @return the registry of layout models
    * @exception RemoteException if a remote exception occurs
    */
    public LayoutRegistryModel getLayoutRegistryModel()
    {
        return m_registry;
    }

   /**
    * Return an array of host models established by the implementation.
    * @return the host model array
    * @exception RemoteException if a remote exception occurs
    */
    public HostModel[] getHostModels()
    {
        synchronized( getLock() )
        {
            return m_sortedHosts;
        }
    }

   /**
    * Return an identified host model .
    * @param id the host model id
    * @return the host model
    * @exception UnknownKeyException the host model id is not recognized
    */
    public HostModel getHostModel( String id ) throws UnknownKeyException
    {
        synchronized( getLock() )
        {
            HostModel[] managers = getHostModels();
            for( int i=0; i < managers.length; i++ )
            {
                HostModel manager = managers[i];
                try
                {
                    if( id.equals( manager.getID() ) )
                    {
                        return manager;
                    }
                }
                catch( RemoteException e )
                {
                    throw new ModelRuntimeException( e.getMessage(), e );
                }
            }
            throw new UnknownKeyException( id );
        }
    }

   /**
    * Add a new host model to the cache model using a host model identifier .
    * @param id the host model id to be added
    * @exception DuplicateKeyException a host model with a matching id already exists
    * @exception UnknownKeyException the host model requests a layout model id that is not recognized
    * @exception MalformedURLException the host model baseurl is malformed
    * @exception RemoteException if a remote exception occurs
    */
    public void addHostModel( String id ) 
       throws DuplicateKeyException, UnknownKeyException, MalformedURLException
    {
        HostStorage store = m_home.getHostStorage( id );
        addHostModel( store, true );
    }

   /**
    * Add a new host model to the cache model using a supplied host model.
    * @param model the host model to add
    * @exception DuplicateKeyException a host model with a matching id already exists
    * @exception RemoteException if a remote exception occurs
    */
    public void addHostModel( HostModel model ) throws DuplicateKeyException
    {
        addHostModel( model, true );
    }

   /**
    * Remove a host from the cache model.
    * @param model the host model to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeHostModel( HostModel model )
    {
        synchronized( getLock() )
        {
            try
            {
                model.dispose();
                m_list.remove( model );
                m_sortedHosts = sortHosts();
                HostRemovedEvent event = new HostRemovedEvent( this, model );
                enqueueEvent( event );
            }
            catch( RemoteException e )
            {
                throw new ModelRuntimeException( e.getMessage(), e );
            }
        }
    }

   /**
    * Return the cache directory path.
    * @return the cache path.
    * @exception RemoteException if a remote exception occurs
    */
    public String getCacheDirectoryPath()
    {
        return m_home.getCacheDirectoryPath();
    }

   /**
    * Return the directory to be used by the cache handler as the cache directory.
    * @return the cache directory.
    * @exception RemoteException if a remote exception occurs
    */
    public File getCacheDirectory()
    {
        return m_cache;
    }

   /**
    * Add a cache change listener.
    * @param listener the listener to add
    * @exception RemoteException if a remote exception occurs
    */
    public void addCacheListener( CacheListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a cache change listener.
    * @param listener the listener to remove
    * @exception RemoteException if a remote exception occurs
    */
    public void removeCacheListener( CacheListener listener )
    {
        super.removeListener( listener );
    }

    // ------------------------------------------------------------------------
    // Disposable
    // ------------------------------------------------------------------------

   /**
    * Disposal of the cache model.
    * @exception RemoteException if a remote exception occurs
    */
    public void dispose()
    {
        super.dispose();
        try
        {
            m_layout.removeDisposalListener( this );
        }
        catch( RemoteException e )
        {
            throw new ModelRuntimeException( e.getMessage(), e );
        }
    }

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

   /**
    * Add a new host model to the cache model.
    * @param model the host model to be added
    * @exception DuplicateKeyException a host model with a matching id already exists
    * @exception RemoteException if a remote exception occurs
    */
    void addHostModel( HostModel manager, boolean notify ) throws DuplicateKeyException
    {
        synchronized( getLock() )
        {
            try
            {
                String id = manager.getID();
                HostModel m = getHostModel( id );
                throw new DuplicateKeyException( id );
            }
            catch( UnknownKeyException e )
            {
                m_list.add( manager );
                m_sortedHosts = sortHosts();
                if( notify )
                {
                    HostAddedEvent event = new HostAddedEvent( this, manager );
                    enqueueEvent( event );
                }
            }
            catch( RemoteException e )
            {
                throw new ModelRuntimeException( e.getMessage(), e );
            }
        }
    }

   /**
    * Add a new host model to the cache model using a host model store .
    * @param store the host model storage unit
    * @param notify if TRUE issue a notification event of host model addition
    * @exception DuplicateKeyException a host model with a matching id already exists
    * @exception UnknownKeyException the host model requests a layout model id that is not recognized
    * @exception MalformedURLException the host model base url is malformed
    * @exception RemoteException if a remote exception occurs
    */
    void addHostModel( HostStorage store, boolean notify ) 
      throws DuplicateKeyException, UnknownKeyException, MalformedURLException
    {
        try
        {
            String id = store.getID();
            Logger logger = getLogger().getChildLogger( id );
            LayoutRegistryModel registry = getLayoutRegistryModel();
            HostModel model = new DefaultHostModel( logger, store, registry );
            addHostModel( model, notify );
        }
        catch( RemoteException e )
        {
            throw new ModelRuntimeException( e.getMessage(), e );
        }
    }

   /**
    * Internal processing of an event.
    * @param event the event
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof CacheEvent )
        {
            processCacheEvent( (CacheEvent) event );
        }
        else if( event instanceof CacheDirectoryChangeEvent )
        {
            processCacheDirectoryChangeEvent( (CacheDirectoryChangeEvent) event );
        }
        else
        {
            super.processEvent( event );
        }
    }

    private void processCacheEvent( CacheEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof CacheListener )
            {
                CacheListener listener = (CacheListener) eventListener;
                if( event instanceof HostAddedEvent )
                {
                    try
                    {
                        listener.hostAdded( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "CacheListener host addition notification error.";
                        getLogger().error( error, e );
                    }
                }
                else if( event instanceof HostRemovedEvent )
                {
                    try
                    {
                        listener.hostRemoved( event );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "CacheListener host removed notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }

    private void processCacheDirectoryChangeEvent( CacheDirectoryChangeEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof CacheListener )
            {
                CacheListener cl = (CacheListener) listener;
                try
                {
                    cl.cacheDirectoryChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "CacheListener host addition notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }

    private HostModel[] sortHosts()
    {
        synchronized( getLock() )
        {
            HostModel[] hosts = (HostModel[]) m_list.toArray( new HostModel[0] );
            HostModel[] list = new HostModel[ hosts.length ];
            for( int i=0; i < hosts.length; i++ )
            {
                list[i] = hosts[i];
            }
            Arrays.sort( list );
            return list;
        }
    }

    private File getAnchorDirectory()
    {
        return Transit.DPML_DATA;
    }

   /**
    * Host addition event.
    */
    static class HostAddedEvent extends CacheEvent 
    {
        private int m_priority;

       /**
        * Creation of a new host addition event.
        * @param source the source cache model
        * @param host the host model that was added
        */
        public HostAddedEvent( CacheModel source, HostModel host )
        {
            super( source, host );
        }
    }

   /**
    * Host removal event.
    */
    static class HostRemovedEvent extends CacheEvent 
    {
       /**
        * Creation of a new host removal event.
        * @param source the source cache model
        * @param host the host model that was removed
        */
        public HostRemovedEvent( CacheModel source, HostModel host )
        {
            super( source, host );
        }
    }
}

