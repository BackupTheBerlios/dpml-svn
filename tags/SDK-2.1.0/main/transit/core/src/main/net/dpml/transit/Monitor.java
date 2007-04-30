/*
 * Copyright 2006 Stephen McConnell.
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

package net.dpml.transit;

import java.io.File;

import java.net.URL;

/**
 * Transit event monitor.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Monitor
{
    // cache monitoring functions
    
   /**
    * Notify the monitor that an artifact has been requested.
    * @param artifact the requested artifact
    */
    void resourceRequested( Artifact artifact );

   /**
    * Notify the monitor that an artifact has been added to the local cache.
    * @param resource the url of the resource added to the local cache
    * @param localFile the local file resident in the cache
    */
    void addedToLocalCache( URL resource, File localFile );

   /**
    * Notify the monitor that an artifact in the local cache has been updated.
    * @param resource the url of the resource updating the local cache
    * @param localFile the local file that has been updated
    */
    void updatedLocalCache( URL resource, File localFile );

   /**
    * Notify the monitor that an artifact has been removed from the local cache.
    * @param resource the url of the resource removed from the local cache
    * @param localFile the local file removed from the cache
    */
    void removedFromLocalCache( URL resource, File localFile );

   /**
    * Notify the monitor of a failed download attempt relative to an identified host.
    * @param host the host raising the fail status
    * @param artifact the requested artifact
    * @param cause the exception causing the failure
    */
    void failedDownloadFromHost( String host, Artifact artifact, Throwable cause );

   /**
    * Notify the monitor of a failed download attempt.
    * @param artifact the requested artifact
    */
    void failedDownload( Artifact artifact );
    
    // network monitoring
    
    /**
     * Notify the monitor of the update in the download status.
     *
     * @param resource the name of the remote resource being downloaded.
     * @param expected the expected number of bytes to be downloaded.
     * @param count the number of bytes downloaded.
     */
    void notifyUpdate( URL resource, int expected, int count );

    /**
     * Notify the monitor of the successful completion of a download
     * process.
     * @param resource the name of the remote resource.
     */
    void notifyCompletion( URL resource );
    

}
