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

import net.dpml.part.control.Component;

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
    
    public Demo( Logger logger, Parts parts )
    {
        m_logger = logger;
        m_parts = parts;
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

