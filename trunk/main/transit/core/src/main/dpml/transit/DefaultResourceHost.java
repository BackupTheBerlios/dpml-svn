/*
 * Copyright 2004 Niclas Hedhman.
 * Copyright 2005-2007 Stephen J. McConnell.
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

import dpml.transit.info.HostDirective;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.PasswordAuthentication;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.dpml.transit.Artifact;
import net.dpml.transit.HostManager;
import net.dpml.transit.Layout;
import net.dpml.transit.TransitException;
import net.dpml.transit.Monitor;

import net.dpml.util.Logger;

import static net.dpml.transit.Transit.DATA;

/** 
 * Resource manager for a single host.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultResourceHost implements Host, HostManager, Comparable<Host>
{
    static
    {
        final File data = DATA; // static initialization
    }
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * Known groups.
    */
    private HashSet<String> m_knownGroups = new HashSet<String>();

   /**
    * The key is the Artifact one wants to lock against, and the value is an
    * anonymously lock object.
    */
    private HashMap<Artifact, Object> m_locks = new HashMap<Artifact, Object>();

   /**
    * Connections.
    */
    private ConnectionCache m_connections;
    private Layout m_layout;
    private HostDirective m_directive;
    private Logger m_logger;
    private String m_path;
    private URL m_base;
    private PasswordAuthentication m_authentication;
    private RequestIdentifier m_identifier;
    
    private final Monitor m_monitor;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor for a resource host using a supplied host configuration.
     *
     * @param directive the immutable host configuration
     * @param logger the assigned logging channel
     * @exception UnknownHostException if the supplied base url references an unknown host
     */
    DefaultResourceHost( Monitor monitor, HostDirective directive, Logger logger )
        throws UnknownHostException, IOException
    {
    
        m_logger = logger;
        m_directive = directive;
        m_monitor = monitor;
        
        String layout = directive.getLayout();
        m_layout = Layout.getLayout( layout );
        m_connections = ConnectionCache.getInstance();
        
        String username = directive.getUsername();
        if( null != username )
        {
            char[] pswd = directive.getPassword();
            m_authentication = new PasswordAuthentication( username, pswd );
        }
        else
        {
            m_authentication = new PasswordAuthentication( null, new char[0] );
        }
        
        String id = directive.getID();
        m_path = resolveBaseValue( directive.getHost() );
        m_base = resolveBaseURL( id, m_path );
        
        String scheme = directive.getScheme();
        String prompt = directive.getPrompt();
        m_identifier = getRequestIdentifier( m_base, scheme, prompt );
        
        TransitAuthenticator ta = new TransitAuthenticatorImpl( m_authentication );
        DelegatingAuthenticator da = DelegatingAuthenticator.getInstance();
        da.addTransitAuthenticator( ta, m_identifier );
        
        String index = m_directive.getIndex();
        setGroups( id, m_base, index );
    }

    // ------------------------------------------------------------------------
    // Host
    // ------------------------------------------------------------------------

   /**
    * Return the host base path.
    * @return the base path
    */
    public String getBase()
    {
        return m_path;
    }

   /**
    * Return the hostid.
    * @return the host identifier
    */
    public String getID()
    {
        return m_directive.getID();
    }

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
        return m_directive.getPriority();
    }

   /** 
    * Downloads a given artifact.
    * @param artifact the artifact that is requested to be downloaded.
    * @param dest the destination output stream into which download content is to be written
    * @exception IOException if an IO related error occurs
    */
    public Date download( Artifact artifact, OutputStream dest )
        throws IOException
    {
        Object lock = obtainLock( artifact );
        synchronized ( lock )
        {
            NetworkLoader loader = new NetworkLoader( m_monitor );
            URL url = createRemoteUrl( artifact );
            URLConnection connection;
            synchronized ( m_connections )
            {
                connection = m_connections.get( artifact );
                if( connection == null )
                {
                    Logger log = getLogger().getChildLogger( "log" );
                    if( log.isTraceEnabled() )
                    {
                        String message = this + " - Info: Connection no longer in cache. Reconnecting.";
                        log.trace( message );
                    }
                    URL remote = createRemoteUrl( artifact );
                    connection = remote.openConnection();
                    connection.connect();
                }
            }
            return loader.loadResource( url, connection, dest );
        }
    }

   /** 
    * Uploads the given file to the resource host as an artifact.
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

   /** 
    * Checks if the Artifact is present on the resource host.
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
        return m_directive.getEnabled();
    }

   /** Returns true if the Host is considered trusted.
    *
    * @return true if the Host is considered trusted.
    */
    public boolean isTrusted()
    {
        return m_directive.getTrusted();
    }

   /** 
    * Returns the layout assigned to the host.
    *
    * @return the layout strategy
    */
    public Layout getLayout()
    {
        return m_layout;
    }

   /** 
    * Returns the layout id assigned to the host.
    *
    * @return the layout strategy id
    */
    public String getLayoutID()
    {
        return m_layout.getID();
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

    private void setGroups( String id, URL base, String path ) throws IOException
    {
        URL index = resolveIndexURL( id, base, path );
        if( null != index )
        {
            String[] groups = getKnownGroups( index );
            setGroups( groups );
        }
        else
        {
            setGroups( new String[0] );
        }
    }

    private void setGroups( String[] groups )
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
        Logger log = getLogger().getChildLogger( "log" );
        if( log.isTraceEnabled() )
        {
            String message = this + " - opening connection: " + artifact;
            log.trace( message );
        }
        URL remote = createRemoteUrl( artifact );
        URLConnection conn = remote.openConnection();
        if( conn instanceof HttpsURLConnection )
        {
            if( log.isTraceEnabled() )
            {
                String message = this + " - HTTPS connection opened.";
                log.trace( message );
            }
            if( isTrusted() )
            {
                if( log.isTraceEnabled() )
                {
                    String message = this + " - Using NullTrustManager.";
                    log.trace( message );
                }
                HttpsURLConnection ssl = (HttpsURLConnection) conn;
                TrustManager nullTrustManager = new NullTrustManager();
                SSLContext ctx = SSLContext.getInstance( "SSLv3" );
                ctx.init( null, new TrustManager[]{nullTrustManager}, null );
                if( log.isTraceEnabled() )
                {
                    String message = this + " - Setting SSLv3 socket factory.";
                    log.trace( message );
                }
                SSLSocketFactory factory = ctx.getSocketFactory();
                ssl.setSSLSocketFactory( factory );
            }
        }
        conn.connect();
        if( conn instanceof HttpURLConnection )
        {
            int code = ( (HttpURLConnection) conn ).getResponseCode();
            if( log.isTraceEnabled() )
            {
                String message = this + " - ResponseCode: " + code;
                log.trace( message );
            }
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
            if( log.isTraceEnabled() )
            {
                String message = this + " - caching connection: " + conn;
                log.trace( message );
            }
            m_connections.put( artifact, conn );
        }
        return true;
    }

   /**
    * Create a remote url from an artifact.
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
    
   /**
    * Compare this host with another.  Lower priorities rank higherest.
    * @param host the other host
    * @return the relative ranking of the other host
    */
    public int compareTo( Host host )
    {
        int primaryPriority = getPriority();
        int secondaryPrimary = host.getPriority();
        if( primaryPriority < secondaryPrimary )
        {
            return -1;
        }
        else if( primaryPriority == secondaryPrimary )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    // ------------------------------------------------------------------------
    // static (utils)
    // ------------------------------------------------------------------------

    private static RequestIdentifier getRequestIdentifier( URL base, String scheme, String prompt )
    {
        if( null == base )
        {
            throw new NullPointerException( "base" );
        }
        if( null == scheme )
        {
            throw new NullPointerException( "scheme" );
        }
        if( null == prompt )
        {
            throw new NullPointerException( "prompt" );
        }
        String protocol = base.getProtocol();
        String host = base.getHost();
        int port = base.getPort();
        if( port == 0 )
        {
            if( protocol.equals( "http" ) )
            {
                port = HTTP_PORT;
            }
            else if( protocol.equals( "ftp" ) )
            {
                port = FTP_PORT;
            }
            else if( protocol.equals( "https" ) )
            {
                port = HTTPS_PORT;
            }
        }
        return new RequestIdentifier( host, port, protocol, scheme, prompt );
    }

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
            //PrintWriter log = Transit.getInstance().getLogWriter();
            //log.println( "NullTrustManager:  authType=" + authType );
            //log.println( "Server Certificates" );
            //log.println( "-------------------" );
            //for( int i=0; i < certs.length; i++ )
            //{
            //    log.println( "   " + certs[ i ] );
            //}
        }

       /**
        * Null implementation.
        * @param certs the supplied certs (ignored)
        * @param authType the supplied type (ignored)
        */
        public void checkClientTrusted( final X509Certificate[] certs, final String authType )
        {
            //PrintWriter log = Transit.getInstance().getLogWriter();
            //log.println( "NullTrustManager:  authType=" + authType );
            //log.println( "Client Certificates" );
            //log.println( "-------------------" );
            //for( int i=0; i < certs.length; i++ )
            //{
            //    log.println( "   " + certs[ i ] );
            //}
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
                return PropertyUtils.readListFile( href );
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

    private static String resolveBaseValue( String path )
    {
        //
        // make sure the base path ends with a "/" otherwise relative url references 
        // will not be correct
        //

        if( !path.endsWith( "/" ) )
        {
            return path + "/";
        }
        else
        {
            return path;
        }
    }

    private static URL resolveBaseURL( String id, String path ) throws MalformedURLException
    {
        if( null == path )
        {
            return getDefaultHostURL();
        }
        try
        {
            Properties properties = System.getProperties();
            String spec = PropertyResolver.resolve( properties, path );
            return new URL( spec );
        }
        catch( MalformedURLException e )
        {
            final String error =  
              "Invalid host base url"
              + "\nHost ID: " + id
              + "\nHost Path: " + path
              + "\nCause: " + e.getMessage();
            throw new MalformedURLException( error );
        }
    }

    private static URL resolveIndexURL( String id, URL base, String path ) throws MalformedURLException
    {
        if( null == path )
        {
            return null;
        }

        Properties properties = System.getProperties();
        String resolved = PropertyResolver.resolve( properties, path );

        try
        {
            return new URL( resolved );
        }
        catch( MalformedURLException e )
        {
            try
            {
                return new URL( base, resolved );
            }
            catch( MalformedURLException ee )
            {
                final String error =  
                  "Invalid index url"
                  + "\nHost ID: " + id
                  + "\nHost Path: " + base
                  + "\nIndex Path: " + path
                  + "\nCause: " + e.getMessage();
                throw new MalformedURLException( error );
            }
        }
    }

    private static URL getDefaultHostURL()
    {
        try
        {
            return new URL( "http://localhost" );
        }
        catch( Exception e )
        {
            return null;
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
