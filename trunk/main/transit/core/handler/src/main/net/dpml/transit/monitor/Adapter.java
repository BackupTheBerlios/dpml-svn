/*
 * Copyright 2004 Stephen J. McConnell.
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

import net.dpml.transit.Logger;

/**
 * Utility interface defining a monitor adapter.  The contract defines a set of operations
 * dealing with the notification of debug, info and error messages, together with
 * and operation supporting the construction of a messages dedicated to a download
 * action.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Adapter.java 2674 2005-05-30 13:59:27Z mcconnell@dpml.net $
 */
public interface Adapter extends Logger
{
    /**
     * Hnadle download notification.
     * @param resource the resource under attention
     * @param expected the estimated download size
     * @param count the progress towards expected
     */
    void notify( URL resource, int expected, int count );
}


