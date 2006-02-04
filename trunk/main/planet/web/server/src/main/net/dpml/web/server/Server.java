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
package net.dpml.web.server;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import net.dpml.logging.Logger;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;
import net.dpml.part.Provider;

import org.mortbay.thread.ThreadPool;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.RequestLog;
import org.mortbay.xml.XmlConfiguration;
import org.mortbay.util.LazyList;

/**
 * HTTP server implementation.
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
        * used to establish the default server configuration.
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
    }
    
    public interface Parts extends PartsManager
    {
        RequestLog getRequestLog();
    }
    
    private final Logger m_logger;
    private final Context m_context;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    */
    public Server( Logger logger, Context context, Parts parts ) throws Exception
    {
        super();
        
        m_logger = logger;
        m_context = context;
        
        getLogger().debug( "commencing http server deployment" );
        URI standard = new URI( "local:xml:dpml/planet/web/default" );
        URI uri = context.getConfiguration( standard );
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        if( null != uri )
        {
            getLogger().info( "configuration: " + uri );
            URL url = uri.toURL();
            XmlConfiguration config = new XmlConfiguration( url );
            config.configure( this );
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
        // check for any internal parts that are context handlers
        //
        
        getLogger().info( "# START HANDLER ADDITION" );
        ComponentHandler[] handlers = parts.getComponentHandlers( org.mortbay.jetty.handler.ContextHandler.class );
        for( int i=0; i<handlers.length; i++ )
        {
            getLogger().info( "# HANDLER START " + (i+1) );
            ComponentHandler handler = handlers[i];
            try
            {
                getLogger().info( "# A" );
                handler.getContextMap().put( "server", this );
                getLogger().info( "# B" );
                Provider provider = handler.getProvider();
                getLogger().info( "# C" );
                ContextHandler ch = (ContextHandler) provider.getValue( false );
                getLogger().info( "# D" );
                super.addHandler( ch );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Failed to deploy content handler: " + handler;
                throw new Exception( error, e );
            }
            getLogger().info( "# HANDLER DONE " + (i+1) );
        }
        getLogger().info( "# STOP HANDLER ADDITION" );
        
        getLogger().debug( "http server is configured" );
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