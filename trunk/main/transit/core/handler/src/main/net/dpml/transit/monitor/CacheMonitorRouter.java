/* 
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.monitor;

import java.io.File;

import java.net.URL;

import net.dpml.transit.Artifact;

/**
 * The cache monitor router is responsible for the dispatiching of cache monitor 
 * events to registered monitors.
 */
public class CacheMonitorRouter extends AbstractMonitorRouter
    implements CacheMonitor, Router
{
    //--------------------------------------------------------------------
    // CacheMonitor
    //--------------------------------------------------------------------

   /**
    * Notify all monitors that a request for an artifact has been received.
    * @param artifact the requested artifact
    */ 
    public void resourceRequested( Artifact artifact )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            CacheMonitor monitor = (CacheMonitor) monitors[i];
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
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            CacheMonitor monitor = (CacheMonitor) monitors[i];
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
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            CacheMonitor monitor = (CacheMonitor) monitors[i];
            monitor.addedToLocalCache( resource, localFile );
        }
    }
    
   /**
    * Notify all monitors that an artifact in the local cache was removed.
    * @param resource the url of the remote resource 
    * @param localFile the local file that was removed
    */ 
    public void removedFromLocalCache( URL resource, File localFile )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            CacheMonitor monitor = (CacheMonitor) monitors[i];
            monitor.removedFromLocalCache( resource, localFile );
        }
    }
    
   /**
    * Notify all monitors that an artifact request on a named host failed.
    * @param host the remote host
    * @param artifact the requested artifact
    * @param cause the cause of the failure
    */ 
    public void failedDownloadFromHost( String host, Artifact artifact, Exception cause )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            CacheMonitor monitor = (CacheMonitor) monitors[i];
            monitor.failedDownloadFromHost( host, artifact, cause );
        }
    }
    
   /**
    * Notify all monitors that an artifact request failed.
    * @param artifact the requested artifact
    */ 
    public void failedDownload( Artifact artifact )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            CacheMonitor monitor = (CacheMonitor) monitors[i];
            monitor.failedDownload( artifact );
        }
    }

   /**
    * Addition of a monitor to the list of monitors maintained by this router.
    * @param monitor the monitor to add
    * @exception IllegalArgumentException if the monitor does not implement CacheMonitor
    */ 
    public void addMonitor( Monitor monitor ) throws IllegalArgumentException
    {
        if( !( monitor instanceof CacheMonitor ) )
        {
            throw new IllegalArgumentException( "monitor must be CacheMonitor type." );
        }
        else
        {
            super.addMonitor( monitor );
        }
    }
}
 
