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

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Set;
import java.util.TreeSet;

import net.dpml.lang.Logger;

import net.dpml.transit.info.LayoutDirective;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.HostDirective;
import net.dpml.transit.info.ContentDirective;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.CacheListener;
import net.dpml.transit.model.CacheDirectoryChangeEvent;
import net.dpml.transit.model.CacheEvent;
import net.dpml.transit.util.PropertyResolver;

import net.dpml.lang.DuplicateKeyException;
import net.dpml.lang.UnknownKeyException;


/**
 * Default implementation of the cache model that maintains information 
 * about the current cache directory and the associated hosts.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultCacheModel extends DefaultModel implements CacheModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final String m_path;
    
    private final Set m_list = Collections.synchronizedSortedSet( new TreeSet() );

    private final File m_cache;

    private final LayoutModel m_layout;

    private final LayoutRegistryModel m_registry;
    
    private final ContentRegistryModel m_content;

    private HostModel[] m_sortedHosts;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construct a new cache model.
    *
    * @param logger the assigned logging channel
    * @param directive the cache configuration directive
    * @param registry a registry of layout models
    * @param content a registry of content models
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultCacheModel( Logger logger, CacheDirective directive )
      throws Exception
    {
        super( logger );
        
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        
        try
        {
            Logger log = logger.getChildLogger( "layout" );
            LayoutDirective[] layouts = directive.getLayoutDirectives();
            m_registry = new DefaultLayoutRegistryModel( log, layouts );
            String layout = directive.getCacheLayout();
            m_layout = m_registry.getLayoutModel( layout );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected internal error while constructing layout registry model.";
            throw new ModelRuntimeException( error, e );
        }
        
        try
        {
            Logger log = logger.getChildLogger( "content" );
            ContentDirective[] content = directive.getContentDirectives();
            m_content = new DefaultContentRegistryModel( log, content );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected internal error while constructing content handler registry model.";
            throw new ModelRuntimeException( error, e );
        }
        
        // set the cache directory
        
        m_path = directive.getCache();
        String resolved = PropertyResolver.resolve( m_path );
        File cache = new File( resolved );
        if( !cache.isAbsolute() )
        {
            File anchor = getAnchorDirectory();
            cache = new File( anchor, resolved );
            cache.mkdirs();
        }
        logger.debug( "setting cache: " + cache ); 
        m_cache = cache;

        // setup the standard local respository host
        
        m_sortedHosts = sortHosts();
        String localPath = directive.getLocal();
        String localLayout = directive.getLocalLayout();
        HostDirective local = createLocalHostDirective( localPath, localLayout );
        addHostModel( local, false );
        
        // setup the supplimentary hosts
        
        HostDirective[] hosts = directive.getHostDirectives();
        for( int i=0; i < hosts.length; i++ )
        {
            HostDirective host = hosts[i];
            addHostModel( host, false );
        }
    }
    
    // ------------------------------------------------------------------------
    // CacheModel
    // ------------------------------------------------------------------------

   /**
    * Return the model maintaining configuration information about
    * the content registry.
    *
    * @return the content model
    * @exception RemoteException if a remote exception occurs
    */
    public ContentRegistryModel getContentRegistryModel()
    {
        return m_content;
    }

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
    * Return the cache directory path.
    * @return the cache path.
    * @exception RemoteException if a remote exception occurs
    */
    public String getCacheDirectoryPath()
    {
        return m_path;
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
    public synchronized void dispose()
    {
        HostModel[] hosts = getHostModels();
        for( int i=0; i<hosts.length; i++ )
        {
            HostModel host = hosts[i];
            dispose( host );
        }
        dispose( m_registry );
        dispose( m_content );
        super.dispose();
    }
    
    private void dispose( Object object )
    {
        if( object instanceof Disposable )
        {
            Disposable disposable = (Disposable) object;
            disposable.dispose();
        }
        if( object instanceof Remote )
        {
            try
            {
                Remote remote = (Remote) object;
                UnicastRemoteObject.unexportObject( remote, true );
            }
            catch( NoSuchObjectException e )
            {
                // ignore
            }
            catch( RemoteException re )
            {
                getLogger().warn( "Unexpected error during remote reference removal.", re );
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // internal
    // ------------------------------------------------------------------------

   /**
    * Add a new host model to the cache model.
    * @param directive the host model configuration
    * @param notify if true issue a notification event of host model addition
    * @exception DuplicateKeyException a host model with a matching id already exists
    * @exception UnknownKeyException the host model requests a layout model id that is not recognized
    * @exception MalformedURLException the host model base url is malformed
    * @exception RemoteException if a remote exception occurs
    */
    void addHostModel( HostDirective directive, boolean notify ) 
      throws DuplicateKeyException, UnknownKeyException, MalformedURLException
    {
        try
        {
            String id = directive.getID();
            Logger logger = getLogger().getChildLogger( id );
            LayoutRegistryModel registry = getLayoutRegistryModel();
            HostModel model = new DefaultHostModel( logger, directive, registry );
            addHostModel( model, notify );
        }
        catch( RemoteException e )
        {
            throw new ModelRuntimeException( e.getMessage(), e );
        }
    }

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
    
    private HostDirective createLocalHostDirective( String path, String layout )
    {
        return new HostDirective( 
          "local", 
          10, 
          path, 
          null, 
          null, 
          null, 
          true, 
          true, 
          layout, 
          null, 
          null );
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

