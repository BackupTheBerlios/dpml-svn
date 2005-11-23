/*
 * Copyright 2004-2005 Niclas Hedhman
 * Copyright 2005 Stephen McConnell
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

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import net.dpml.transit.link.ArtifactLinkManager;
import net.dpml.transit.link.LinkManager;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.CodeBaseListener;
import net.dpml.transit.model.CodeBaseModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.ProxyListener;
import net.dpml.transit.model.ProxyEvent;
import net.dpml.transit.model.RequestIdentifier;
import net.dpml.transit.model.ParametersEvent;
import net.dpml.transit.model.LocationEvent;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * The initial context of the transit system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class SecuredTransitContext
{
    //------------------------------------------------------------------
    // static
    //------------------------------------------------------------------

   /**
    * Creation of the transit context.  If the transit context has already
    * been established the method returns the singeton context otherwise a new 
    * context is created relative to the authoritve url and returned.
    * @param model the active transit model
    * @return the secured transit context
    * @exception TransitException if an error occurs during context creation
    * @exception NullArgumentException if the supplied configration model is null 
    *    and an instance of this class has not been created already.
    */
    public static SecuredTransitContext create( TransitModel model )
        throws TransitException, NullArgumentException
    {
        synchronized( SecuredTransitContext.class )
        {
            if( m_CONTEXT != null )
            {
                return m_CONTEXT;
            }

            if( null == model )
            {
                throw new NullArgumentException( "model" );
            }

            Logger logger = new LoggingAdapter();
            if( logger.isDebugEnabled() )
            {
                logger.debug( "creating transit context" );
            }

            try
            {
                m_CONTEXT = new SecuredTransitContext( model, logger );
            }
            catch( TransitException e )
            {
                throw e;
            }
            catch( Exception e )
            {
                String error = "Unable to establish the transit context.";
                throw new TransitException( error, e );
            }

            return m_CONTEXT;
        }
    }

   /**
    * Return the singleton context.
    * @return the secure context
    */
    public static SecuredTransitContext getInstance()
    {
        synchronized( SecuredTransitContext.class )
        {
            if( null == m_CONTEXT )
            {
                throw new IllegalStateException( "context" );
            }
            else
            {
                return m_CONTEXT;
            }
        }
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The configuration model.
    */
    private TransitModel m_model;

   /**
    * The cache handler.
    */
    private CacheHandler m_cacheHandler;

   /**
    * The LinkManager instance.
    */
    private LinkManager m_linkManager;

   /**
    * The registry.
    */
    //private ContentRegistry m_registry;

   /**
    * Logging channel.
    */
    private Logger m_logger;

   /**
    * The repository service provider.
    */
    private Repository m_repository;

   /**
    * The transit plugin listener.
    */
    //private CodeBaseListener m_listener;

    //------------------------------------------------------------------
    // constructors
    //------------------------------------------------------------------
    /**
     * Creation of a new secured transit context.
     * @param model the transit configuration model
     * @param handler the cache handler
     * @param registry the content handler registry
     */
    private SecuredTransitContext( TransitModel model, Logger logger ) throws IOException
    {
        m_model = model;
        m_logger = logger;

        //
        // During boostrap there is only the DefaultCacheHandler, ClassicLayout
        // and DefaultContentRegistry. After bootstrap is complete we can evaluate  
        // alternative cache handlers, resolvers and registries via plugins
        // (see SecuredTransitContext.initialize() )
        //

        CacheModel cacheModel = model.getCacheModel();
        Logger cacheLogger = logger.getChildLogger( "cache" );
        DefaultCacheHandler cache = new DefaultCacheHandler( cacheModel, cacheLogger );
        //ContentRegistryModel registryModel = cacheModel.getContentRegistryModel();
        //Logger contentLogger = logger.getChildLogger( "content" );
        //ContentRegistry registry = new DefaultContentRegistry( registryModel, contentLogger );

        m_cacheHandler = cache;
        //m_registry = registry;
        //m_listener = new TransitListener();

        ProxyModel proxy = m_model.getProxyModel();
        if( null != proxy )
        {
            synchronized( proxy )
            {
                setupProxy();
                ProxyController controller = new ProxyController();
                proxy.addProxyListener( controller );
            }
        }
    }

    //------------------------------------------------------------------
    // SecuredTransitContext 
    //------------------------------------------------------------------

   /**
    * Return the cache layout.
    * @return the layout
    */
    public Layout getCacheLayout()
    {
        return getCacheHandler().getLayout();
    }
    
   /**
    * Return the Transit repository service.
    * @return the repository service
    */
    public Repository getRepository()
    {
        return m_repository;
    }

   /**
    * Return the cache handler.
    * @return the cache handler
    */
    public CacheHandler getCacheHandler()
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
    * Return the registry of pluggable content handlers.
    * @return the content handler registry
    */
    //public ContentRegistry getContentHandlerRegistry()
    //{
    //    return m_registry;
    //}

    //------------------------------------------------------------------
    // internals 
    //------------------------------------------------------------------

   /**
    * General setup.
    * @exception RemoteException if a remote error occurs
    */
    protected synchronized void setupProxy() throws RemoteException
    {
        ProxyModel model = m_model.getProxyModel();
        URL proxy = model.getHost();
        if( null != proxy )
        {
            PasswordAuthentication auth = model.getAuthentication();
            if( null != auth )
            {
                TransitAuthenticator ta = new TransitAuthenticatorImpl( auth );
                RequestIdentifier id = model.getRequestIdentifier();
                DelegatingAuthenticator da = DelegatingAuthenticator.getInstance();
                da.addTransitAuthenticator( ta, id );
            }

            int port = proxy.getPort();
            Properties system = System.getProperties();
            system.put( "http.proxyHost", proxy );
            system.put( "http.proxyPort", "" + port );
            String[] excludes = model.getExcludes();
            String path = toExcludesPath( excludes );
            if( null != path )
            {
                system.put( "http.nonProxyHosts", path );
            }
        }
    }

   /**
    * Initialization of any sub-systems following the establishment of the initial
    * transit system. As a general principal any subsystems that cannot be established
    * for technical reasons (security or permission restrictions, etc.) should log 
    * an appropriate message and fallback to the initial setup thereby ensuring that
    * an operable transit system is available.
    *
    * @exception IOException if an io error occurs
    */
    protected void initialize() throws IOException
    {
        m_linkManager = new ArtifactLinkManager();
        m_repository = new StandardLoader();
        initializeCache();
        //initializeRegistry();
    }

   /**
    * ContentRegistry initialization.
    *
    * @exception IOException if an initialization error occurs
    */
    /*
    private void initializeRegistry() throws IOException
    {
        synchronized( m_listener )
        {
            ContentRegistryModel model = m_model.getContentRegistryModel();
            model.addCodeBaseListener( m_listener );
            URI uri = model.getCodeBaseURI();
            if( null != uri )
            {
                try
                {
                    ContentRegistry registry = loadContentRegistry( model );
                    handleDisposal( m_registry );
                    m_registry = registry;
                }
                catch( TransitException e )
                {
                    final String error = 
                      "Content handler initialization error (continuing with defalt registry)";
                    m_logger.warn( error, e );
                }
            }
        }
    }
    */

   /**
    * ContentRegistry initialization.
    * @param model the content registry model
    * @exception TransitException if an initialization error occurs
    */
    /*
    private ContentRegistry loadContentRegistry( ContentRegistryModel model ) throws IOException
    {
        synchronized( m_registry )
        {
            URI uri = model.getCodeBaseURI();
            if( null != uri )
            {
                m_logger.info( "loading custom content handler registry" );
                ClassLoader classloader = Transit.class.getClassLoader();
                try
                {
                    Logger log = m_logger.getChildLogger( "content" );
                    Object[] args = new Object[]{model, log};
                    Repository repository = getRepository();
                    return (ContentRegistry) repository.getPlugin( classloader, uri, args );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Critical error while attempting to establish content management subsystem."
                      + "\nURI: " + uri
                      + "\nClassLoader: " + classloader; 
                    throw new TransitException( error, e );
                }
            }
            else
            {
                try
                {
                    m_logger.info( "loading standard content handler registry" );
                    handleDisposal( m_registry );
                    Logger log = m_logger.getChildLogger( "content" );
                    return new DefaultContentRegistry( model, log );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Critical error while attempting to establish default content management subsystem.";
                    throw new TransitException( error, e );
                }
            }
        }
    }
    */

   /**
    * Cache initialization.
    *
    * @exception IOException if an initialization error occurs
    */
    private void initializeCache() throws IOException
    {
        getCacheHandler().initialize();
    }

    private void handleDisposal( Object object ) 
    {
        if( object instanceof Service )
        {
            Service handler = (Service) object;
            try
            {
                handler.dispose();
            }
            catch( Throwable e )
            {
                // interesting but ignorable
                e.printStackTrace();
            }
        }
    }

   /**
    * Internal listener to the proxy model.
    */
    private class ProxyController extends UnicastRemoteObject implements ProxyListener
    {
       /**
        * Listener creation.
        * @exeption RemoteException if a remote error occurs
        */
        public ProxyController() throws RemoteException
        {
            super();
        }

       /**
        * Notify a listener of the change to Transit proxy settings.
        * @param event the proxy change event
        */
        public void proxyChanged( ProxyEvent event )
        {
            try
            {
                setupProxy();
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unexpected error while attrempting to set proxy settings.";
                getLogger().error( error, e );
            }
        }
    }

   /**
    * Internal listener that listens to changes to plugable sub-systems 
    * and is responsible for swapping facilities on the fly.
    */
    //private class TransitListener extends UnicastRemoteObject implements CodeBaseListener
    //{
    //   /**
    //    * Creation of a new transit listener.
    //    * @exception RemoteException if an remote error occurs
    //    */
    //    public TransitListener() throws RemoteException
    //    {
    //        super();
    //    }
    //
    //   /**
    //    * Notification of the change to a plugin uri assigned to a sub-system.
    //    * @param event a plugin change event
    //    * @exception RemoteException if an remote error occurs
    //    */
    //    public void codeBaseChanged( LocationEvent event ) throws RemoteException
     //   {
    //        CodeBaseModel model = event.getCodeBaseModel();
    //        URI uri = event.getCodeBaseURI();
    //        if( getLogger().isDebugEnabled() )
    //        {
    //            final String message = 
    //              "Initiating sub-system change."
    //              + "\nSub-System: " + model
    //              + "\nURI: " + uri;
    //            getLogger().debug( message );
    //        }
    //        reload( model );
    //    }
    //
    //    public void parametersChanged( ParametersEvent event ) throws RemoteException
    //    {
    //        CodeBaseModel model = event.getCodeBaseModel();
    //        if( getLogger().isDebugEnabled() )
    //        {
    //            final String message = 
    //              "Initiating sub-system parameter change."
    //              + "\nSub-System: " + model;
    //            getLogger().debug( message );
    //        }
    //        reload( model );
    //    }
    //
    //    
    //    void reload( CodeBaseModel model )
    //    {
    //        //
    //        // Currently the only switchable sub-system is the content management
    //        // system (which is where Metro gets connected).  This needs to be extended to 
    //        // include the cache system, the layout registry and the indexing system.
    //        //
    //
    //        if( model instanceof ContentRegistryModel )
    //        {
    //            ContentRegistryModel crm = (ContentRegistryModel) model;
    //            try
    //            {
    //                ContentRegistry registry = loadContentRegistry( crm );
    //                handleDisposal( m_registry );
    //                m_registry = registry;
    //            }
    //            catch( IOException e )
    //            {
    //                final String error = 
    //                  "Content handler change error (registry remains unchanged)";
    //                m_logger.warn( error, e );
    //            }
    //        }
    //    }
    //}

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
    * @param prefs the 'proxy' prefs node containing a child node named 'excludes'
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
    private static SecuredTransitContext m_CONTEXT;
}
