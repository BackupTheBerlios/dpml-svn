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

/**
 * A Control is an object that is responsible for the activatation and 
 * deactivation of handlers. Objects gaining a reference to a control have 
 * ultimate discression concerning activiation and deactivation of the 
 * control.
 *
 * @see PartHandler#createHandler(Context)
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Control extends Remote
{
   /**
    * Create a new runtime handler using a supplied context.
    * @param context the managed context
    * @return the runtime handler
    */
    Handler createHandler( Context context ) throws RemoteException;

   /**
    * Initiate activation of a runtime handler.
    * @param handler the runtime handler
    * @exception Exception if an activation error occurs
    */
    public void activate( Handler handler ) throws ControlException, RemoteException;
    
   /**
    * Initiate deactivation of a supplied handler.
    * @exception Exception if an activation error occurs
    */
    public void deactivate( Handler handler ) throws RemoteException;

}
