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
import java.io.IOException;
import java.rmi.RemoteException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.BackingStoreException;

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
 * @version $Id: StandardTransitDirector.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public class DefaultCacheModel extends DisposableCodeBaseModel 
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

    public DefaultCacheModel( 
      Logger logger, CacheHome home, LayoutRegistryModel registry )
      throws DuplicateKeyException, RemoteException, UnknownKeyException
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

        File cache = home.getCacheDirectory();
        setCacheDirectory( cache, false );

        HostStorage[] hosts = home.getInitialHosts();
        m_sortedHosts = sortHosts();
        for( int i=0; i<hosts.length; i++ )
        {
            HostStorage host = hosts[i];
            addHostModel( host, false );
        }
    }

    // ------------------------------------------------------------------------
    // DisposalListener (listen to modification to the assigned layout)
    // ------------------------------------------------------------------------

   /**
    * Notify the listener of the disposal of a layout.
    */
    public void disposing( DisposalEvent event ) throws VetoDisposalException, RemoteException
    {
        final String message = "Layout currently assigned to cache.";
        throw new VetoDisposalException( this, message );
    }

   /**
    * Notify the listener of the disposal of a manager.
    */
    public void disposed( DisposalEvent event )  throws RemoteException // should never happen
    {
        final String error = 
          "Unexpected notification of disposal of an assigned cache layout.";
        throw new TransitError( error );
    }

    // ------------------------------------------------------------------------
    // CacheModel
    // ------------------------------------------------------------------------

   /**
    * Return the cache layout strategy model.
    * @return the layout model
    */
    public LayoutModel getLayoutModel() throws RemoteException
    {
        return m_layout;
    }

   /**
    * Update the value the local cache directory.
    *
    * @param file the cache directory
    */
    public void setCacheDirectory( final File file ) throws RemoteException
    {
        setCacheDirectory( file, true );
    }

   /**
    * Update the value the local cache directory.
    *
    * @param file the cache directory
    */
    public void setCacheDirectory( final File file, boolean notify ) throws RemoteException
    {
        synchronized( m_lock )
        {
            if( null == file )
            {
                throw new NullPointerException( "file" );
            }
            File cache = file;
            if( false == cache.isAbsolute() )
            {
                File anchor = getAnchorDirectory();
                cache = new File( anchor, file.toString() );
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
            m_home.setCacheDirectory( cache );
            if( notify )
            {
                CacheDirectoryChangeEvent event = new CacheDirectoryChangeEvent( this, m_cache );
                enqueueEvent( event );
            }
        }
    }

    public LayoutRegistryModel getLayoutRegistryModel() throws RemoteException
    {
        return m_registry;
    }

   /**
    * Return an array of host managers assigned to the cache.
     *
    * @return the host manager array
    */
    public HostModel[] getHostModels() throws RemoteException
    {
        synchronized( m_lock )
        {
            return m_sortedHosts;
        }
    }

    public HostModel getHostModel( String id ) throws UnknownKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            HostModel[] managers = getHostModels();
            for( int i=0; i<managers.length; i++ )
            {
                HostModel manager = managers[i];
                if( id.equals( manager.getID() ) )
                {
                    return manager;
                }
            }
            throw new UnknownKeyException( id );
        }
    }

    public void addHostModel( String id ) throws DuplicateKeyException, UnknownKeyException, RemoteException
    {
        HostStorage store = m_home.getHostStorage( id );
        addHostModel( store, true );
    }

    public void addHostModel( HostStorage store, boolean notify ) 
      throws DuplicateKeyException, UnknownKeyException, RemoteException
    {
        String id = store.getID();
        Logger logger = getLogger().getChildLogger( id );
        LayoutRegistryModel registry = getLayoutRegistryModel();
        HostModel model = new DefaultHostModel( logger, store, registry );
        addHostModel( model, notify );
    }

    public void addHostModel( HostModel model ) throws DuplicateKeyException, RemoteException
    {
        addHostModel( model, true );
    }

    public void addHostModel( HostModel manager, boolean notify ) throws DuplicateKeyException, RemoteException
    {
        synchronized( m_lock )
        {
            String id = manager.getID();
            try
            {
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
        }
    }

    public void removeHostModel( HostModel model ) throws RemoteException
    {
        synchronized( m_lock )
        {
            model.dispose();
            m_list.remove( model );
            m_sortedHosts = sortHosts();
            HostRemovedEvent event = new HostRemovedEvent( this, model );
            enqueueEvent( event );
        }
    }

   /**
    * Return the directory to be used by the cache handler as the cache directory.
    * @return the cache directory.
    */
    public File getCacheDirectory() throws RemoteException
    {
        return m_cache;
    }

   /**
    * Add a cache change listener.
    * @param listener the listener to add
    */
    public void addCacheListener( CacheListener listener ) throws RemoteException
    {
        super.addListener( listener );
    }

   /**
    * Remove a cache change listener.
    * @param listener the listener to remove
    */
    public void removeCacheListener( CacheListener listener ) throws RemoteException
    {
        super.removeListener( listener );
    }

    // ------------------------------------------------------------------------
    // Disposable
    // ------------------------------------------------------------------------

    public void dispose() throws RemoteException
    {
        super.dispose();
        m_layout.removeDisposalListener( this );
    }

    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

    public void processEvent( EventObject event )
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
        for( int i=0; i<listeners.length; i++ )
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
        for( int i=0; i<listeners.length; i++ )
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

    private HostModel[] sortHosts() throws RemoteException
    {
        synchronized( m_lock )
        {
            HostModel[] hosts = (HostModel[]) m_list.toArray( new HostModel[0] );
            HostModel[] list = new HostModel[ hosts.length ];
            for( int i=0; i<hosts.length; i++ )
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

    static class HostAddedEvent extends CacheEvent 
    {
        private int m_priority;

        public HostAddedEvent( CacheModel source, HostModel host )
        {
            super( source, host );
        }
    }

    static class HostRemovedEvent extends CacheEvent 
    {
        public HostRemovedEvent( CacheModel source, HostModel host )
        {
            super( source, host );
        }
    }

    class CacheDirectoryChangeEvent extends FileChangeEvent
    {
        public CacheDirectoryChangeEvent( Object source, File file )
        {
            super( source, file );
        }
    }
}

