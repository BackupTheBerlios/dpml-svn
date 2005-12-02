/*
 * Copyright 2005 Stephen McConnell
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

import java.util.Map;

import net.dpml.logging.Logger;

import net.dpml.activity.Startable;

import net.dpml.http.spi.FileResourceHandler;
import net.dpml.http.spi.HttpContextService;
import net.dpml.http.spi.HttpService;
import net.dpml.http.spi.SocketListenerService;

import org.mortbay.http.HttpHandler;

public class Demo implements Startable
{
   /**
    * HTTP Demo component context.
    */
    public interface Context
    {
       /**
        * Get the port on which the demo listener will be assigned.
        * @param port the default port value 
        * @return the port to assign to the socket listener
        */
        int getPort( int port );
    }

   /**
    * Internal part managmeent interface.
    */
    public interface Parts
    {
       /**
        * Return the context map associated with the socket listener.
        * @return the context map
        */
        Map getSocketListenerMap();
        
       /**
        * Return the socket listener service.
        * @return the socket listener
        */
        SocketListenerService getSocketListener();
    }
    
    private final Logger m_logger;
    private final Parts m_parts;
    
   /**
    * Creation of the HTTP Demo Component.
    * @param logger the assigned logging channel
    * @param context the public component context
    * @param parts the internal parts manager
    */
    public Demo( Logger logger, Context context, Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
        int port = context.getPort( 8080 );
        parts.getSocketListenerMap().put( "port", new Integer( port ) );
    }

   /**
    * Start the demo.
    */
    public void start()
    {
        m_logger.info( "Starting" );
        m_parts.getSocketListener();
    }

   /**
    * Stop the demo.
    * @exception Exception if an error occurs
    */
    public void stop()
    {
        m_logger.info( "Stopping" );
    }
    
}

