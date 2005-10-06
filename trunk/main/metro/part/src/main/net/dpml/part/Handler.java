/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.part;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.net.URI;

import net.dpml.state.State;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;

/**
 * The Handler represents a remote interface to a single runtime instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Handler extends Remote
{
   /**
    * Returns the current state of the control.
    * @return the current runtime state
    */
    State getState() throws RemoteException;
    
   /**
    * Returns the active status of the handler.
    * @return TRUE if the handler has been activated otherwise FALSE
    */
    boolean isActive() throws RemoteException;
    
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
}

