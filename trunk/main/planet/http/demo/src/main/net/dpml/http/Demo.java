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

import net.dpml.logging.Logger;

import net.dpml.activity.Startable;

import

import net.dpml.http.spi.FileResourceHandler;
import net.dpml.http.spi.HttpContextService;
import net.dpml.http.spi.HttpService;
import net.dpml.http.spi.SocketListenerService;

import org.mortbay.http.HttpHandler;

public class Demo implements Startable
{
    public interface Context
    {
        int getPort( int port );
    }

    public interface Parts
    {
        //FileResourceHandler getHttpHandler();
        //SocketListenerService getSocketListener();
        //Model getSocketListenerModel();
    }
    
    private final Logger m_logger;

    private final Parts m_parts;
    
    public Demo( Logger logger, Context context, Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
        System.out.println( "### PORT: " + context.getPort( 8080 ) );
    }

    public void start() throws Exception
    {
        m_logger.info( "Starting" );
    }

    public void stop() throws Exception
    {
        m_logger.info( "Stopping" );
    }
    
}

