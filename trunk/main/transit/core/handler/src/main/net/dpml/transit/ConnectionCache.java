/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.transit;

import net.dpml.transit.Artifact;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

class ConnectionCache
    implements Runnable
{
    static private Object DUMMY = new Object();
    static private ConnectionCache m_instance;

    private HashMap m_hardStore;
    private WeakHashMap m_weakStore;

    private long    m_timeToLive;
    private Thread  m_thread;

    static
    {
        m_instance = new ConnectionCache();
    }

    static public ConnectionCache getInstance()
    {
        return m_instance;
    }

    private ConnectionCache()
    {
        m_hardStore = new HashMap();
        m_weakStore = new WeakHashMap();
        m_timeToLive = 30000;
    }

    public URLConnection get( Artifact key )
    {
        synchronized( this ) // ensure no ConcurrentModificationException can occur.
        {
            Entry entry = (Entry) m_hardStore.get( key );
            if( entry == null )
                return null;
            URLConnection conn = entry.m_connection;
            return conn;
        }
    }

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
                    wait( 10000 );
                }
            } catch( Exception e )
            {
                // Can not happen?
                // Just ignore and it will be handled in the next round.
                e.printStackTrace();
            }
        }
    }

    private class Entry
    {
        private URLConnection   m_connection;
        private long            m_collectTime;

        Entry( URLConnection conn )
        {
            m_connection = conn;
            m_collectTime = System.currentTimeMillis() + m_timeToLive;
        }

        public boolean equals( Object obj )
        {
            if( obj == null )
                return false;
            if( obj.getClass().equals( Entry.class ) == false )
                return false;
            Entry other = (Entry) obj;

            if( m_connection.equals( other.m_connection ) == false )
                return false;

            return true;
        }

        public int hashCode()
        {
            return m_connection.hashCode();
        }

        public String toString()
        {
            return "Entry[" + m_connection + ", " + m_collectTime + "]";
        }
    }
}