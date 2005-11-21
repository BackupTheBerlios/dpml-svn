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
 * A router that handles multicasr distribution of monitor events to registered monitors.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class NetworkMonitorRouter extends AbstractMonitorRouter
    implements NetworkMonitor, Router
{

    //--------------------------------------------------------------------
    // NetworkMonitor
    //--------------------------------------------------------------------

   /**
    * Notify all subscribing monitors of a updated event.
    * @param resource the url of the updated resource
    * @param expected the size in bytes of the download
    * @param count the progress in bytes
    */
    public void notifyUpdate( URL resource, int expected, int count )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            NetworkMonitor monitor = (NetworkMonitor) monitors[i];
            monitor.notifyUpdate( resource, expected, count );
        }
    }

   /**
    * Notify all subscribing monitors of a download completion event.
    * @param resource the url of the downloaded resource
    */
    public void notifyCompletion( URL resource )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            NetworkMonitor monitor = (NetworkMonitor) monitors[i];
            monitor.notifyCompletion( resource );
        }
    }

   /**
    * Add a monitor to the list of monitors managed by this router.
    * @param monitor the monitor to add
    * @exception IllegalArgumentException if the supplied monitor is not a NetworkMonitor
    */
    public void addMonitor( Monitor monitor ) throws IllegalArgumentException 
    {
        if( !( monitor instanceof NetworkMonitor ) )
        {
            throw new IllegalArgumentException( "monitor must be NetworkMonitor type." );
        }
        else
        {
            super.addMonitor( monitor );
        }
    }
}

