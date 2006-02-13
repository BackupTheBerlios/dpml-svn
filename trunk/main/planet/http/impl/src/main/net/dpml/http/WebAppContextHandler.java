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
import java.util.ArrayList;

import net.dpml.transit.util.PropertyResolver;

import net.dpml.logging.Logger;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

import org.mortbay.jetty.Handler;

/**
 * Servlet context handler. 
 */
public class WebAppContextHandler extends org.mortbay.jetty.webapp.WebAppContext
{
   /**
    * HTTP static resource vontext handler parameters.
    */
    public interface Context
    {
       /**
        * Get the context path under which the http context instance will 
        * be associated.
        *
        * @return the assigned context path
        */
        String getContextPath();
        
       /**
        * Get the war artifact uri.
        * @return the uri identifying the war artifact
        */
        URI getWar();
    }
    
    private final Logger m_logger;
    
    private int m_priority = 0;
    
    public WebAppContextHandler( Logger logger, Context context ) throws Exception
    {
        m_logger = logger;
        
        String path = context.getContextPath();
        setContextPath( path );
        getSessionHandler().setSessionManager( new org.mortbay.jetty.servlet.HashSessionManager() );
        setWar( context.getWar().toString() );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
}
