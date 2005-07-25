/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.dpml.transit.artifact.ArtifactAlreadyExistsException;
import net.dpml.transit.artifact.ArtifactAlreadyExistsException;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.CacheListener;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.CacheDirectoryChangeEvent;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.CacheEvent;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.LayoutRegistryModel;
import net.dpml.transit.monitor.Adapter;
import net.dpml.transit.monitor.RepositoryMonitorAdapter;
import net.dpml.transit.monitor.CacheMonitorAdapter;
import net.dpml.transit.monitor.NetworkMonitorAdapter;
import net.dpml.transit.monitor.CacheMonitorRouter;
import net.dpml.transit.monitor.Monitor;
import net.dpml.transit.repository.Repository;
import net.dpml.transit.util.PropertyResolver;
import net.dpml.transit.util.Util;

/**
 * Default cache handler that maintains a file based cache.  
 */
public class DefaultCacheHandler extends UnicastRemoteObject implements CacheHandler, CacheListener, Handler
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The base directory of the cache.
    */
    private File m_cacheDir;

   /**
    * The resource hosts.
    */
    private TreeMap m_resourceHosts;

    private Layout m_resolver;

    private ZipCache m_zipCache;

    private final Logger m_logger;

    private final CacheModel m_model;

    private final Map m_plugins = new WeakHashMap();  // plugin uris as keys to plugin classes

    private LayoutRegistry m_registry;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new file based cache controller using a supplied
    * configuration model and cache layout resolver.
    *
    * @param model the cache system confiuguration model
    */
    public DefaultCacheHandler( CacheModel model, Logger logger ) throws IOException
    {
        super();

        m_model = model;
        m_logger = logger;
        m_zipCache = new ZipCache();
        m_resourceHosts = new TreeMap();

        LayoutRegistryModel layoutModel = model.getLayoutRegistryModel();
        m_registry = new DefaultLayoutRegistry( layoutModel, logger );
        LayoutModel layout = model.getLayoutModel();
        try
        {
            String key = layout.getID();
            m_resolver = m_registry.getLayout( key );
        }
        catch( Throwable e )
        {
            final String error = 
              "Cannot construct cache handler due to a cache layout resolvution failure.";
            throw new TransitException( error, e );
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "cache initialization" );
        }

        //
        // For all of the declared host models we check for hosts that do not
        // declare a plugin uri.  If no plugin uri is declared we are dealing with
        // a classic resource host that serves as a bootstrap host.  Leter on the
        // SecuredTransitContext will initialize this instance and we will continue
        // with host loading for plugin based resource hosts.
        //

        HostModel[] hosts = model.getHostModels();
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "host count: " + hosts.length );
        }
        for( int i=0; i<hosts.length; i++ )
        {
            HostModel host = hosts[i];
            if( null == host.getCodeBaseURI() )
            {
                String id = host.getID();
                ResourceHost handler = createDefaultResourceHost( host );
                m_resourceHosts.put( id, handler );
            }
        }
        
        //
        // setup the cache directory
        //

        File cache = model.getCacheDirectory();
        setLocalCacheDirectory( cache );

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "bootstrap initialization complete" );
        }
    }

    // ------------------------------------------------------------------------
    // CacheListener
    // ------------------------------------------------------------------------

   /**
    * Return a file referencing the the locally cached resource.
    *
    * @return the cached file
    */
    public File getLocalFile( Artifact artifact ) throws IOException
    {
        File cache = getLocalCacheDirectory();
        String name = m_resolver.resolvePath( artifact );
        return new File( cache, name );
    }

   /**
    * Notify the listener of a change to the cache directory.
    * @param event the cache directory change event
    */
    public void cacheDirectoryChanged( CacheDirectoryChangeEvent event ) throws RemoteException
    {
        File cache = m_model.getCacheDirectory();
        synchronized( this )
        {
            setLocalCacheDirectory( cache );
        }
    }

   /**
    * Notify the listener of the addition of a new host.
    * @param event the host added event
    */
    public void hostAdded( CacheEvent event ) throws RemoteException
    {
        HostModel host = event.getHostModel();
        try
        {
            handleHostAddition( host );
        }
        catch( Throwable e )
        {
            final String error =
              "An internal error occured while attempting to handle host addition.";
            getLogger().error( error, e );
        }
    }

   /**
    * Notify the listener of the removal of a host.
    * @param event the host removed event
    */
    public void hostRemoved( CacheEvent event ) throws RemoteException
    {
        synchronized( m_resourceHosts )
        {
            HostModel model = event.getHostModel();
            String id = model.getID();
            ResourceHost host = (ResourceHost) m_resourceHosts.get( id );
            if( null != host )
            {
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "removing host: " + id );
                }
                if( host instanceof Handler )
                {
                    Handler handler = (Handler) host;
                    handler.dispose();
                }
                m_resourceHosts.remove( id );
            }
        }
    }

    // ------------------------------------------------------------------------
    // Handler
    // ------------------------------------------------------------------------

   /**
    * Dispose of the manager.  During disposal a manager is required to 
    * release all references such as listeners and internal resources
    * in preparation for garbage collection.
    */
    public void dispose()
    {
        try
        {
            m_model.removeCacheListener( this );
            synchronized( m_resourceHosts )
            {

                ResourceHost[] hosts = (ResourceHost[]) m_resourceHosts.values().toArray( new ResourceHost[0] );
                for( int i=0; i<hosts.length; i++ )
                {
                    ResourceHost host = hosts[i];
                    if( host instanceof Handler )
                    {
                        Handler handler = (Handler) host;
                        handler.dispose();
                    }
                }
                m_resourceHosts.clear();
            }
        }
        catch( RemoteException e )
        {
            final String warning = 
              "Unexpected remote exception occured while attempting to dispose of the cache handler.";
            getLogger().error( warning, e );
        }
    }

    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

   /**
    * Initialization of the cache controller.  This operation is invoked by
    * the secure transit context following the establishment of bootstrap
    * services.  During initialization the implementation loads any custom
    * resource hosts.
    *
    * @exception TransitException if a custom host deployment error occurs
    */
    public void initialize() throws IOException
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "secondary initialization phase" );
        }

        m_model.addCacheListener( this );

        HostModel[] hosts = m_model.getHostModels();
        for( int i=0; i<hosts.length; i++ )
        {
            HostModel host = hosts[i];
            if( null != host.getCodeBaseURI() )
            {
                try
                {
                    handleHostAddition( host );
                }
                catch( Throwable e )
                {
                    //
                    // TODO: mark the host model as invalid by apply
                    // the exception to the model
                    //

                    final String error =
                      "Dropping custom resource host due to a deployment failure."
                      + "\nHost ID: " + host.getID();
                    getLogger().error( error, e );
                }
            }
        }

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "cache subsystem established" );
        }
    }

   /**
    * Set the local cache directory.
    * @param file the cache directory
    */
    private synchronized void setLocalCacheDirectory( File file )
    {
        if( null == file )
        {
            throw new NullArgumentException( "file" );
        }
        File cache = file;
        if( null == m_cacheDir )
        {
             final String message =
               "Setting cache to: "
               + cache;
             getLogger().debug( message );
        }
        else
        {
            final String message =
              "Changing cache:"
              + "\nOld: " + m_cacheDir
              + "\nNew: " + cache;
             getLogger().debug( message );
        }
        if( false == cache.isAbsolute() )
        {
            cache = new File( Transit.DPML_DATA, file.toString() );
        }
        cache.mkdirs();
        m_cacheDir = cache;
    }

   /**
    * Return a directory established to provide the local cache.
    *
    * @return the local cache directory
    */
    protected File getLocalCacheDirectory()
    {
        return m_cacheDir;
    }

    // ------------------------------------------------------------------------
    // CacheHandler
    // ------------------------------------------------------------------------

    /**
     * Attempts to download and cache a remote artifact using a set of remote
     * repositories.  The operation is not fail fast and so it keeps trying if
     * the first repository does not have the artifact in question.
     *
     * @param artifact the artifact to retrieve and cache
     * @return input stream containing the artifact content.
     * @exception IOException if an IO error occurs.
     * @exception TransitException if a transit system error occurs.
     * @exception NullArgumentException if the artifact argument is null.
     */
    public InputStream getResource( Artifact artifact )
        throws IOException, TransitException, NullArgumentException
    {
        File destination = getResourceFile( artifact );

        if( destination.exists() )
        {
            FileInputStream stream = new FileInputStream( destination );
            return new BufferedInputStream( stream );
        }

        String error = "Unresolvable artifact: [" + artifact + "]. (" + destination + ")";
        throw new ArtifactNotFoundException( error, artifact.toURI() );
    }

    /**
     * Attempts to download and cache a remote artifact using a set of remote
     * repositories.
     * <p>
     *   This method allows an internal reference to be passed to the
     *   cache handler and it is expected to return the InputStream of the
     *   internal item inside Jar/Zip files. If this method is called, the
     *   implementation can assume that the artifact is a Zip file.
     * </p>
     *
     * @param artifact the artifact to retrieve and cache
     * @param internalReference referencing a item within the artifact. This
     *        argument may start with "!" or "!/", which should be ignored.
     * @return a file referencing the local resource
     * @exception IOException if an IO error occurs
     * @exception TransitException is a transit system error occurs
     */
    public InputStream getResource( Artifact artifact, String internalReference )
        throws IOException, TransitException
    {
        synchronized( this )
        {
            if( internalReference.startsWith( "!" ) )
                internalReference = internalReference.substring( 1 );
            if( internalReference.startsWith( "/" ) )
                internalReference = internalReference.substring( 1 );

            ZipFile zip = m_zipCache.get( artifact );
            if( zip == null )
            {
                File resourceFile = getResourceFile( artifact );
                zip = new ZipFile( resourceFile );
                m_zipCache.put( artifact, zip );
            }
            ZipEntry entry = zip.getEntry( internalReference );
            InputStream stream = zip.getInputStream( entry );
            return new BufferedInputStream( stream );
        }
    }

    /** Creates an output stream to where the artifact content can be written
     *  to.
     * <p>
     *   If the artifact already exists and the artifact is not a link a 
     *   <code>ArtifactAlreadyExistsException</code>
     *   will be thrown. If the directory doesn't exists, it will be created.
     * </p>
     * @exception IOException if an IO error occurs.
     * @exception NullArgumentException if the artifact argument is null.
     * @exception ArtifactAlreadyExistsException if the artifact already exists
     *            in the cache and the artifact is not a link.
     */
    public OutputStream createOutputStream( Artifact artifact )
        throws NullArgumentException, ArtifactAlreadyExistsException, IOException
    {
        if( null == artifact )
        {
            throw new NullArgumentException( "artifact" );
        }
        ResourceHost any = findAnyPresence( artifact );
        String scheme = artifact.getScheme();
        boolean flag = !"link".equals( scheme );
        if( ( any != null ) && flag )
        {
            throw new ArtifactAlreadyExistsException( "Artifact found on server.", artifact );
        }
        String path = m_resolver.resolvePath( artifact );
        File destination = new File( m_cacheDir, path );
        if( destination.exists() && flag )
        {
            throw new ArtifactAlreadyExistsException( "Artifact found in cache.", artifact );
        }
        File parentDir = destination.getParentFile();
        if( !parentDir.exists() )
        {
            parentDir.mkdirs();
        }
        return new FileOutputStream( destination );
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Return a resource host.
    * @param artifact the artifact
    * @return the resource host (possibly null)
    */
    private ResourceHost findKnownGroupHost( Artifact artifact )
    {
        synchronized( m_resourceHosts )
        {
            Iterator list = m_resourceHosts.values().iterator();
            while ( list.hasNext() )
            {
                ResourceHost host = (ResourceHost) list.next();
                if( host.isEnabled() )
                {
                    if( host.checkPresence( artifact, true ) )
                    {
                        return host;
                    }
                }
            }
            return null;
        }
    }

   /**
    * Find any host.
    * @param artifact the artifact
    * @return the resource host (possibly null)
    */
    private ResourceHost findAnyPresence( Artifact artifact )
    {
        synchronized( m_resourceHosts )
        {
            Iterator list = m_resourceHosts.values().iterator();
            while ( list.hasNext() )
            {
                ResourceHost host = (ResourceHost) list.next();
                if( host.isEnabled() )
                {
                    if( host.checkPresence( artifact, false ) )
                    {
                        return host;
                    }
                }
            }
            return null;
        }
    }

   /**
    * Download an artifact from a host to the cache.
    * @param host the remote host
    * @param artifact the artifact being retrieved
    * @param destination the cached destination
    * @return TRUE if downloaded
    * @exception IOException if an IO error occurs
    * @exception TransitException if a transit system error occurs
    */
    private boolean download( ResourceHost host, Artifact artifact, File destination )
        throws IOException, TransitException
    {
        if( host == null )
        {
            return false;
        }
        File parentDir = destination.getParentFile();
        File tempFile = File.createTempFile( "~dpml", ".tmp", parentDir );
        tempFile.deleteOnExit(); // safety harness in case we abort abnormally
                                 // like a Ctrl-C.
        FileOutputStream tempOut = new FileOutputStream( tempFile );

        CacheMonitorRouter monitor = Transit.getInstance().getCacheMonitorRouter();
        try
        {
            Date lastModified = host.download( artifact, tempOut );
            // An atomic operation and no risk of a corrupted
            // artifact content.
            tempFile.renameTo( destination );
            destination.setLastModified( lastModified.getTime() );
            return true;
        }
        catch( IOException e )
        {
            tempFile.delete();
            if( monitor != null )
            {
                monitor.failedDownloadFromHost( host.toString(), artifact, e );
            }
            return false;
        }
    }

    private void endNotifyMonitor( CacheMonitorRouter monitor, boolean existed,
                                   Artifact artifact, File destination )
    {
        if( monitor != null )
        {
            if( existed )
            {
                monitor.updatedLocalCache( artifact.toURL(), destination );
            }
            else
            {
                monitor.addedToLocalCache( artifact.toURL(), destination );
            }
        }
    }

    private void checkInternalConsistency( Artifact artifact, File destination )
    {
        if( destination.exists() )
            return;
        String error = "Download reported [success], but the destination does not exist: "
            + artifact + ", " + destination;
        throw new InternalError( error );
    }

    /** Locates and if necessary downloads the artifact.
     * @param artifact the Artifact to download and locate in the cache.
     * @return the File pointing to the artifact. The file may not exist if the
     *         download has failed.
     */
    private File getResourceFile( Artifact artifact )
        throws TransitException, IOException
    {
        CacheMonitorRouter monitor = Transit.getInstance().getCacheMonitorRouter();
        if( monitor != null )
        {
            monitor.resourceRequested( artifact );
        }

        if( null == artifact )
        {
            throw new NullArgumentException( "artifact" );
        }
        String path = m_resolver.resolvePath( artifact );
        File destination = new File( m_cacheDir, path );
        File parentDir = destination.getParentFile();
        parentDir.mkdirs();
        boolean exist = destination.exists();
        boolean success;
        if( exist )
        {
            success = true;
        }
        else
        {
            ResourceHost known = findKnownGroupHost( artifact );
            success = download( known, artifact, destination );
            if( !success )
            {
                ResourceHost any = findAnyPresence( artifact );
                success = download( any, artifact, destination );
            }
        }
        if( success )
        {
            checkInternalConsistency( artifact, destination );
            endNotifyMonitor( monitor, exist, artifact, destination );
        }

        if( !destination.exists() && monitor != null )
        {
            monitor.failedDownload( artifact );
        }

        return destination;
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private void handleHostAddition( HostModel model )
      throws UnknownHostException, TransitException, IOException
    {
        synchronized( m_resourceHosts )
        {
            String id = model.getID();
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "adding host: " + id );
            }
            if( null == m_resourceHosts.get( id ) )
            {
                ResourceHost host = createResourceHost( model );
                m_resourceHosts.put( id, host );
            }
            else
            {
                final String error =
                  "Illegal attempt to override existing host handler: " + id;
                throw new TransitException( error );
            }
        }
    }

    private ResourceHost createResourceHost( HostModel model )
      throws UnknownHostException, TransitException, IOException
    {
        URI uri = model.getCodeBaseURI();
        if( null == uri )
        {
            return createDefaultResourceHost( model );
        }
        else
        {
            return loadResourceHost( model );
        }
    }

    protected ResourceHost loadResourceHost( HostModel model ) throws IOException
    {
        Class clazz = loadResourceHostClass( model );
        Repository loader = Transit.getInstance().getRepository();
        try
        {
            return (ResourceHost) loader.instantiate( clazz, new Object[]{ model, m_registry } );
        }
        catch( Throwable e )
        { 
            final String error =  
              "Unable to load request host due to an instantiation failure."
              + "\nHost ID: " + model.getID();
            throw new TransitException( error, e );
        }
    }

    protected Class loadResourceHostClass( HostModel model ) throws IOException
    {
        URI uri = model.getCodeBaseURI();
        Class clazz = (Class) m_plugins.get( uri );
        if( null != clazz )
        {
            return clazz;
        }
        else
        {
            try
            {
                getLogger().debug( "loading host plugin: " + uri );
                Repository loader = Transit.getInstance().getRepository();
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                clazz = loader.getPluginClass( classloader, uri );
            }
            catch( Exception e )
            {
                final String error =
                  "Unable to load a content handler plugin due to an unexpected exception.";
                throw new TransitException( error, e );
            }

            if( ResourceHost.class.isAssignableFrom( clazz ) )
            {
                m_plugins.put( uri, clazz );
                return clazz;
            }
            else
            {
                final String error =
                  "Host plugin is not assignable to a net.dpml.transit.host.ResourceHost."
                  + "\nPlugin URI: " + uri
                  + "\nPlugin Class: " + clazz.getName();
                throw new TransitException( error );
            }
        }
    }

    private ResourceHost createDefaultResourceHost( HostModel model ) throws IOException
    {
        if( getLogger().isDebugEnabled() )
        {
            if( model.isBootstrap() )
            {
                final String message =
                  "Creating bootstrap host ["
                   + model.getID()
                   + "] on " + model.getBaseURL();
                getLogger().debug( message );
            }
            else
            {
                final String message =
                  "Creating host ["
                   + model.getID()
                   + "] on " + model.getBaseURL();
                getLogger().debug( message );
            }
        }
        try
        {
            return new DefaultResourceHost( model, m_registry, m_logger );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected exception while attempting to load standard host: " + model.getID()
              + "\nBase URL: " + model.getBaseURL();
            throw new TransitException( error, e );
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

