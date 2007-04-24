/*
 * Copyright 2004-2005 Niclas Hedhman
 * Copyright 2005-2007 Stephen McConnell
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

import java.lang.management.ManagementFactory;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Properties;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.ServiceLoader;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.dpml.util.Logger;
import dpml.util.DefaultLogger;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitManager;
import net.dpml.transit.LinkManager;
import net.dpml.transit.TransitException;
import net.dpml.transit.Layout;
import net.dpml.transit.Monitor;
import net.dpml.transit.ContentHandler;

import dpml.transit.info.ProxyDirective;
import dpml.transit.info.CacheDirective;
import dpml.transit.info.TransitDirective;

/**
 * Initial context of the Transit system used by protocol handlers.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class TransitContext implements TransitManager
{
    //------------------------------------------------------------------
    // static
    //------------------------------------------------------------------

   /**
    * Creation of the transit context.  If the transit context has already
    * been established the method returns the singleton context otherwise a new 
    * context is created relative to the authoritve url and returned.
    * @param directive the transit configuration directive
    * @return the secured transit context
    * @exception TransitException if an error occurs during context creation
    * @exception NullPointerException if the supplied configration directive is null 
    *    and an instance of this class has not been created already.
    */
    public static synchronized TransitContext create( TransitDirective directive )
        throws TransitException, NullPointerException
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        
        if( m_CONTEXT != null )
        {
            final String error = 
              "Transit context already exists.";
            throw new IllegalStateException( error );
        }
        
        Logger logger = new DefaultLogger( "dpml.transit" );
        if( logger.isTraceEnabled() )
        {
            logger.trace( "creating new transit context" );
        }
        
        try
        {
            String jmxEnabled = System.getProperty( "dpml.jmx.enabled" );
            if( "true".equals( jmxEnabled ) )
            {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                m_CONTEXT = new TransitContext( server, directive, logger );
            }
            else
            {
                m_CONTEXT = new TransitContext( null, directive, logger );
            }
        }
        catch( TransitException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            String error = "Transit context creation error.";
            throw new TransitException( error, e );
        }
        
        if( logger.isTraceEnabled() )
        {
            logger.trace( "transit context established" );
        }
        return m_CONTEXT;
    }
    
   /**
    * Return the singleton context.
    * @return the secure context
    */
    public static synchronized TransitContext getInstance()
    {
        if( null == m_CONTEXT )
        {
            final String error = 
              "Transit context has not been initialized.";
            throw new IllegalStateException( error );
        }
        else
        {
            return m_CONTEXT;
        }
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The configuration directive.
    */
    private final TransitDirective m_directive;

   /**
    * The cache handler.
    */
    private final DefaultCacheHandler m_cacheHandler;

   /**
    * The LinkManager instance.
    */
    private final LinkManager m_linkManager;

   /**
    * Logging channel.
    */
    private final Logger m_logger;
    
   /**
    * The singleton router.
    */
    private final DefaultMonitor m_monitor;
    
    //------------------------------------------------------------------
    // constructors
    //------------------------------------------------------------------
    
   /**
    * Creation of a new secured transit context.
    * @param server JMX server (may be null)
    * @param directive the transit configuration directive
    * @param logger the assigned logging channel
    * @exception IOException if an I/O error occurs
    */
    private TransitContext( 
      MBeanServer server, TransitDirective directive, Logger logger ) throws Exception
    {
        m_directive = directive;
        m_logger = logger;
        
        if( null != server )
        {
            Hashtable<String,String> table = new Hashtable<String,String>();
            table.put( "type", "System" );
            ObjectName name =
              ObjectName.getInstance( "net.dpml.transit", table );
            server.registerMBean( this, name );
        }
        
        m_monitor = new DefaultMonitor();
        Logger cacheLogger = logger.getChildLogger( "cache" );
        CacheDirective cacheDirective = directive.getCacheDirective();
        m_cacheHandler = new DefaultCacheHandler( server, this, cacheDirective, cacheLogger );
        m_linkManager = new StandardLinkManager();
        ProxyDirective proxy = directive.getProxyDirective();
        setupProxy( proxy );
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

   /**
    * Returns the Transit monitor.
    * @return the monitor
    */
    public Monitor getMonitor()
    {
        return m_monitor;
    }
    
   /**
    * Add a monitor to Transit.
    * @param monitor the monitor
    */
    public void addMonitor( Monitor monitor )
    {
        m_monitor.addMonitor( monitor );
    }

   /**
    * Return a content handler for the given artifact type.
    * @param type the artifact type
    * @return the content handler or null if unknown
    */
    public ContentHandler getContentHandler( String type )
    {
        ContentHandler[] handlers = getContentHandlers();
        for( ContentHandler handler : handlers )
        {
            String t = handler.getType();
            if( type.equals( t ) )
            {
                if( m_logger.isTraceEnabled() )
                {
                    m_logger.trace( 
                      "selecting handler [" 
                      + handler.getClass().getName() 
                      + "] for the type [" 
                      + type 
                      + "]" );
                }
                return handler;
            }
        }
        return null;
    }
    
    private ContentHandler[] getContentHandlers()
    {
        ArrayList<ContentHandler> list = new ArrayList<ContentHandler>();
        ServiceLoader<ContentHandler> loaders = 
          ServiceLoader.load( ContentHandler.class );
        for( ContentHandler handler : loaders )
        {
            m_logger.trace( "loaded handler: " + handler );
            list.add( handler );
        }
        return (ContentHandler[]) list.toArray( new ContentHandler[0] );
    }
    
   /**
    * Return the cache layout.
    * @return the layout
    */
    public Layout getCacheLayout()
    {
        return getCacheHandler().getLayout();
    }
    
   /**
    * Return the cache handler.
    * @return the cache handler
    */
    public Cache getCacheHandler()
    {
        return m_cacheHandler;
    }

   /**
    * Return the link manager.
    * @return the cache handler
    */
    public LinkManager getLinkManager()
    {
        return m_linkManager;
    }

   /**
    * Return the Transit proxy host
    * @return the host name
    */
    public String getProxyHost()
    {
        return m_directive.getProxyDirective().getHost();
    }
    
   /**
    * Return the Transit cache directory path.
    * @return the cache path
    */
    public String getVersion()
    {
        return Transit.VERSION;
    }
    
   /**
    * Return the Transit home directory.
    * @return the home directory
    */
    public String getHome()
    {
        return Transit.HOME.toString();
    }
    
   /**
    * Return the Transit data directory.
    * @return the data directory
    */
    public String getData()
    {
        return Transit.DATA.toString();
    }
    
   /**
    * Return the Transit prefs directory.
    * @return the prefs directory
    */
    public String getPrefs() 
    {
        return Transit.PREFS.toString();
    }
    
   /**
    * Return the Transit share directory.
    * @return the share directory
    */
    public String getShare()
    {
        return Transit.SYSTEM.toString();
    }
    
    //------------------------------------------------------------------
    // internals 
    //------------------------------------------------------------------

    private synchronized void setupProxy( ProxyDirective directive ) throws Exception
    {
        if( null == directive )
        {
            return;
        }
        
        String host = directive.getHost();
        if( null != host )
        {
            URL proxy = new URL( host );
            PasswordAuthentication auth = getPasswordAuthentication( directive );
            if( null != auth )
            {
                TransitAuthenticator ta = new TransitAuthenticatorImpl( auth );
                RequestIdentifier id = getRequestIdentifier( proxy );
                DelegatingAuthenticator da = DelegatingAuthenticator.getInstance();
                da.addTransitAuthenticator( ta, id );
            }

            int port = proxy.getPort();
            Properties system = System.getProperties();
            system.put( "http.proxyHost", proxy );
            system.put( "http.proxyPort", "" + port );
            String[] excludes = directive.getExcludes();
            String path = toExcludesPath( excludes );
            if( null != path )
            {
                system.put( "http.nonProxyHosts", path );
            }
        }
    }
    
    private PasswordAuthentication getPasswordAuthentication( ProxyDirective directive )
    {
        String username = directive.getUsername();
        if( null != username )
        {
           return new PasswordAuthentication( username, directive.getPassword() );
        }
        else
        {
            return null;
        }
    }

    private RequestIdentifier getRequestIdentifier( URL url )
    {
        if( null == url )
        {
            return null;
        }
        else
        {
            String protocol = url.getProtocol();
            String scheme = "";
            String prompt = "";
            int port = url.getPort();
            String host = url.getHost();
            return new RequestIdentifier( host, port, protocol, scheme, prompt );
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

    //------------------------------------------------------------------
    // static (utils)
    //------------------------------------------------------------------

   /**
    * Resolve the list of host names to be assigned as non-proxied hosts. If proxy
    * excludes are defined the string returned contains the host name (wilcards allowed)
    * separated by the "|" character.  If no proxy excludes are defined the value returned
    * shall be null.  The implementation reads the set of attribute names associated 
    * with a preferences node named "excludes".  Each attribute name is appended to 
    * a single string where names are separated by the "|" character.
    *
    * @param names an array of named excludes
    * @return a string containing a sequence of excluded hosts (possibly null)
    */
    private static String toExcludesPath( String[] names )
    {
        String spec = null;
        for( int i=0; i < names.length; i++ )
        {
            String name = names[i];
            if( null == spec )
            {
                spec = name;
            }
            else
            {
                spec = spec + "|" + name;
            }
        }
        return spec;
    }

   /**
    * The namespace string for transit related properties.
    */
    public static final String DOMAIN = "dpml.transit";

   /**
    * The singleton transit context.
    */
    private static TransitContext m_CONTEXT;
}
