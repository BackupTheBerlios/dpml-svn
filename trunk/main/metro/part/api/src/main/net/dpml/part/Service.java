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
 * The Component interface is implemented by objects that handle the runtime
 * state of a component instance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Service extends Remote
{
   /**
    * Return the service class.
    * @return the service class
    * @exception RemoteException if a remoting I/O error occurs
    */
    Class getServiceClass() throws RemoteException;
    
   /**
    * Return the service version.
    * @return the version
    * @exception RemoteException if a remoting I/O error occurs
    */
    Version getVersion() throws RemoteException;
    
}

