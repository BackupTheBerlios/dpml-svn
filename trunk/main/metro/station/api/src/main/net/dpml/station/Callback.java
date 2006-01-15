/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

import net.dpml.part.remote.Component;

import net.dpml.transit.PID;

/**
 * The Callback interface defines a service provider to a process through
 * which the process may issue notification of state change.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Callback extends Remote
{
   /**
    * Method invoked by a process to signal that the process has started.
    *
    * @param pid the process identifier
    * @param handler optional handler reference
    * @exception RemoteException if a remote error occurs
    */
    void started( PID pid, Component handler ) throws RemoteException;
    
   /**
    * Method invoked by a process to signal that the process has 
    * encounter an error condition.
    *
    * @param throwable the error condition
    * @param fatal if true the process is requesting termination
    * @exception RemoteException if a remote error occurs
    */
    void error( Throwable throwable, boolean fatal ) throws RemoteException;
    
   /**
    * Method invoked by a process to send a arbitary message to the 
    * the callback handler.
    *
    * @param message the message
    * @exception RemoteException if a remote error occurs
    */
    void info( String message ) throws RemoteException;
    
   /**
    * Method invoked by a process to signal its imminent termination.
    *
    * @exception RemoteException if a remote error occurs
    */
    void stopped() throws RemoteException;
    
}

