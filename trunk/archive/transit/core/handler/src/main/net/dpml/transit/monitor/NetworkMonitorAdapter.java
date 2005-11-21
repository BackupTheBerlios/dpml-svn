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

import java.net.URL;

/**
 * Adapts network events to logging messages.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class NetworkMonitorAdapter extends AbstractAdapter
    implements NetworkMonitor
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new network monitor adapter.
    * @param adapter the logging adapter
    */
    public NetworkMonitorAdapter( Adapter adapter )
    {
        super( adapter );
    }

    // ------------------------------------------------------------------------
    // NetworkMonitor
    // ------------------------------------------------------------------------

    /**
     * Handle the notification of an update in the download status.
     *
     * @param resource the name of the remote resource being downloaded.
     * @param expected the expected number of bytes to be downloaded.
     * @param count the number of bytes downloaded.
     */
    public void notifyUpdate( URL resource, int expected, int count )
    {
        getAdapter().notify( resource, expected, count );
    }

    /**
     * Handle the notification of the completion of of download process.
     * @param resource the url of the completed resource
     */
    public void notifyCompletion( URL resource )
    {
        if( getAdapter().isInfoEnabled() )
        {
            getAdapter().info( "downloaded: " + resource );
        }
    }
}

