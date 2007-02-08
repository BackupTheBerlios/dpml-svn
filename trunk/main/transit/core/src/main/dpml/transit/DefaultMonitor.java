/* 
 * Copyright 2006-2007 Stephen McConnell
 *
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

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.dpml.transit.Artifact;
import net.dpml.transit.Monitor;

/**
 * The cache monitor router is responsible for the dispatiching of cache monitor 
 * events to registered monitors.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultMonitor implements Monitor, Iterable<Monitor>
{
    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------
    
   /**
    * List of attached monitors.
    */
    private final List<Monitor> m_monitors = new CopyOnWriteArrayList<Monitor>();

    //--------------------------------------------------------------------
    // cache monitoring
    //--------------------------------------------------------------------

   /**
    * Notify all monitors that a request for an artifact has been received.
    * @param artifact the requested artifact
    */ 
    public void resourceRequested( Artifact artifact )
    {
        for( Monitor monitor : this )
        {
            monitor.resourceRequested( artifact );
        }
    }
    
   /**
    * Notify all monitors that a request artifact has been added to the local cache.
    * @param resource the url of the remote resource
    * @param localFile the local file added to the cache
    */ 
    public void addedToLocalCache( URL resource, File localFile )
    {
        for( Monitor monitor : this )
        {
            monitor.addedToLocalCache( resource, localFile );
        }
    }
    
   /**
    * Notify all monitors that an artifact in the local cache has been updated.
    * @param resource the url of the remote resource 
    * @param localFile the local file that was modified
    */ 
    public void updatedLocalCache( URL resource, File localFile )
    {
        for( Monitor monitor : this )
        {
            monitor.updatedLocalCache( resource, localFile );
        }
    }
    
   /**
    * Notify all monitors that an artifact in the local cache was removed.
    * @param resource the url of the remote resource 
    * @param localFile the local file that was removed
    */ 
    public void removedFromLocalCache( URL resource, File localFile )
    {
        for( Monitor monitor : this )
        {
            monitor.removedFromLocalCache( resource, localFile );
        }
    }
    
   /**
    * Notify all monitors that an artifact request on a named host failed.
    * @param host the remote host
    * @param artifact the requested artifact
    * @param cause the cause of the failure
    */ 
    public void failedDownloadFromHost( String host, Artifact artifact, Throwable cause )
    {
        for( Monitor monitor : this )
        {
            monitor.failedDownloadFromHost( host, artifact, cause );
        }
    }
    
   /**
    * Notify all monitors that an artifact request failed.
    * @param artifact the requested artifact
    */ 
    public void failedDownload( Artifact artifact )
    {
        for( Monitor monitor : this )
        {
            monitor.failedDownload( artifact );
        }
    }
    
    //--------------------------------------------------------------------
    // network monitoring
    //--------------------------------------------------------------------
    
   /**
    * Notify all subscribing monitors of a updated event.
    * @param resource the url of the updated resource
    * @param expected the size in bytes of the download
    * @param count the progress in bytes
    */
    public void notifyUpdate( URL resource, int expected, int count )
    {
        for( Monitor monitor : this )
        {
            monitor.notifyUpdate( resource, expected, count );
        }
    }

   /**
    * Notify all subscribing monitors of a download completion event.
    * @param resource the url of the downloaded resource
    */
    public void notifyCompletion( URL resource )
    {
        for( Monitor monitor : this )
        {
            monitor.notifyCompletion( resource );
        }
    }
    
    //--------------------------------------------------------------------
    // Router
    //--------------------------------------------------------------------

   /**
    * Add a monitor to the set of monitors managed by this router.
    * @param monitor the monitor to add
    */
    public void addMonitor( Monitor monitor )
    {
        m_monitors.add( monitor );
    }
    
   /**
    * Remove a monitor from the set of monitors managed by this router.
    * @param monitor the monitor to remove
    */
    public void removeMonitor( Monitor monitor )
    {
        m_monitors.remove( monitor );
    }

    public Iterator<Monitor> iterator()
    {
        return m_monitors.iterator();
    }
}

