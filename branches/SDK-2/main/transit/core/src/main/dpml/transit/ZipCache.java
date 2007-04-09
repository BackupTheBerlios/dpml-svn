/*
 * Copyright 2004-2007 Stephen J. McConnell.
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

import java.util.HashMap;
import java.util.Iterator;

import java.util.zip.ZipFile;

import net.dpml.transit.Artifact;

/**
 * Internal cache.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ZipCache implements Runnable
{
    private static final int TIME_TO_LIVE = 30000;
    private static final int DELAY = 10000;

    private HashMap<Artifact, Entry> m_store;
    private long m_timeToLive;
    private Thread m_thread;

   /**
    * Internal zip cache constructor.
    */
    ZipCache()
    {
        m_store = new HashMap<Artifact, Entry>();
        m_timeToLive = TIME_TO_LIVE;
    }

    ZipFile get( Artifact key )
    {
        synchronized( this ) // ensure no ConcurrentModificationException can occur.
        {
            Entry entry = (Entry) m_store.get( key );
            if( entry == null )
            {
                return null;
            }
            ZipFile zip = entry.m_file;
            return zip;
        }
    }

    void put( Artifact key, ZipFile file )
    {
        synchronized( this ) // ensure no ConcurrentModificationException can occur.
        {
            Entry entry = new Entry( file );
            m_store.put( key, entry );
            if( m_thread == null )
            {
                m_thread = new Thread( this, "DPML Transit Zip Cache Cleaner" );
                m_thread.setDaemon( true );
                m_thread.start();
            }
        }
    }

   /**
    * Start the cache handler.
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
                    Iterator list = m_store.values().iterator();
                    while( list.hasNext() )
                    {
                        Entry entry = (Entry) list.next();
                        if( entry.m_collectTime < now )
                        {
                            entry.m_file.close();
                            list.remove();
                        }
                    }
                    if( m_store.size() == 0 )
                    {
                        m_thread = null;
                        break;
                    }
                    wait( DELAY );
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
    * Internal class that maintains a cache entry.
    */
    class Entry
    {
        private ZipFile  m_file;
        private long     m_collectTime;

       /**
        * Creation of a new zip cache entry.
        * @param file the zip file to cache
        */
        Entry( ZipFile file )
        {
            m_file = file;
            m_collectTime = System.currentTimeMillis() + m_timeToLive;
        }

       /**
        * Test this objexct for equality with another.
        * @param obj the other object
        * @return the equaility status
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

            if( !m_file.equals( other.m_file ) )
            {
                return false;
            }

            return true;
        }

       /**
        * Retun the instance hashcode.
        * @return the hashcode value
        */
        public int hashCode()
        {
            return m_file.hashCode();
        }

       /**
        * Return a string represention of the cache entry.
        * @return the string value
        */
        public String toString()
        {
            return "Entry[" + m_file + ", " + m_collectTime + "]";
        }
    }
}
