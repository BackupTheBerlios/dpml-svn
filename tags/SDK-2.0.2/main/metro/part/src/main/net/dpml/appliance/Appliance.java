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

import net.dpml.state.State;

/**
 * Appliance interface.  An appliance represents a component or component collection
 * that can be comissioned and decommissioned.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Appliance extends Remote
{
   /**
    * Return the current state of the instance.
    * @return the current state
    * @exception RemoteException if a RMI remoting exception occurs
    */
    State getState() throws RemoteException;
    
   /**
    * Add an appliance listener to the appliance.
    * @param listener the appliance listener
    * @exception RemoteException if a RMI error occurs
    */
    void addApplianceListener( ApplianceListener listener ) throws RemoteException;
    
   /**
    * Remove an appliance listener from the appliance.
    * @param listener the appliance listener
    * @exception RemoteException if a RMI error occurs
    */
    void removeApplianceListener( ApplianceListener listener ) throws RemoteException;
    
   /**
    * Commission the appliance.
    * @exception IOException if a I/O error occurs
    */
    void commission() throws IOException;
    
   /**
    * Decommission the appliance.
    * @exception RemoteException if a RMI error occurs
    */
    void decommission() throws RemoteException;
    
   /**
    * Return an array of subsidiary appliance instances managed by this appliance.
    * @return an array of subsidiary appliance instances
    * @exception RemoteException if a RMI error occurs
    */
    Appliance[] getChildren() throws RemoteException;
  
   /**
    * Get the appliance name.
    * @return the name
    * @exception RemoteException if a RMI error occurs
    */
    String getName() throws RemoteException;
    
   /**
    * Get the appliance codebase uri.
    * @return the uri as a string
    * @exception RemoteException if a RMI error occurs
    */
    String getCodebaseURI() throws RemoteException;
    
   /**
    * Get the commissioned state of the appliance.
    * @return TRUE if the appliance is commissioned
    * @exception RemoteException if a RMI error occurs
    */
    boolean isCommissioned() throws RemoteException;
}

