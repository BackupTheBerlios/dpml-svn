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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventObject;

import net.dpml.transit.Logger;
import net.dpml.transit.TransitError;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.info.HostDirective;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * The DefaultTransitModel class maintains an active configuration of the 
 * Transit system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultTransitModel extends DefaultModel implements TransitModel
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    static TransitModel getBootstrapModel() throws Exception
    {
        Logger logger = new LoggingAdapter( "transit" );
        return getSecureModel( logger );
    }
    
    public static TransitModel getSecureModel( Logger logger ) throws Exception
    {
        TransitDirective directive = new TransitDirective( null, new CacheDirective() );
        return new DefaultTransitModel( logger, directive );
    }
    
    public static TransitModel getDefaultModel( Logger logger ) throws Exception
    {
        // TODO: do nifty stuff with prefs, properties, whatever
        return getClassicModel( logger );
    }
    
    public static TransitModel getClassicModel( Logger logger ) throws Exception
    {
        HostDirective[] hosts = new HostDirective[3];
        hosts[0] = 
          new HostDirective( 
            "dpml", 40, "http://repository.dpml.net/classic", null, null, null, 
            true, false, "classic", null, null );
        hosts[1] = 
          new HostDirective( 
            "ibiblio", 70, "http://www.ibiblio.org/maven", null, null, null, 
            true, false, "classic", null, null );
        hosts[2] = 
          new HostDirective( 
            "apache", 100, "http://www.apache.org/dist/java-repository", null, null, null,
            true, false, "classic", null, null );
        
        CacheDirective cache = 
          new CacheDirective( 
            CacheDirective.CACHE_PATH,
            CacheDirective.LOCAL_PATH,
            CacheDirective.LAYOUT,
            CacheDirective.EMPTY_LAYOUTS,
            hosts,
            CacheDirective.EMPTY_CONTENT );
        TransitDirective directive = new TransitDirective( null, cache );
        return new DefaultTransitModel( logger, directive );
    }
    
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------
    
    private final DefaultProxyModel m_proxy;
    private final DefaultCacheModel m_cache;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new TransitModel using a supplied configuration
    * and logging channel.  The implementation will construct a proxy
    * model, layout registry model, cache modle, content registry model, 
    * and repository codebase model using the supplied configuration.
    *
    * @param logger the assigned loging channel
    * @param directive the transit configuration
    * @exception NullPointerException if the logger or directive arguments are null
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel( Logger logger, TransitDirective directive ) 
      throws RemoteException, NullPointerException
    {
        super( logger );

        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }

        m_proxy = createProxyModel( directive );
        m_cache = createCacheModel( directive );
    }

    // ------------------------------------------------------------------------
    // TransitModel
    // ------------------------------------------------------------------------

   /**
    * Return the proxy configuration model.
    * @return the proxy model (null if no proxy config defined).
    */
    public ProxyModel getProxyModel()
    {
        return m_proxy;
    }

   /**
    * Return the cache model.
    * @return the cache model 
    */
    public CacheModel getCacheModel()
    {
        return m_cache;
    }

   /**
    * Internal event handler.
    * @param eventObject the event to handle
    */
    protected void processEvent( EventObject eventObject )
    {
        // no event in this object
    }
    
    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

   /**
    * Trigger disposal of the transit model.
    */
    public synchronized void dispose()
    {
        m_cache.dispose();
        m_proxy.dispose();
        try
        {
            UnicastRemoteObject.unexportObject( m_cache, true );
        }
        catch( RemoteException e )
        {
            getLogger().warn( "Remote error during disposal.", e );
        }
        try
        {
            UnicastRemoteObject.unexportObject( m_proxy, true );
        }
        catch( RemoteException e )
        {
            getLogger().warn( "Remote error during disposal.", e );
        }
    }

    private DefaultProxyModel createProxyModel( final TransitDirective directive )
    {
        try
        {
            ProxyDirective config = directive.getProxyDirective();
            if( null == config )
            {
                return null;
            }
            else
            {
                Logger logger = getLogger().getChildLogger( "proxy" );
                return new DefaultProxyModel( logger, config );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the proxy model.";
            throw new TransitError( error, e );
        }
    }

    private DefaultCacheModel createCacheModel( final TransitDirective directive )
    {
        try
        {
            Logger logger = getLogger().getChildLogger( "cache" );
            CacheDirective config = directive.getCacheDirective();
            return new DefaultCacheModel( logger, config );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the cache model.";
            throw new TransitError( error, e );
        }
    }
}

