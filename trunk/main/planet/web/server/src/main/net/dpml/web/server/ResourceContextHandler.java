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

import net.dpml.transit.util.PropertyResolver;

import net.dpml.logging.Logger;

import org.mortbay.jetty.Handler;

/**
 * Context handler with enhanced support for symbolic property dereferencing. 
 */
public class ResourceContextHandler extends ResolvingContextHandler
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
    
    public ResourceContextHandler( Context context ) throws Exception
    {
        String base = context.getResourceBase();
        super.setResourceBase( base );
        String path = context.getContextPath();
        super.setContextPath( path );
        ResourceHandler handler = new ResourceHandler( "static", "/" );
        super.setHandler( handler );
        Server server = context.getServer();
        server.addHandler( this );
    }
    
}
