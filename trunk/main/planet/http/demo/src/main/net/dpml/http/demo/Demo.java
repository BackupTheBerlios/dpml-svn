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
import java.util.Date;
import java.util.Map;

import net.dpml.logging.Logger;

import net.dpml.http.spi.SocketListenerService;

/**
 * Working demo/test component.  This component is aimed primarily 
 * and testing and validating interfaces and implementations related
 * to remote management and control of interal parts.
 */
public class Demo
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
        m_parts.getSocketListener();
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
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

