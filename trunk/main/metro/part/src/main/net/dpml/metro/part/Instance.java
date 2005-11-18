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

package net.dpml.metro.part;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.metro.state.State;
import net.dpml.metro.state.StateListener;

import net.dpml.transit.model.Value;

/**
 * Instance holder.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Instance extends Remote
{
    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

   /**
    * Returns the current state of the control.
    * @return the current runtime state
    */
    State getState() throws RemoteException;
    
   /**
    * Add a state listener to the control.
    * @param listener the state listener
    */
    void addStateListener( StateListener listener ) throws RemoteException;

   /**
    * Remove a state listener from the control.
    * @param listener the state listener
    */
    void removeStateListener( StateListener listener ) throws RemoteException;

   /**
    * Return the runtime value associated with this instance.
    * @param isolate if TRUE the value returned is a proxy exposing the 
    *    service interfaces declared by the component type otherwise 
    *    the instance value is returned.
    */
    Object getValue( boolean isolate ) throws RemoteException;
}
