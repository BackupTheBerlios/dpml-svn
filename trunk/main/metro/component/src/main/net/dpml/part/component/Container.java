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

import java.io.IOException;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.part.DelegationException;
import net.dpml.part.Part;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.component.Service;

/**
 * The Container interface defines the a contract for a component that 
 * manages a collection of subsidiary components.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Container extends Component
{
   /**
    * Add a component to the collection of components managed by the container.
    *
    * @param part a part uri
    * @param key the key under which the component will be referenced
    * @return the component
    */
    Component addComponent( String key, URI part ) 
      throws IOException, ComponentException, PartNotFoundException, 
      PartHandlerNotFoundException, DelegationException, RemoteException;

   /**
    * Add a component to the collection of components managed by the container.
    *
    * @param part a part
    * @param key the key under which the component will be referenced
    * @return the component
    */
    Component addComponent( String key, Part part ) 
      throws ComponentException, PartHandlerNotFoundException, DelegationException, RemoteException;

   /**
    * Retrieve a component using a supplied key.
    * @param key the key
    * @return the component
    * @exception ComponentNotFoundException if the key is unknown
    */
    Component getComponent( String key ) throws ComponentNotFoundException, RemoteException;

   /**
    * Return the ordered startup sequence for the set of components contained 
    * within the container.
    * @return the startup sequence
    */
    Component[] getStartupSequence() throws RemoteException;

   /**
    * Return the ordered shutdown sequence for the set of components contained 
    * within the container.
    * @return the shutdown sequence
    */
    Component[] getShutdownSequence() throws RemoteException;

}

