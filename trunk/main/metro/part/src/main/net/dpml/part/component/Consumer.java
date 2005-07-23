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

package net.dpml.part.component;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.part.Part;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.DelegationException;


/**
 * The Consumer interface exposes an operation through which service providers
 * may be declared.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Consumer extends Remote
{
   /**
    * Return an array of components supporting the component.
    * @return the provider array
    */
    Component[] getProviders() throws RemoteException;

   /**
    * Return the component assigned as provider for the specified context key.
    * @param key the context entry key
    * @return the provider component
    */
    Component getProvider( String key ) throws RemoteException;

   /**
    * Set the component part to use as the provider for the supplied key.
    * @param key the context entry key
    * @param part the provider definition
    * @exception ComponentException if an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the handler for the part could not be resolved
    * @exception DelegationException if the handler raised an exception
    */
    void setProvider( String key, Part part )
      throws ComponentException, PartHandlerNotFoundException, DelegationException, RemoteException;
}
