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
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Properties;

import net.dpml.transit.link.ArtifactLinkManager;
import net.dpml.transit.link.LinkManager;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.ProxyListener;
import net.dpml.transit.model.ProxyEvent;
import net.dpml.transit.model.RequestIdentifier;
import net.dpml.transit.model.DisposalListener;
import net.dpml.transit.model.DisposalEvent;
import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.lang.UnknownKeyException;
import net.dpml.util.Logger;


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
            
            Logger logger = resolveLogger( model );
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
    
    private static Logger resolveLogger( TransitModel model )
    {
        if( model instanceof DefaultTransitModel )
        {
            DefaultTransitModel m = (DefaultTransitModel) model;
            return m.getLoggingChannel();
        }
        else
        {
            return new LoggingAdapter();
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
    * Logging channel.
    */
    private Logger m_logger;

    private ProxyController m_proxyController;
    
    private DisposalController m_disposalController;

    //------------------------------------------------------------------
    // constructors
    //------------------------------------------------------------------
   /**
    * Creation of a new secured transit context.
    * @param model the transit configuration model
    * @param logger the assigned logging channel
    * @exception IOException if an I/O error occurs
    */
    private SecuredTransitContext( TransitModel model, Logger logger ) throws IOException
    {
        m_model = model;
        m_logger = logger;

        CacheModel cacheModel = model.getCacheModel();
        Logger cacheLogger = logger.getChildLogger( "cache" );
        DefaultCacheHandler cache = new DefaultCacheHandler( cacheModel, cacheLogger );
        m_cacheHandler = cache;
        ProxyModel proxy = m_model.getProxyModel();
        if( null != proxy )
        {
            synchronized( proxy )
            {
                setupProxy();
                m_proxyController = new ProxyController();
                proxy.addProxyListener( m_proxyController );
            }
        }
        m_disposalController = new DisposalController();
        model.addDisposalListener( m_disposalController );
    }

    //------------------------------------------------------------------
    // SecuredTransitContext 
    //------------------------------------------------------------------

   /**
    * Return a layout object matching the supplied identifier.
    * @param id the layout identifier
    * @return the layout object
    * @exception UnknownKeyException if the supplied layout id is unknown
    * @exception IOException if an IO error occurs
    */
    public Layout getLayout( String id ) throws UnknownKeyException, IOException
    {
        LayoutRegistry registry = m_cacheHandler.getLayoutRegistry();
        Layout layout = registry.getLayout( id );
        if( null == layout )
        {
            throw new UnknownKeyException( id );
        }
        else
        {
            return layout;
        }
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
        initializeCache();
    }

   /**
    * Cache initialization.
    *
    * @exception IOException if an initialization error occurs
    */
    private void initializeCache() throws IOException
    {
        getCacheHandler().initialize();
    }

   /**
    * Internal listener to the proxy model.
    */
    private class ProxyController extends UnicastRemoteObject implements ProxyListener
    {
       /**
        * Listener creation.
        * @exception RemoteException if a remote error occurs
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
    * Internal listener to the proxy model.
    */
    private class DisposalController extends UnicastRemoteObject implements DisposalListener
    {
       /**
        * Listener creation.
        * @exception RemoteException if a remote error occurs
        */
        public DisposalController() throws RemoteException
        {
            super();
        }
        
       /**
        * Notify a listener of transit model disposal.
        * @param event the disposal event
        */
        public void notifyDisposal( DisposalEvent event )
        {
            Thread thread = new Terminator();
            thread.start();
        }
    }
    
   /**
    * Internal model terminator.
    */
    private class Terminator extends Thread
    {
        Terminator()
        {
        }
        
       /**
        * Initiate model retraction from the RMI.
        */
        public void run()
        {
            m_logger.debug( "initiating transit runtime disposal" );
            terminate( m_proxyController );
            terminate( m_cacheHandler );
            terminate( m_disposalController );
            m_logger.debug( "transit runtime disposal complete" );
        }
        
        private void terminate( Object object )
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
                catch( Throwable e )
                {
                    final String error = 
                      "Unexpected error encountered during transit runtime termination.";
                    m_logger.warn( error, e );
                }
            }
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
    private static SecuredTransitContext m_CONTEXT;
}
