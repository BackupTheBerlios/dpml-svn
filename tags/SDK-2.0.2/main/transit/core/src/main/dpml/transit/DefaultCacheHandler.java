/*
 * Copyright 2004-2007 Stephen J. McConnell.
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

package dpml.transit;

import dpml.util.PropertyResolver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Date;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.dpml.transit.Artifact;
import net.dpml.transit.CacheManager;
import net.dpml.transit.Layout;
import net.dpml.transit.HostManager;
import net.dpml.transit.Transit;
import net.dpml.transit.TransitException;
import net.dpml.transit.ArtifactAlreadyExistsException;
import net.dpml.transit.ArtifactNotFoundException;
import net.dpml.transit.Monitor;

import dpml.transit.info.CacheDirective;
import dpml.transit.info.HostDirective;

import net.dpml.util.Logger;

import static net.dpml.transit.Transit.DATA;

/**
 * Default cache handler that maintains a file based cache.  
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultCacheHandler implements Cache, CacheManager
{
    private static final File TRANSIT_DATA = DATA;
    
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The immutable cache configuration.
    */
    private final CacheDirective m_directive;
    
   /**
    * The cache base directory.
    */
    private File m_cacheDir;

   /**
    * The resource hosts.
    */
    private TreeSet<Host> m_resourceHosts = new TreeSet<Host>();

   /**
    * The cache layout strategy.
    */
    private Layout m_layout;

   /**
    * Internal zip file cache.
    */
    private ZipCache m_zipCache;

   /**
    * Internal logger.
    */
    private final Logger m_logger;
    
    private final TransitContext m_context;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new file based cache controller using a supplied
    * configuration model.
    *
    * @param server JMX server (may be null)
    * @param directive the cache configuration directive
    * @param logger the assigned logging channel
    * @exception IOException if an IO error occurs
    */
    DefaultCacheHandler( 
      MBeanServer server, TransitContext context, CacheDirective directive, Logger logger ) throws Exception
    {
        super();

        m_directive = directive;
        m_logger = logger;
        m_context = context;
        
        m_zipCache = new ZipCache();

        String key = directive.getCacheLayout();
        m_layout = Layout.getLayout( key );
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "cache initialization" );
        }

        HostDirective local = createLocalHostDirective( directive );
        Host system = createDefaultResourceHost( local );
        m_resourceHosts.add( system );

        HostDirective[] hosts = directive.getHostDirectives();
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "host count: " + hosts.length );
        }
        for( int i=0; i < hosts.length; i++ )
        {
            HostDirective host = hosts[i];
            String id = host.getID();
            Host handler = createDefaultResourceHost( host );
            m_resourceHosts.add( handler );
        }
        
        //
        // setup the cache directory
        //

        String path = directive.getCache();
        File cache = resolveCacheDirectory( path );
        cache.mkdirs();
        m_cacheDir = cache;
        
        if( logger.isTraceEnabled() )
        {
            logger.trace( "setting cache: " + cache );
        }
        
        if( null != server )
        {
            Hashtable<String, String> table = new Hashtable<String, String>();
            table.put( "type", "Cache" );
            ObjectName name =
              ObjectName.getInstance( "net.dpml.transit", table );
            server.registerMBean( this, name );
            
            HostManager[] hostManagers = getHosts();
            for( int i=0; i < hostManagers.length; i++ )
            {
                HostManager host = hostManagers[i];
                registerHostManager( server, host );
            }
        }

        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "cache initialization complete" );
        }
    }
    
    private void registerHostManager( 
      final MBeanServer server, final HostManager host ) throws Exception
    {
        String id = host.getID();
        Hashtable<String, String> table = new Hashtable<String, String>();
        table.put( "type", "Cache" );
        table.put( "name", id );
        ObjectName name =
          ObjectName.getInstance( "net.dpml.transit", table );
        server.registerMBean( host, name );
    }
    
    private File resolveCacheDirectory( final String path )
    {
        Properties properties = System.getProperties();
        String resolved = PropertyResolver.resolve( properties, path );
        File cache = new File( resolved );
        if( !cache.isAbsolute() )
        {
            File anchor = Transit.DATA;
            return new File( anchor, resolved );
        }
        else
        {
            return cache;
        }
    }
    
   /**
    * Return a file referencing the the locally cached resource.
    *
    * @return the cached file
    */
    public File getLocalFile( Artifact artifact ) throws IOException
    {
        File cache = getCacheDirectory();
        String name = m_layout.resolvePath( artifact );
        return new File( cache, name );
    }

    // ------------------------------------------------------------------------
    // CacheManager
    // ------------------------------------------------------------------------

    public String getPath()
    {
        return m_directive.getCache(); 
    }
    
   /**
    * Return the current cache directory.
    * @return the cache directory.
    */
    public String getDirectory()
    {
        try
        {
            return m_cacheDir.getCanonicalPath();
        }
        catch( IOException e )
        {
            return m_cacheDir.toString();
        }
    }

   /**
    * Return the Transit cache layout id.
    * @return the cache layout identifier
    */
    public String getLayoutID()
    {
        return getLayout().getID();
    }

    public HostManager[] getHosts()
    {
        return (HostManager[]) m_resourceHosts.toArray( new HostManager[0] ); 
    }

    // ------------------------------------------------------------------------
    // CacheHandler
    // ------------------------------------------------------------------------

   /**
    * Return the current cache directory.
    * @return the cache directory.
    */
    public File getCacheDirectory()
    {
        return m_cacheDir;
    }

   /**
    * Attempts to download and cache a remote artifact using a set of remote
    * repositories.  The operation is not fail fast and so it keeps trying if
    * the first repository does not have the artifact in question.
    *
    * @param artifact the artifact to retrieve and cache
    * @return input stream containing the artifact content.
    * @exception IOException if an IO error occurs.
    * @exception TransitException if a transit system error occurs.
    * @exception NullPointerException if the artifact argument is null.
    */
    public InputStream getResource( Artifact artifact )
        throws IOException, TransitException, NullPointerException
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
    * @param resource referencing a item within the artifact. This
    *        argument may start with "!" or "!/", which should be ignored.
    * @return a file referencing the local resource
    * @exception IOException if an IO error occurs
    * @exception TransitException is a transit system error occurs
    */
    public InputStream getResource( final Artifact artifact, final String resource )
        throws IOException, TransitException
    {
        String internalReference = resource;
        synchronized( this )
        {
            if( internalReference.startsWith( "!" ) )
            {
                internalReference = internalReference.substring( 1 );
            }
            if( internalReference.startsWith( "/" ) )
            {
                internalReference = internalReference.substring( 1 );
            }
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

   /** 
    * Creates an output stream to where the artifact content can be written
    * to. If the artifact already exists and the artifact is not a link a 
    * <code>ArtifactAlreadyExistsException</code> will be thrown. If the 
    * directory doesn't exists, it will be created.
    * 
    * @exception IOException if an IO error occurs.
    * @exception NullPointerException if the artifact argument is null.
    * @exception ArtifactAlreadyExistsException if the artifact already exists
    *            in the cache and the artifact is not a link.
    */
    public OutputStream createOutputStream( Artifact artifact )
        throws NullPointerException, ArtifactAlreadyExistsException, IOException
    {
        if( null == artifact )
        {
            throw new NullPointerException( "artifact" );
        }
        Host any = findAnyPresence( artifact );
        String scheme = artifact.getScheme();
        boolean flag = !"link".equals( scheme );
        if( ( any != null ) && flag )
        {
            throw new ArtifactAlreadyExistsException( "Artifact found on server.", artifact );
        }
        String path = m_layout.resolvePath( artifact );
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
    
   /**
    * Return the layout used by the cache.
    * @return the cache layout
    */
    public Layout getLayout()
    {
        return m_layout;
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Return a resource host.
    * @param artifact the artifact
    * @return the resource host (possibly null)
    */
    private Host findKnownGroupHost( Artifact artifact )
    {
        synchronized( m_resourceHosts )
        {
            Iterator list = m_resourceHosts.iterator();
            while ( list.hasNext() )
            {
                Host host = (Host) list.next();
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
    private Host findAnyPresence( Artifact artifact )
    {
        synchronized( m_resourceHosts )
        {
            //Iterator list = m_resourceHosts.values().iterator();
            Iterator list = m_resourceHosts.iterator();
            while ( list.hasNext() )
            {
                Host host = (Host) list.next();
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
    private boolean download( Host host, Artifact artifact, File destination )
        throws IOException, TransitException
    {
        if( host == null )
        {
            return false;
        }
        
        File parentDir = destination.getParentFile();
        File tempFile = File.createTempFile( "~dpml", ".tmp", parentDir );
        tempFile.deleteOnExit(); // safety harness in case we abort abnormally
        FileOutputStream tempOut = new FileOutputStream( tempFile );
        
        try
        {
            Date lastModified = host.download( artifact, tempOut );
            // An atomic operation and no risk of a corrupted
            // artifact content.
            tempFile.renameTo( destination );
            destination.setLastModified( lastModified.getTime() );
            return true;
        }
        catch( Throwable e )
        {
            tempFile.delete();
            getMonitor().failedDownloadFromHost( host.toString(), artifact, e );
            return false;
        }
    }
    
    private Monitor getMonitor()
    {
        return m_context.getMonitor();
    }

    private void endNotifyMonitor( 
      boolean existed, Artifact artifact, File destination )
    {
        if( existed )
        {
            getMonitor().updatedLocalCache( artifact.toURL(), destination );
        }
        else
        {
            getMonitor().addedToLocalCache( artifact.toURL(), destination );
        }
    }

    private void checkInternalConsistency( Artifact artifact, File destination )
    {
        if( destination.exists() )
        {
            return;
        }
        final String error = 
          "Download reported [success], but the destination does not exist: "
          + artifact 
          + ", " 
          + destination;
        throw new InternalError( error );
    }

   /** 
    * Locates and if necessary downloads the artifact.
    * @param artifact the Artifact to download and locate in the cache.
    * @return the File pointing to the artifact. The file may not exist if the
    *         download has failed.
    */
    private File getResourceFile( Artifact artifact )
        throws TransitException, IOException
    {
        if( null == artifact )
        {
            throw new NullPointerException( "artifact" );
        }
        getMonitor().resourceRequested( artifact );
        
        String path = m_layout.resolvePath( artifact );
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
            Host known = findKnownGroupHost( artifact );
            success = download( known, artifact, destination );
            if( !success )
            {
                Host any = findAnyPresence( artifact );
                success = download( any, artifact, destination );
            }
        }
        if( success )
        {
            checkInternalConsistency( artifact, destination );
            endNotifyMonitor( exist, artifact, destination );
        }

        if( !destination.exists() )
        {
            getMonitor().failedDownload( artifact );
        }

        return destination;
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private Host createDefaultResourceHost( HostDirective directive ) throws IOException
    {
        if( getLogger().isTraceEnabled() )
        {
            final String message =
              "Creating host ["
              + directive.getID()
              + "] on " 
              + directive.getHost();
            getLogger().trace( message );
        }
        try
        {
            Monitor monitor = getMonitor();
            return new DefaultResourceHost( monitor, directive, m_logger );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected exception while attempting to load standard host: " + directive.getID()
              + "\nBase URL: " + directive.getHost();
            throw new TransitException( error, e );
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }
    
    private static HostDirective createLocalHostDirective( CacheDirective directive )
    {
        String path = directive.getLocal();
        String layout = directive.getLocalLayout();
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
    
}

