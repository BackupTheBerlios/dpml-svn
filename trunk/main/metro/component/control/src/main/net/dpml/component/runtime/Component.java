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

package net.dpml.component.runtime;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.component.info.ServiceDescriptor;

import net.dpml.part.Handler;
import net.dpml.part.Service;
import net.dpml.part.ServiceNotFoundException;

/**
 * The Component interface is implemented by objects that handle the runtime
 * state of a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Component extends Handler
{
   /**
    * Return a handler capable of supporting the requested service.
    * @param descriptor the service descriptor
    */
    Handler lookup( Service service ) throws ServiceNotFoundException, RemoteException;
    
   /**
    * Return an array of service descriptors corresponding to 
    * the service contracts that the component publishes.
    * @return the service descriptor array
    */
    //ServiceDescriptor[] getDescriptors() throws RemoteException;

   /**
    * Return the part that defines this component.
    * @return the component part definition
    */
    //Part getDefinition() throws RemoteException;

   /**
    * Return the short name of this component.
    * @return the component name
    */
    //String getName() throws RemoteException;

}

