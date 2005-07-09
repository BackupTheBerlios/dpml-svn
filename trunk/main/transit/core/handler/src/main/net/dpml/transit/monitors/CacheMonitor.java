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

package net.dpml.transit.monitors;

import java.io.File;

import java.net.URL;

import net.dpml.transit.artifact.Artifact;

/**
 * A monitor of a Transit cache activity.
 *
 * <p>
 *   The CacheMonitor must be thread safe.
 * </p>
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: CacheMonitor.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 * @see Monitor
 */
public interface CacheMonitor extends Monitor
{
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
    void failedDownloadFromHost( String host, Artifact artifact, Exception cause );

   /**
    * Notify the monitor of a failed download attempt.
    * @param artifact the requested artifact
    */
    void failedDownload( Artifact artifact );

}
