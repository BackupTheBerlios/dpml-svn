/*
 * Copyright 2004 Niclas Hedhman.
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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.PasswordAuthentication;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.RequestIdentifier;
import net.dpml.transit.model.HostListener;
import net.dpml.transit.model.HostNameEvent;
import net.dpml.transit.model.HostChangeEvent;
import net.dpml.transit.model.HostPriorityEvent;
import net.dpml.transit.model.HostLayoutEvent;
import net.dpml.transit.util.Util;

import net.dpml.lang.UnknownKeyException;

/** 
 * This class represents a single host where resources are stored at.
 * <p>
 *   <strong>NOTE:</strong> This ResourceHost does NOT currently support
 *   realm/domain authentication. It may be introduced in the future.
 * </p>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultResourceHost extends UnicastRemoteObject 
  implements ResourceHost, HostListener, Service
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * Known groups.
    */
    private HashSet m_knownGroups = new HashSet();

   /**
    * The key is the Artifact one wants to lock against, and the value is an
    * anonymously lock object.
    */
    private HashMap m_locks = new HashMap();

   /**
    * Connections.
    */
    private ConnectionCache m_connections;

    private LayoutRegistry m_registry;

    private Layout m_layout;

    private HostModel m_model;

    private Logger m_logger;

    private int m_priority;

    private URL m_base;

    private boolean m_enabled;
    private boolean m_trusted;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor for a resource host using a supplied host configuration.
     *
     * @param model the host model
     * @param registry the host layout registry
     * @exception UnknownHostException if the supplied base url references an unknown host
     */
    public DefaultResourceHost( HostModel model, LayoutRegistry registry, Logger logger )
        throws UnknownHostException, UnknownKeyException, IOException
    {
        super();

        m_model = model;
        m_priority = model.getPriority();
        m_base = model.getBaseURL();
        m_trusted = model.getTrusted();
        m_enabled = model.getEnabled();
        m_registry = registry;
        m_logger = logger;
        m_layout = registry.getLayout( model.getLayoutModel().getID() );
        model.addHostListener( this );
        m_connections = ConnectionCache.getInstance();
        String[] groups = setupGroups();
        resetGroups( groups );
        setupAuthenticator();
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
            m_model.removeHostListener( this );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception while disposing of resource host.";
            m_logger.error( error, e );
        }
    }

    // ------------------------------------------------------------------------
    // HostListener
    // ------------------------------------------------------------------------

   /**
    * Notify a consumer of an aggregated set of changes.
    * @param event the host change event
    */
    public void hostChanged( HostChangeEvent event ) throws RemoteException
    {
        synchronized( this )
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "updating runtime" );
            }
            try
            {
                m_base = event.getBaseURL();
                m_enabled = event.getEnabled();
                m_trusted = event.getTrusted();
                setupAuthenticator();
                String[] groups = setupGroups();
                resetGroups( groups );
            }
            catch( Exception e )
            {
                final String error =
                  "Could not complete host update due to a internal error.";
                getLogger().error( error, e );
            }
        }
    }

   /**
    * Notify a consumer of a change to the enabled state.
    * @param event the host event
    */
    public void nameChanged( HostNameEvent event ) throws RemoteException
    {
        // not used by the runtime
    }

   /**
    * Notify a consumer of a change to the host priority.
    * @param event the host event
    */
    public void priorityChanged( HostPriorityEvent event ) throws RemoteException
    {
        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "setting priority to " + event.getPriority() );
        }
        int priority = event.getPriority();
        m_priority = priority;
    }

   /**
    * Notify a consumer of a change to the assigned resolver.
    * @param event the resolver id change event
    */
    public void layoutChanged( HostLayoutEvent event ) throws RemoteException
    {
        synchronized( m_layout )
        {
            try
            {
                final String id = event.getLayoutModel().getID();
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "setting layout to " + id );
                }
                m_layout = m_registry.getLayout( id );
            }
            catch( Exception e )
            {
                final String error =
                  "Layout change could not be completed.";
                getLogger().error( error, e );
            }
        }
    }

    // ------------------------------------------------------------------------
    // ResourceHost
    // ------------------------------------------------------------------------

   /**
    * Return the host name.
    * @return the hostname
    */
    public String getHostName()
    {
        return m_base.getHost();
    }

    /**
     * Returns the full host url.
     *
     * @return the host url
     */
    public URL getURL()
    {
        return m_base;
    }

   /**
    * Return the host priority.
    * @return the priority
    */
    public int getPriority()
    {
        return m_priority;
    }

    /** Downloads the given artifact to the directory indicated.
     * <p>
     *   The cachedir argument is the root cache directory, and the ResourceHost
     *   class is responsible for the creation of the directory structure of the
     *   group if nonexistent.
     * </p>
     * <p>
     *   If the knownOnly argument is true, then only attempt download if the
     *   group is known to exist on this resource host.
     * </p>
     * @param artifact the artifact that is requested to be downloaded.
     * @param dest The output stream where to write the downloaded content.
     * @exception IOException if an IO related error occurs
     */
    public Date download( Artifact artifact, OutputStream dest )
        throws IOException
    {
        Object lock = obtainLock( artifact );
        synchronized ( lock )
        {
            NetworkLoader loader = new NetworkLoader();
            URL url = createRemoteUrl( artifact );

            URLConnection connection;
            synchronized ( m_connections )
            {
                connection = m_connections.get( artifact );
                if( connection == null )
                {
                    // Not in cache. Why not?  --->  Garbage Collection has occurred.
                    String message = this + " - Info: Connection no longer in cache. Reconnecting.";
                    Transit.getInstance().getLogWriter().println( message );
                    URL remote = createRemoteUrl( artifact );
                    connection = remote.openConnection();
                    connection.connect();
                }
            }

            return loader.loadResource( url, connection, dest );
        }
    }

    /** Uploads the given file to the resource host as an artifact.
     *
     * @param artifact the artifact destination specification.
     * @param source The input stream from where to read the content to be uploaded.
     * @exception IOException if an IO related error occurs
     */
    public void upload( Artifact artifact, InputStream source )
        throws IOException
    {
        // TODO: implement upload through HTTP POST requests.
    }

    /** Checks if the Artifact is present on the resource host.
     *
     * <p>
     *   Performs a check to see if the artifact exists on the resource host. If
     *   <i>knownOnly</i> is set to true, then the implementation will only
     *   consult the knownGroups table, and if found there, it is considered
     *   found without checking at the resource host itself. If <i>knownOnly</i>
     *   is false, however, a connection will be established to the resource
     *   host and a check of the actual resource existence.
     * </p>
     *
     * @param artifact the artifact for which the method checks its presence.
     * @param knownOnly does not perform a remote connection, and instead lookup
     *        the group table, and if not found there it will return false.
     *
     * @return true if the artifact can be located, false otherwise.
     */
    public boolean checkPresence( Artifact artifact, boolean knownOnly )
    {
        if( "file".equals( getURL().getProtocol() ) )
        {
            return checkAtServer( artifact );
        }
        else if( knownOnly )
        {
            return checkKnown( artifact );
        }
        else
        {
            return checkAtServer( artifact );
        }
    }

    /**
     * Return true if the reosurce host is enabled and online.
     *
     * @return true if the reosurce host is enabled and online.
     */
    public boolean isEnabled()
    {
        return m_enabled;
    }

    /** Returns true if the ResourceHost is considered trusted.
     *
     * @return true if the ResourceHost is considered trusted.
     */
    public boolean isTrusted()
    {
        return m_trusted;
    }

    /** Returns the layout assigned to the host.
     *
     * @return the layout model
     */
    public Layout getLayout()
    {
        return m_layout;
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

    private String[] setupGroups() throws IOException
    {
        URL index = m_model.getIndexURL();
        if( null != index )
        {
            return getKnownGroups( index );
        }
        else
        {
            return new String[0];
        }
    }

    private void resetGroups( String[] groups )
    {
        synchronized( m_knownGroups )
        {
            m_knownGroups.clear();
            for( int i=0; i < groups.length; i++ )
            {
                String group = groups[i];
                m_knownGroups.add( group );
            }
        }
    }

    private void setupAuthenticator() throws RemoteException
    {
        RequestIdentifier id = m_model.getRequestIdentifier();
        PasswordAuthentication auth = m_model.getAuthentication();
        TransitAuthenticator ta = new TransitAuthenticatorImpl( auth );
        DelegatingAuthenticator da = DelegatingAuthenticator.getInstance();
        da.addTransitAuthenticator( ta, id );
    }

   /**
    * Check if the supplied artifact group is known.
    * @param artifact the subject artifact to check
    * @return TRUE if known else FALSE
    */
    private boolean checkKnown( Artifact artifact )
    {
        String group = artifact.getGroup();
        return m_knownGroups.contains( group );
    }

   /**
    * Ckeck if the server conection is available.
    * @param artifact the subject artifact
    * @return TRUE if a connection is available
    */
    private boolean checkAtServer( Artifact artifact )
    {
        try
        {
            URL remote = createRemoteUrl( artifact );
            String protocol = remote.getProtocol();
            if( protocol.startsWith( "file" ) )
            {
                String path = remote.getPath();
                File f = new File( path );
                return f.exists();
            }
            else
            {
                return openRemoteConnection( artifact );
            }
        }
        catch( IOException e )
        {
            return false;
        }
        catch( NoSuchAlgorithmException e )
        {
            return false;
        }
        catch( KeyManagementException e )
        {
            return false;
        }
        catch( RuntimeException e )
        {
            return false;
        }
    }

   /**
    * Obtain a lock on the supplied artifact.
    * @param artifact the artifact to obtain a lock on
    * @return the lock
    */
    private Object obtainLock( Artifact artifact )
    {
        synchronized ( this )
        {
            Object lock = m_locks.get( artifact );
            if( lock == null )
            {
                lock = new Object();
                m_locks.put( artifact, lock );
            }
            return lock;
        }
    }

   /**
    * Open a remote connection relative to a supplied artifact.
    * @param artifact the artifact against which a connection will be established
    * @exception IOException if an IO error occurs
    * @exception KeyManagementException if a key management error occurs
    * @exception NoSuchAlgorithmException if no such algorithm exists?
    * @return true if the atrifact was found and could be opened, false if not
    *         available at this resource host.
    */
    private boolean openRemoteConnection( Artifact artifact )
        throws IOException, KeyManagementException, NoSuchAlgorithmException
    {
        PrintWriter log = Transit.getInstance().getLogWriter();
        log.println( this + " - opening connection: " + artifact );
        URL remote = createRemoteUrl( artifact );
        URLConnection conn = remote.openConnection();
        if( conn instanceof HttpsURLConnection )
        {
            log.println( this + " - HTTPS connection opened." );
            if( isTrusted() )
            {
                log.println( this + " - Using NullTrustManager." );
                HttpsURLConnection ssl = (HttpsURLConnection) conn;
                TrustManager nullTrustManager = new NullTrustManager();
                SSLContext ctx = SSLContext.getInstance( "SSLv3" );
                ctx.init( null, new TrustManager[]{nullTrustManager}, null );
                log.println( this + " - Setting SSLv3 socket factory." );
                SSLSocketFactory factory = ctx.getSocketFactory();
                ssl.setSSLSocketFactory( factory );
            }
        }
        conn.connect();
        if( conn instanceof HttpURLConnection )
        {
            int code = ( (HttpURLConnection) conn ).getResponseCode();
            log.println( this + " - ResponseCode: " + code );
            if( code == HttpURLConnection.HTTP_UNAUTHORIZED )
            {
                throw new IOException( "Unauthorized request." );
            }
            else if( code == HttpURLConnection.HTTP_NOT_FOUND )
            {
                return false;
            }
            else if( code != HttpURLConnection.HTTP_OK )
            {
                throw new IOException( "Unexpected Result: " + code );
            }
        }
        synchronized ( m_connections )
        {
            log.println( this + " - caching connection: " + conn );
            m_connections.put( artifact, conn );
        }
        return true;
    }

   /**
    * Create a remote urtl from an artifact.
    *
    * @param artifact the subject artifact
    * @return the remote url connection
    * @exception MalformedURLException if the artifact path is not resolvable to a url
    */
    private URL createRemoteUrl( Artifact artifact )
        throws MalformedURLException
    {
        String path = m_layout.resolvePath( artifact );
        URL base = getURL();
        URL url = new URL( base, path );
        return url;
    }

   /**
    * Return a string representation of this host.
    * @return the representation
    */
    public String toString()
    {
        return "[HOST: " + getURL() + "]";
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    // ------------------------------------------------------------------------
    // static (utils)
    // ------------------------------------------------------------------------

   /**
    * A null trust manager that will accept any certificate. I.e. this
    * class performs NO TRUST MANAGEMENT and simply serves as a mechanism
    * through which https connections can be established with the same notion
    * of trust as a http connection (i.e. none).
    */
    private static final class NullTrustManager
        implements X509TrustManager
    {
       /**
        * Empty certificate sequence.
        */
        private static final X509Certificate[] EMPTY_CERTS = new X509Certificate[0];

       /**
        * Null implementation.
        * @param certs the supplied certs (ignored)
        * @param authType the supplied type (ignored)
        */
        public void checkServerTrusted( final X509Certificate[] certs, final String authType )
        {
            PrintWriter log = Transit.getInstance().getLogWriter();
            log.println( "NullTrustManager:  authType=" + authType );
            log.println( "Server Certificates" );
            log.println( "-------------------" );
            for( int i=0; i < certs.length; i++ )
            {
                log.println( "   " + certs[ i ] );
            }
        }

       /**
        * Null implementation.
        * @param certs the supplied certs (ignored)
        * @param authType the supplied type (ignored)
        */
        public void checkClientTrusted( final X509Certificate[] certs, final String authType )
        {
            PrintWriter log = Transit.getInstance().getLogWriter();
            log.println( "NullTrustManager:  authType=" + authType );
            log.println( "Client Certificates" );
            log.println( "-------------------" );
            for( int i=0; i < certs.length; i++ )
            {
                log.println( "   " + certs[ i ] );
            }
        }

       /**
        * Null implementation.
        * @return an empty certificate array
        */
        public X509Certificate[] getAcceptedIssuers()
        {
            return EMPTY_CERTS;
        }
    }

   /**
    * Return a set of known groups froma host.
    * @param href the host against which the request is to be made
    * @return the set of known group names
    * @exception TransitException if an error occurs while attempting to read the
    *   remote address
    */
    private static String[] getKnownGroups( URL href ) throws TransitException
    {
        if( href != null )
        {
            try
            {
                return Util.readListFile( href );
            }
            catch( Exception e )
            {
                final String error =
                  "Unable to extract the groups from "
                  + href;
                throw new TransitException( error, e );
            }
        }
        else
        {
            return new String[0];
        }
    }

   /**
    * HTTP port number.
    */
    private static final int HTTP_PORT = 80;

   /**
    * FTP port number.
    */
    private static final int FTP_PORT = 21;

   /**
    * HTTPS port number.
    */
    private static final int HTTPS_PORT = 443;

}
