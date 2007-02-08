/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.station;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Station interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Station extends Remote
{
    static final String STATION_CONNECTOR_KEY = "dpml.station.key";
    static final String STATION_JMX_URI_KEY = "dpml.station.jmx.uri";
    //static final String STATION_RMI_PORT = "dpml.station.rmi.port";
    
    void shutdown() throws RemoteException;
}

