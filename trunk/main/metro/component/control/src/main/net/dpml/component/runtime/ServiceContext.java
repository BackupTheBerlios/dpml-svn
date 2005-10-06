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
import java.rmi.RemoteException;

import net.dpml.component.info.ServiceDescriptor;


/**
 * A ServiceContext is a local interface implemented by components that 
 * provide support for the resolution of service requests from sibling 
 * components.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface ServiceContext 
{
   /**
    * Handle a request for the provision of a component relative to the supplied
    * uri. 
    *
    * @param uri a uri identifying or resolvable to a service
    */
    Component lookup( URI uri ) throws ServiceException, RemoteException;

   /**
    * Handle a request for the provision of a component relative to a supplied descriptor.
    *
    * @param service a service descriptor
    */
    Component lookup( ServiceDescriptor service ) throws ServiceException, RemoteException;

}
