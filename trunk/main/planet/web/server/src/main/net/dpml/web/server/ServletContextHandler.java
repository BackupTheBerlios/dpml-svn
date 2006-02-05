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

import java.util.ArrayList;

import net.dpml.transit.util.PropertyResolver;

import net.dpml.logging.Logger;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

import org.mortbay.jetty.Handler;

/**
 * Servlet context handler. 
 */
public class ServletContextHandler extends ResolvingContextHandler
{
   /**
    * HTTP static resource vontext handler parameters.
    */
    public interface Context
    {
       /**
        * Get the required HTTP server.
        * @return the assigned http server
        */
        Server getServer();
        
       /**
        * Get the http context resource base.  The value may contain symbolic
        * property references and should resolve to a local directory.
        *
        * @return the resource base
        */
        String getResourceBase();
        
       /**
        * Get the context path under which the http context instance will 
        * be associated.
        *
        * @return the assigned context path
        */
        String getContextPath();
    }
    
    private int m_priority = 0;
    
    public ServletContextHandler( Context context, Configuration config ) throws Exception
    {
        String base = context.getResourceBase();
        super.setResourceBase( base );
        String path = context.getContextPath();
        super.setContextPath( path );
        Handler handler = buildHandler( config );
        super.setHandler( handler );
        Server server = context.getServer();
        server.addHandler( this );
    }
    
    private Handler buildHandler( Configuration config ) throws ConfigurationException
    {
        Configuration servlets = config.getChild( "servlets" );
        ArrayList servletList = new ArrayList();
        Configuration[] servletConfigs = servlets.getChildren( "servlet" );
        for( int i=0; i<servletConfigs.length; i++ )
        {
            Configuration servletConfig = servletConfigs[i];
            String name = servletConfig.getAttribute( "name" );
            String classname = servletConfig.getAttribute( "class" );
            ServletHolder holder = new ServletHolder( name, classname );
            servletList.add( holder );
        }
        ArrayList mappingList = new ArrayList();
        Configuration mappings = config.getChild( "mappings" );
        Configuration[] servletMappings = config.getChildren( "map" );
        for( int i=0; i<servletMappings.length; i++ )
        {
            Configuration servletMap = servletMappings[i];
            String name = servletMap.getAttribute( "servlet" );
            String path = servletMap.getAttribute( "path" );
            ServletMapping mapping = new ServletMapping( name, path );
            mappingList.add( mapping );
        }
        ServletHolder[] servletArray = (ServletHolder[]) servletList.toArray( new ServletHolder[0] );
        ServletMapping[] mappingArray = (ServletMapping[]) mappingList.toArray( new ServletMapping[0] );
        return new ServletHandler( servletArray, mappingArray );
    }
}
