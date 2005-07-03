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

import java.net.URL;

/**
 * A connection monitor implementation that routes connection notification requests
 * to registered listeners.
 */
public class ConnectionMonitorRouter extends AbstractMonitorRouter
    implements ConnectionMonitor, Router
{
    //--------------------------------------------------------------------
    // ConnectionMonitor
    //--------------------------------------------------------------------

   /**
    * Notify registered monitors of a connection opened event.
    * @param url the target url
    */
    public void connectionOpened( URL url )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            ConnectionMonitor monitor = (ConnectionMonitor) monitors[i];
            monitor.connectionOpened( url );
        }
    }

   /**
    * Notify registered monitors of a connection started event.
    * @param url the target url
    */
    public void connectStarted( URL url )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            ConnectionMonitor monitor = (ConnectionMonitor) monitors[i];
            monitor.connectStarted( url );
        }
    }

   /**
    * Notify registered monitors of a connection completed event.
    * @param url the target url
    */
    public void connectCompleted( URL url )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            ConnectionMonitor monitor = (ConnectionMonitor) monitors[i];
            monitor.connectCompleted( url );
        }
    }

   /**
    * Add a monitor to the set of registered monitors.
    * @param monitor the monitor to add
    */
    public void addMonitor( Monitor monitor )
    {
        if( !( monitor instanceof ConnectionMonitor ) )
        {
            throw new IllegalArgumentException( "monitor must be ConnectionMonitor type." );
        }
        else
        {
            super.addMonitor( monitor );
        }
    }
}

