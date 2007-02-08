/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dpml.transit;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import net.dpml.transit.Artifact;

/**
 * Internal cache for url connections.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class ConnectionCache implements Runnable
{
    private static final Object DUMMY = new Object();
    private static final long TIME_TO_LIVE = 30000;
    private static final int PAUSE_DELAY = 10000;
    private static ConnectionCache m_INSTANCE;

    private final HashMap<Artifact, Entry> m_hardStore;
    private final WeakHashMap<URLConnection, Object> m_weakStore;
    private Thread m_thread;

    static
    {
        m_INSTANCE = new ConnectionCache();
    }

   /**
    * Return the connection cacne instance.
    * @return the singleton instance
    */
    public static ConnectionCache getInstance()
    {
        return m_INSTANCE;
    }

   /**
    * Internal constructor of the connection cache.
    */
    private ConnectionCache()
    {
        m_hardStore = new HashMap<Artifact, Entry>();
        m_weakStore = new WeakHashMap<URLConnection, Object>();
    }

   /**
    * Return the cached url connnection for an artifact.
    * @param key the arfifact
    * @return the cached url connection or null if not cached
    */
    public URLConnection get( Artifact key )
    {
        synchronized( this ) // ensure no ConcurrentModificationException can occur.
        {
            Entry entry = m_hardStore.get( key );
            if( entry == null )
            {
                return null;
            }
            URLConnection conn = entry.m_connection;
            return conn;
        }
    }

   /**
    * Put a connection into the cache.
    * @param key the artifact to be used as the cache key
    * @param conn the url connection to cache
    */
    public void put( Artifact key, URLConnection conn )
    {
        synchronized( this ) // ensure no ConcurrentModificationException can occur.
        {
            Entry entry = new Entry( conn );
            m_hardStore.put( key, entry );
            if( m_thread == null )
            {
                m_thread = new Thread( this, "ConnectionCache-cleaner" );
                m_thread.setDaemon( true );
                m_thread.start();
            }
        }
    }

   /**
    * Start the cache.
    */
    public void run()
    {
        while( true )
        {
            try
            {
                synchronized( this )
                {
                    long now = System.currentTimeMillis();
                    Iterator list = m_hardStore.values().iterator();
                    while( list.hasNext() )
                    {
                        Entry entry = (Entry) list.next();
                        if( entry.m_collectTime < now )
                        {
                            m_weakStore.put( entry.m_connection, DUMMY );
                            list.remove();
                        }
                    }
                    if( m_hardStore.size() == 0 )
                    {
                        m_thread = null;    // mark to start a new thread next time.
                        break;              // Exit the thread
                    }
                    wait( PAUSE_DELAY );
                }
            }
            catch( Exception e )
            {
                // Can not happen?
                // Just ignore and it will be handled in the next round.
                e.printStackTrace();
            }
        }
    }

   /**
    * Internal class used for cache entries.
    */
    private class Entry
    {
        private final URLConnection m_connection;
        private final long m_collectTime;

       /**
        * Creation of a new cache entry instance.
        * @param conn the url connection
        */
        Entry( URLConnection conn )
        {
            m_connection = conn;
            m_collectTime = System.currentTimeMillis() + TIME_TO_LIVE;
        }

       /**
        * Test for equality.
        * @param obj the other object
        * @return the equality status
        */
        public boolean equals( Object obj )
        {
            if( obj == null )
            {
                return false;
            }
            if( !obj.getClass().equals( Entry.class ) )
            {
                return false;
            }
            Entry other = (Entry) obj;
            if( !m_connection.equals( other.m_connection ) )
            {
                return false;
            }
            return true;
        }

       /**
        * Return the hashcode for the instance.
        * @return the hashcode
        */
        public int hashCode()
        {
            return m_connection.hashCode();
        }

       /**
        * Return the string representation of the instance.
        * @return the string
        */
        public String toString()
        {
            return "Entry[" + m_connection + ", " + m_collectTime + "]";
        }
    }
}
