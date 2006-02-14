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

import java.util.ArrayList;

import net.dpml.transit.util.PropertyResolver;

import net.dpml.logging.Logger;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

import org.mortbay.jetty.Handler;

/**
 * Servlet context handler. 
 */
public class ServletContextHandler extends org.mortbay.jetty.handler.ContextHandler
{
   /**
    * HTTP static resource vontext handler parameters.
    */
    public interface Context extends ContextHandlerContext
    {
       /**
        * Get the http context resource base.  The value may contain symbolic
        * property references and should resolve to a local directory.
        *
        * @return the resource base
        */
        String getResourceBase();
    }
    
    private int m_priority = 0;
    
    public ServletContextHandler( Logger logger, Context context, Configuration config ) throws Exception
    {
        super();
        
        ContextHelper helper = new ContextHelper( logger );
        helper.contextualize( this, context );
        
        String base = context.getResourceBase();
        super.setResourceBase( base );
        
        Handler handler = buildHandler( logger, config );
        super.setHandler( handler );
    }
    
    private Handler buildHandler( Logger logger, Configuration config ) throws ConfigurationException
    {
        logger.debug( "configuration " + config );
        Configuration servlets = config.getChild( "servlets" );
        ArrayList servletList = new ArrayList();
        Configuration[] servletConfigs = servlets.getChildren( "servlet" );
       logger.debug( "servlet count: " + servletConfigs.length );
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
        Configuration[] servletMappings = mappings.getChildren( "map" );
        logger.debug( "mapping count: " + servletMappings.length );
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
