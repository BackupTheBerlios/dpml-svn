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

import net.dpml.activity.Startable;

import net.dpml.http.spi.SocketListenerService;

/**
 * Working demo/test component.
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
    
   /**
    * Return information about the running state of the application.
    */
    public Stats getStats()
    {
        //Runtime runtime = Runtime.getRuntime();
        //long free = runtime.freeMemory();
        //long total = runtime.totalMemory();
        //long max = runtime.maxMemory();
        //return new Stats( free, total, max );
        return new Stats();
    }
    
   /**
    * Utility class used to marshal memory usage information 
    * from the current runtime.
    */
    public static final class Stats implements Serializable
    {
        private final long m_free;
        private final long m_max;
        private final long m_total;
        private final Date m_timestamp;
        
        //private Stats( long free, long total, long max )
        private Stats()
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

