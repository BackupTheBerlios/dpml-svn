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

import net.dpml.http.impl.*;
import net.dpml.http.HttpService;
import net.dpml.http.HttpContextService;

import net.dpml.part.manager.Component;

import org.mortbay.http.HttpHandler;

public class Demo implements Startable
{
    public interface Parts
    {
        Component getServerComponent();
        Component getSocketListenerComponent();
        Component getContextComponent();
        Component getNotFoundHandlerComponent();
        Component getResourceHandlerComponent();
    }

    private final Logger m_logger;
    private final Parts m_parts;
    
    private Object m_context;
    private Object m_server;
    private Object m_listener;
    private Object m_handler;
    private Object m_nf;

    public Demo( Logger logger, Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
    }

    public void start() throws Exception
    {
        m_logger.info( "Starting" );
        m_server = m_parts.getServerComponent().resolve( false );
        m_context = m_parts.getContextComponent().resolve( false );
        m_listener = m_parts.getSocketListenerComponent().resolve( false );
        m_handler = m_parts.getResourceHandlerComponent().resolve( false );
        m_nf = m_parts.getNotFoundHandlerComponent().resolve( false );
        m_logger.info( "Started" );
    }

    public void stop() throws Exception
    {
        m_logger.info( "Stopping" );
        m_parts.getServerComponent().terminate();
        m_logger.info( "Stopped" );
    }
}

