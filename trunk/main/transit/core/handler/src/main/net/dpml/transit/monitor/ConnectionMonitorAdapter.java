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
 * Adapts connection events to logging messages.
 *
 * <p>
 *   The NetworkMonitor must be thread safe.
 * </p>
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ConnectionMonitorAdapter.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 * @see net.dpml.transit.monitor.Monitor
 */
public class ConnectionMonitorAdapter extends AbstractAdapter
    implements ConnectionMonitor
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new adaptive connection monitor.
    * @param adapter the adapter to assign to the monitor
    */
    public ConnectionMonitorAdapter( Adapter adapter )
    {
        super( adapter );
    }

    // ------------------------------------------------------------------------
    // ConnectionMonitor
    // ------------------------------------------------------------------------

   /**
    * Notify the monitor that a connection was opened.
    * @param url the url on which the open connection was issued
    */
    public void connectionOpened( URL url )
    {
        if( getAdapter().isDebugEnabled() )
        {
            getAdapter().debug( "opened connection: " + url );
        }
    }

   /**
    * Notify the monitor that a connection was started.
    * @param url the target connection
    */
    public void connectStarted( URL url )
    {
    }

   /**
    * Notify the monitor that a connection was completed.
    * @param url the target connection
    */
    public void connectCompleted( URL url )
    {
        if( getAdapter().isDebugEnabled() )
        {
            getAdapter().debug( "closed connection: " + url );
        }
    }
}
