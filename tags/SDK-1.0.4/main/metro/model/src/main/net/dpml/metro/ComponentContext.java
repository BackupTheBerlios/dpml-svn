/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro;

import java.rmi.RemoteException;

import net.dpml.component.Controller;

/**
 * The ComponentContext interface may be referenced by a component 
 * context inner interface to expose container related services to 
 * a component implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentContext
{
   /**
    * Return the current controller.
    * @return the root system controller
    * @exception RemoteException if a remote I/O error occurs
    */
    Controller getController() throws RemoteException;
}

