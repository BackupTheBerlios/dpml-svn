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
package net.dpml.http.demo;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.logging.Logger;

import net.dpml.metro.ComponentContext;
import net.dpml.metro.ComponentHandler;
import net.dpml.metro.PartsManager;

import net.dpml.part.Controller;
import net.dpml.part.Directive;
import net.dpml.part.Disposable;

import net.dpml.http.spi.SocketListenerService;

/**
 * Working demo/test component.  This component is aimed primarily 
 * and testing and validating interfaces and implementations related
 * to remote management and control of interal parts.
 */
public class Demo implements ManagementOperations, Disposable
{
    //---------------------------------------------------------
    // criteria
    //---------------------------------------------------------

   /**
    * HTTP Demo component context.
    */
    public interface Context extends ComponentContext
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
    public interface Parts extends PartsManager
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
    
    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    private final Logger m_logger;
    private final Context m_context;
    private final Parts m_parts;
    private final Map m_httpContextTable = new Hashtable(); // context path to context mapping
    
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------

   /**
    * Creation of the HTTP Demo Component.
    * @param logger the assigned logging channel
    * @param context the public component context
    * @param parts the internal parts manager
    */
    public Demo( Logger logger, Context context, Parts parts ) throws Exception
    {
        m_logger = logger;
        m_parts = parts;
        m_context = context;
        int port = context.getPort( 8080 );
        parts.getSocketListenerMap().put( "port", new Integer( port ) );
        m_parts.getSocketListener(); // trigger activation
        ComponentHandler handler = parts.getComponentHandler( "context" );
        m_httpContextTable.put( "/", handler );
    }
    
   /**
    * Dispose of the component.
    */
    public void dispose()
    {
        getLogger().info( "# DISPOSAL IN DEMO" );
    }
    
    //---------------------------------------------------------
    // ManagementOperations
    //---------------------------------------------------------
    
   /**
    * Add a new http context to the application. This implementation 
    * is experimental and can be very significantly optimized however
    * the primary object here is to test dynamic component loading.
    * @param base the http resource base
    * @param path the http context path
    */
    public void addContext( String base, String path ) throws Exception
    {
        if( m_httpContextTable.containsKey( path ) )
        {
            final String error = 
              "Component path ["
              + path 
              + "] is already assigned to a http context instance.";
            throw new IllegalArgumentException( error );
        }
        
        getLogger().info( "# BASE: " + base );
        getLogger().info( "# PATH: " + path );
        URI uri = new URI( "link:part:dpml/planet/http/dpml-http-context" );
        ClassLoader anchor = ManagementOperations.class.getClassLoader();
        ComponentHandler handler = m_context.createComponentHandler( anchor, uri );
        handler.getContextMap().put( "resourceBase", base );
        handler.getContextMap().put( "contextPath", path );
        m_httpContextTable.put( path, handler );
        Object value = handler.getProvider().getValue( false );
        getLogger().info( "# VALUE: " + value );
    }

   /**
    * Add a new http content handler to a http context.
    * @param path the http context path
    */
    public void addResourceHandler( String path ) throws Exception
    {
        getLogger().info( "# adding resource handler to path: " + path );
        ComponentHandler httpContextHandler = (ComponentHandler) m_httpContextTable.get( path );
        if( null == httpContextHandler )
        {
            final String error = 
              "Http context path [" + path + "] is undefined.";
            throw new IllegalArgumentException( error );
        }
        URI uri = new URI( "link:part:dpml/planet/http/dpml-http-resource" );
        ClassLoader anchor = ManagementOperations.class.getClassLoader();
        ComponentHandler handler = m_context.createComponentHandler( anchor, uri );
        Object httpContext = httpContextHandler.getProvider().getValue( false );
        handler.getContextMap().put( "httpContext", httpContext );
        Object value = handler.getProvider().getValue( false );
        getLogger().info( "# VALUE: " + value );
    }
    
   /**
    * Remove an http context from the application.
    * @param path the context path
    */
    public void removeContext( String path )
    {
        getLogger().info( "# REMOVE: " + path );
    }
    
   /**
    * Strurn the array of context paths.
    * @return the context path array
    */
    public String[] getContextPaths()
    {
        String[] keys = (String[]) m_httpContextTable.keySet().toArray( new String[0] );
        getLogger().info( "# LIST" );
        return keys;
    }

    //---------------------------------------------------------
    // declared operations
    //---------------------------------------------------------

   /**
    * Return some stats infomation using a supplied argument.
    * @param arg a value such as 'memory'
    * @return a stats instance
    * @exception IllegalArgumentException if the supplied argument is not recognized
    */
    public Stats getStats( String arg ) throws IllegalArgumentException
    {
        System.out.println( "processing stats command with option: [" + arg + "]" );
        if( "memory".equals( arg ) )
        {
            return getMemoryStats();
        }
        else
        {
            throw new IllegalArgumentException( arg );
        }
    }
    
   /**
    * Return information about memory usage.
    * @return a stats instance
    */
    public MemoryStats getMemoryStats()
    {
        return new MemoryStats();
    }
    
    //---------------------------------------------------------
    // implementation
    //---------------------------------------------------------
    
   /**
    * Return the logging channel.
    * @return the logger
    */
    private Logger getLogger()
    {
        return m_logger;
    }
    
    //---------------------------------------------------------
    // utilities
    //---------------------------------------------------------

   /**
    * Utility stats class.
    */
    public abstract static class Stats implements Serializable
    {
    }
    
   /**
    * Utility class used to marshal memory usage information 
    * from the current runtime.
    */
    public static final class MemoryStats extends Stats
    {
        private final long m_free;
        private final long m_max;
        private final long m_total;
        private final Date m_timestamp;
        
        private MemoryStats()
        {
            Runtime runtime = Runtime.getRuntime();
            long free = runtime.freeMemory();
            long total = runtime.totalMemory();
            long max = runtime.maxMemory();
            m_timestamp = new Date();
            m_free = free;
            m_total = total;
            m_max = max;
        }
        
       /**
        * Return the free memory value.
        * @return the free memory value
        */
        public long getFreeMemory()
        {
            return m_free;
        }
        
       /**
        * Return the total memory value.
        * @return the free memory value
        */
        public long getTotalMemory()
        {
            return m_total;
        }
        
       /**
        * Return the max memory value.
        * @return the max memory value
        */
        public long getMaxMemory()
        {
            return m_max;
        }
        
       /**
        * Return the timestamp of the momory usage info.
        * @return the timestamp value
        */
        public Date getTimestamp()
        {
            return m_timestamp;
        }
        
       /**
        * Return a string representation of the datastructure.
        * @return the string value
        */
        public String toString()
        {
            StringBuffer buffer = new StringBuffer( "Memory Usage" );
            buffer.append( "\n  Timestamp: " + m_timestamp );
            buffer.append( "\n       Free: " + m_free );
            buffer.append( "\n      Total: " + m_total );
            buffer.append( "\n        Max: " + m_max );
            return buffer.toString();
        }
    }
}

