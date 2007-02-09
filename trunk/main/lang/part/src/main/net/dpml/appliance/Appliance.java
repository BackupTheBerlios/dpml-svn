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

package net.dpml.appliance;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;


/**
 * Appliance interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Appliance extends Remote
{
    void addApplianceListener( ApplianceListener listener ) throws RemoteException;
    
    void removeApplianceListener( ApplianceListener listener ) throws RemoteException;
    
    void commission() throws IOException;
    
    void decommission() throws RemoteException;
    
   /**
    * Return a value assignable to the supplied remote type or null if the type
    * cannot be resolved from the underlying component.
    * @param type the service type
    * @return an instance of the type or null
    * @exception IOException if an IO error occurs
    */
    //public <T>T getContentForClass( Class<T> type ) throws IOException;
    
}

