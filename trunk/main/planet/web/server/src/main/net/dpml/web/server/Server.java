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

import net.dpml.logging.Logger;

import org.mortbay.thread.ThreadPool;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Handler;
import org.mortbay.xml.XmlConfiguration;

/**
 * HTTP server implementation.
 */
public class Server extends org.mortbay.jetty.Server
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Get the Jetty XML configuration uri.
        * @param uri the default uri
        * @return a uri referencing a Jetty configuration
        */
        URI getConfiguration( URI uri );
        
       /**
        * Get the thread pool.
        * @param pool the fallback thread pool (may be null)
        * @return the thread pool
        */
        //ThreadPool getThreadPool( ThreadPool fallback );
        
       /**
        * Get the request log.
        * @param log the fallback request log (may be null)
        * @return the resolved request log
        */
        //RequestLog getRequestLog( RequestLog log );
        
       /**
        * Get the request log.
        * @param handler the fallback not-found handler
        * @return the resolved handler
        */
        //Handler getNotFoundHandler( Handler handler );
        
       /**
        * Get the stop-at-shutdown policy.
        * @return true if the server should be stopped at shutdown
        */
        //boolean getShutdownPolicy( boolean flag );
    }
    
    private final Logger m_logger;
    private final Context m_context;

   /**
    * Creation of a new HTTP server implementation.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    */
    public Server( Logger logger, Context context ) throws Exception
    {
        super();
        
        m_logger = logger;
        m_context = context;
        
        
        URI standard = new URI( "local:xml:dpml/planet/web/jetty" );
        URI uri = context.getConfiguration( standard );
        if( null != uri )
        {
            getLogger().info( "configuration: " + uri );
            URL url = uri.toURL();
            XmlConfiguration config = new XmlConfiguration( url );
            config.configure( this );
        }
        
        getLogger().info( "ready: " + this );
        
        /*
        ThreadPool pool = context.getThreadPool( null );
        if( null != pool )
        {
            setThreadPool( pool );
        }
        
        RequestLog log = context.getRequestLog( null );
        if( null != log )
        {
            setRequestLog( log );
        }
        
        Handler handler = context.getNotFoundHandler( null );
        if( null != handler )
        {
            setNotFoundHandler( handler );
        }
        
        boolean policy = context.getShutdownPolicy( false );
        if( !policy )
        {
            setStopAtShutdown( policy );
        }
        */
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
