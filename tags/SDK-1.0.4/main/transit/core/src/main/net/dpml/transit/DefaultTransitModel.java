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
import java.net.URL;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.ProxyDirective;
import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DisposalEvent;
import net.dpml.transit.model.DisposalListener;
import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.util.EventQueue;
import net.dpml.util.Logger;

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
    
   /**
    * Default configuration url path.
    */
    public static final String DEFAULT_PROFILE_PATH = "local:xml:dpml/transit/standard";
    
   /**
    * Default configuration url path.
    */
    public static final URI DEFAULT_PROFILE_URI = createStaticURI( DEFAULT_PROFILE_PATH );
    
   /**
    * System property key used to hold an overriding configuration url.
    */
    public static final String PROFILE_KEY = "dpml.transit.profile";

   /**
    * Return a model that is restricted to the secure local environment with 
    * no proxy setting or external hosts.
    * @param logger the logging channel to assign to the model
    * @return the transit model
    */
    public static DefaultTransitModel getSecureModel( Logger logger )
    {
        try
        {
            TransitDirective directive = new TransitDirective( null, new CacheDirective() );
            if( logger.isTraceEnabled() )
            {
                ClassLoader system = ClassLoader.getSystemClassLoader();
                int id = System.identityHashCode( system );
                logger.trace( "system classloader id: " + id );
            }
            EventQueue queue = new EventQueue( logger, "DPML Transit Event Queue" );
            return new DefaultTransitModel( queue, logger, directive );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while constructing static secure model.";
            throw new RuntimeException( error, e );
        }
    }
    
   /**
    * Resolve the transit configuration using the default resource path 
    * <tt>local:xml:dpml/transit/config</tt>. If the resource does not exist a classic 
    * default scenario will be returned.
    *
    * @return the transit model
    * @exception Exception if an error occurs during model construction
    */
    public static DefaultTransitModel getDefaultModel() throws Exception
    {
        return getDefaultModel( "transit" );
    }
    
   /**
    * Resolve the transit configuration using the default resource path 
    * <tt>local:xml:dpml/transit/config</tt>. If the resource does not exist a classic 
    * default scenario will be returned.
    *
    * @param category the logging channel category name
    * @return the transit model
    * @exception Exception if an error occurs during model construction
    */
    public static DefaultTransitModel getDefaultModel( String category ) throws Exception
    {
        LoggingAdapter adapter = new LoggingAdapter( category );
        return getDefaultModel( adapter );
    }
    
   /**
    * Resolve the transit configuration using the default resource path 
    * <tt>local:xml:dpml/transit/config</tt>. If the resource does not exist a classic 
    * default scenario will be returned.
    *
    * @param logger the logging channel
    * @return the transit model
    * @exception Exception if an error occurs during model construction
    */
    public static DefaultTransitModel getDefaultModel( Logger logger ) throws Exception
    {
        String path = System.getProperty( PROFILE_KEY );
        if( logger.isTraceEnabled() )
        {
            ClassLoader system = ClassLoader.getSystemClassLoader();
            int id = System.identityHashCode( system );
            logger.trace( "system classloader id: " + id );
        }
        if( null != path )
        {
            URI uri = Artifact.createArtifact( path ).toURI();
            URL url = uri.toURL();
            TransitBuilder builder = new TransitBuilder( logger );
            TransitDirective directive =  builder.load( url );
            return new DefaultTransitModel( logger, directive );
        }
        else
        {
            File prefs = Transit.DPML_PREFS;
            File config = new File( prefs, "dpml/transit/xmls/standard.xml" );
            if( config.exists() )
            {
                URI uri = config.toURI();
                URL url = uri.toURL();
                TransitBuilder builder = new TransitBuilder( logger );
                TransitDirective directive =  builder.load( url );
                return new DefaultTransitModel( logger, directive );
            }
            else
            {
                return getClassicModel( logger );
            }
        }
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
    * model, layout registry model, cache model, and repository codebase 
    * model using the supplied configuration.
    *
    * @param logger the assigned loging channel
    * @param directive the transit configuration
    * @exception NullPointerException if the logger or directive arguments are null
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel( Logger logger, TransitDirective directive ) 
      throws RemoteException, NullPointerException
    {
        this( 
          new EventQueue( logger, "DPML Transit Event Queue" ), 
          logger, directive );
    }
    
   /**
    * Creation of a new TransitModel using a supplied configuration
    * and logging channel.  The implementation will construct a proxy
    * model, layout registry model, cache model, and repository codebase 
    * model using the supplied configuration.
    *
    * @param queue the event queue 
    * @param logger the assigned logging channel
    * @param directive the transit configuration
    * @exception NullPointerException if the logger or directive arguments are null
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultTransitModel( EventQueue queue, Logger logger, TransitDirective directive ) 
      throws RemoteException, NullPointerException
    {
        super( queue, logger );

        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        
        m_proxy = createProxyModel( queue, directive );
        m_cache = createCacheModel( queue, directive );
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
    * Add a disposal listener to the model.
    * @param listener the listener to add
    */
    public void addDisposalListener( DisposalListener listener )
    {
        super.addListener( listener );
    }

   /**
    * Remove a disposal listener from the model.
    * @param listener the listener to remove
    */
    public void removeDisposalListener( DisposalListener listener )
    {
        super.removeListener( listener );
    }

   /**
    * Internal event handler.
    * @param eventObject the event to handle
    */
    public void processEvent( EventObject eventObject )
    {
        if( eventObject instanceof DisposalEvent )
        {
            DisposalEvent event = (DisposalEvent) eventObject;
            processDisposalEvent( event );
        }
    }
    
    private void processDisposalEvent( DisposalEvent event )
    {
        EventListener[] listeners = super.getEventListeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof DisposalListener )
            {
                DisposalListener pl = (DisposalListener) listener;
                try
                {
                    pl.notifyDisposal( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "Disposal notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // impl
    // ------------------------------------------------------------------------

    Logger getLoggingChannel()
    {
        return getLogger();
    }

   /**
    * Trigger disposal of the transit model.
    */
    public synchronized void dispose()
    {
        DisposalEvent event = new DisposalEvent( this );
        enqueueEvent( event, true );
        disposeCacheModel();
        disposeProxyModel();
        super.dispose();
        getEventQueue().terminateDispatchThread();
        Thread thread = new Terminator( this );
        thread.start();
    }
    
   /**
    * Internal model terminator.
    */
    private class Terminator extends Thread
    {
        private final DefaultTransitModel m_model;
        Terminator( DefaultTransitModel model )
        {
            m_model = model;
        }
        
       /**
        * Initiate model retraction from the RMI.
        */
        public void run()
        {
            try
            {
                UnicastRemoteObject.unexportObject( m_model, true );
            }
            catch( NoSuchObjectException e )
            {
                // ignore
            }
            catch( RemoteException e )
            {
                e.printStackTrace();
            }
        }
    }
    
    private synchronized void disposeProxyModel()
    {
        if( null == m_proxy )
        {
            return;
        }
        else
        {
            m_proxy.dispose();
            try
            {
                UnicastRemoteObject.unexportObject( m_proxy, true );
            }
            catch( NoSuchObjectException e )
            {
                // ignore
            }
            catch( RemoteException e )
            {
                getLogger().warn( "Remote error during proxy reference removal.", e );
            } 
        }
    }
    
    private synchronized void disposeCacheModel()
    {
        m_cache.dispose();
        try
        {
            UnicastRemoteObject.unexportObject( m_cache, true );
        }
        catch( NoSuchObjectException e )
        {
            // ignore
        }
        catch( RemoteException e )
        {
            getLogger().warn( "Remote error during cache reference removal.", e );
        }
    }
    
    private DefaultProxyModel createProxyModel( final EventQueue queue, final TransitDirective directive )
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
                return new DefaultProxyModel( queue, logger, config );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the proxy model.";
            throw new TransitError( error, e );
        }
    }

    private DefaultCacheModel createCacheModel( final EventQueue queue, final TransitDirective directive )
    {
        try
        {
            Logger logger = getLogger().getChildLogger( "cache" );
            CacheDirective config = directive.getCacheDirective();
            return new DefaultCacheModel( queue, logger, config );
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured during construction of the cache model.";
            throw new TransitError( error, e );
        }
    }

    static DefaultTransitModel getBootstrapModel() throws Exception
    {
        Logger logger = new LoggingAdapter( "transit" );
        return getSecureModel( logger );
    }
    
    static DefaultTransitModel getClassicModel( Logger logger ) throws Exception
    {
        TransitDirective directive = TransitDirective.CLASSIC_PROFILE;
        EventQueue queue = new EventQueue( logger, "DPML Transit Event Queue" );
        return new DefaultTransitModel( queue, logger, directive );
    }
    
    private static URI createStaticURI( String path )
    {
        try
        {
            return Artifact.createArtifact( path ).toURI();
        }
        catch( Exception e )
        {
            return null;
        }
    }
    
}

