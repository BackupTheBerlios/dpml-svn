/*
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import net.dpml.logging.Logger;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;
import net.dpml.component.Provider;

import org.mortbay.thread.ThreadPool;
import org.mortbay.jetty.RequestLog;
import org.mortbay.xml.XmlConfiguration;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.Connector;

/**
 * HTTP server implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Server extends org.mortbay.jetty.Server
{
   /**
    * Component context through which the server configuration uri may be declared.
    */
    public interface Context
    {
       /**
        * Get the Jetty XML configuration uri.  The configuration uri is 
        * used to establish the default server configuration prior to 
        * customization via the server context. If not supplied the server
        * will be deployed relative to the supplied context.
        *
        * @param uri the default uri
        * @return a uri referencing a Jetty configuration profile
        */
        URI getConfiguration( URI uri );
        
       /**
        * Get the assigned request logger. If no request logger is 
        * assigned by the deployment scenario a default request logger
        * will be established.
        *
        * @param logger the default value
        * @return the resolved request logger
        */
        RequestLog getRequestLog( RequestLog logger );
        
       /**
        * Get the assigned thread pool. If no thread pool is 
        * assigned by the deployment scenario a default pool
        * will be established.
        *
        * @param pool the default value
        * @return the resolved thread pool
        */
        ThreadPool getThreadPool( ThreadPool pool );
    }
    
   /**
    * Internal parts managemwent interface.
    */
    public interface Parts extends PartsManager
    {
       /**
        * Return the default request log.
        * @return the default request log.
        */
        RequestLog getRequestLog();
        
       /**
        * Return the default thread pool.
        * @return the default thread pool.
        */
        ThreadPool getThreadPool();
    }
    
    private final Logger m_logger;
    private final Context m_context;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    * @param parts the parts manager
    * @exception Exception if an instantiation error occurs
    */
    public Server( Logger logger, Context context, Parts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        m_context = context;
        
        getLogger().debug( "commencing http server deployment" );
        Logger internal = logger.getChildLogger( "jetty" );
        internal.debug( "assigning internal jetty logger" );
        LoggerAdapter.setRootLogger( internal );
        
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        URI uri = context.getConfiguration( null );
        if( null != uri )
        {
            getLogger().debug( "applying server configuration: " + uri );
            URL url = uri.toURL();
            XmlConfiguration config = new XmlConfiguration( url );
            config.configure( this );
        }
        
        //
        // setup the thread pool
        //
        
        ThreadPool pool = context.getThreadPool( null );
        if( null != pool )
        {
            super.setThreadPool( pool );
        }
        else
        {
            super.setThreadPool( parts.getThreadPool() );
        }
        
        //
        // setup the request log
        //
        
        RequestLog log = context.getRequestLog( null );
        if( null != log )
        {
            super.setRequestLog( log );
        }
        else
        {
            super.setRequestLog( parts.getRequestLog() );
        }
        
        //
        // add connectors, realms and context handlers
        //
        
        addConnectors( parts );
        addUserRealms( parts );
        addContextHandlers( parts );
        getLogger().debug( "server established" );
    }
    
    private void addConnectors( PartsManager parts ) throws Exception
    {
        getLogger().debug( "commencing connector addition" );
        ComponentHandler[] handlers = parts.getComponentHandlers( Connector.class );
        getLogger().debug( "connector count: " + handlers.length );
        ArrayList list = new ArrayList();
        for( int i=0; i<handlers.length; i++ )
        {
            ComponentHandler handler = handlers[i];
            getLogger().debug( "adding connector: " + handler );
            try
            {
                Provider provider = handler.getProvider();
                Connector ch = (Connector) provider.getValue( false );
                list.add( ch );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Failed to deploy content handler: " + handler;
                throw new Exception( error, e );
            }
        }
        Connector[] connectors = (Connector[]) list.toArray( new Connector[0] );
        setConnectors( connectors );
    }
    
    private void addContextHandlers( PartsManager parts ) throws Exception
    {
        getLogger().debug( "commencing context handler addition" );
        ComponentHandler[] handlers = parts.getComponentHandlers( org.mortbay.jetty.handler.ContextHandler.class );
        getLogger().debug( "handler count: " + handlers.length );
        for( int i=0; i<handlers.length; i++ )
        {
            ComponentHandler handler = handlers[i];
            getLogger().debug( "adding handler: " + handler );
            try
            {
                Provider provider = handler.getProvider();
                org.mortbay.jetty.handler.ContextHandler ch = 
                  (org.mortbay.jetty.handler.ContextHandler) provider.getValue( false );
                super.addHandler( ch );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Failed to deploy content handler: " + handler;
                throw new Exception( error, e );
            }
        }
    }
    
    private void addUserRealms( PartsManager parts )  throws Exception
    {
        getLogger().debug( "commencing realm addition" );
        ArrayList list = new ArrayList();
        ComponentHandler[] handlers = parts.getComponentHandlers( org.mortbay.jetty.security.UserRealm.class );
        for( int i=0; i<handlers.length; i++ )
        {
            ComponentHandler handler = handlers[i];
            getLogger().debug( "adding realm: " + handler );
            try
            {
                Provider provider = handler.getProvider();
                org.mortbay.jetty.security.UserRealm ch = 
                  (org.mortbay.jetty.security.UserRealm) provider.getValue( false );
                  list.add( ch );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Failed to deploy user realm: " + handler;
                throw new Exception( error, e );
            }
        }
        UserRealm[] realms = (UserRealm[]) list.toArray( new UserRealm[0] );
        setUserRealms( realms );
    }
    
    /*
    public void addHandler( Handler handler )
        throws Exception
    {
        Handler[] handlers = getHandlers();
        Handler[] newHandlers = (Handler[]) LazyList.addToArray( handlers, handler );
        Arrays.sort( newHandlers );
        super.setHandlers( newHandlers );
    }
    
    public void setHandlers( Handler[] handlers )
    {
        Arrays.sort( handlers );
        super.setHandlers( handlers );
    }
    */
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
