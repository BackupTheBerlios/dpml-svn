/*
 * Copyright 2004 Stephen J. McConnell.
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
 * A monitor of a network activity or activities.
 *
 * <p>
 *   The NetworkMonitor must be thread safe.
 * </p>
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ConnectionMonitor.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 * @see Monitor
 */
public interface ConnectionMonitor extends Monitor
{
   /**
    * Notify the monitor that a connection was opened.
    * @param url the url on which the open connection was issued
    */
    void connectionOpened( URL url );

   /**
    * Notify the monitor that a connection was started.
    * @param url the target connection
    */
    void connectStarted( URL url );


   /**
    * Notify the monitor that a connection was completed.
    * @param url the target connection
    */
    void connectCompleted( URL url );
}
