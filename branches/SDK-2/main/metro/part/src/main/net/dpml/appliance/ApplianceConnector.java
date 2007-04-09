/*
 * Copyright 2004-2007 Stephen J. McConnell.
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

package net.dpml.appliance;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.concurrent.TimeUnit;

/**
 * Appliance connector.
 */
public interface ApplianceConnector extends Remote
{
   /**
    * Connect an appliance to the connector.
    * @param appliance the appliance 
    * @exception RemoteException if a remote error occurs
    */
    void connect( Appliance appliance ) throws RemoteException;
    
   /**
    * Retrieve the connected appliance.
    * @param units the timout units
    * @param timeout duration (in units) to wait for a connection
    * @exception IOException if an I/O error occurs
    */
    Appliance getAppliance( TimeUnit units, int timeout ) throws IOException;
    
}

