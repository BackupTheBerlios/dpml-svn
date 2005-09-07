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

package net.dpml.component.control;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.part.Part;
import net.dpml.part.PartHandler;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.DelegationException;
import net.dpml.component.Service;
import net.dpml.component.Component;
import net.dpml.component.Container;
import net.dpml.component.ComponentException;

/**
 * The Controller interface defines the a contract for an object that provides general
 * component and part handling management services.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Controller extends PartHandler
{
   /**
    * Returns the identity of the object implementing this interface.
    * @return a uri identifying the object
    */
    URI getURI() throws RemoteException;

   /**
    * Construct a new top-level component.
    *
    * @param uri a uri identifying a part from which the component will be created.
    * @return the new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception IOException if an error occurs while attempting to resolve the component part uri
    * @exception PartNotFoundException if the uri could not be resolved to a physical resource
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    */
    Component newComponent( URI uri ) throws 
      ComponentException, IOException, PartNotFoundException, PartHandlerNotFoundException, 
      DelegationException, RemoteException;

   /**
    * Construct a new component using the supplied part as the defintion of the 
    * component type and deployment criteria.
    *
    * @param parent the enclosing parent container
    * @param part component definition including type and deployment data
    * @param name the name to assign to the new component
    * @return a new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    * @exception UnsupportedPartTypeException if the component type is recognized but not supported
    */
    Component newComponent( Container parent, Part part, String name )
      throws ComponentException, PartHandlerNotFoundException, DelegationException, RemoteException;

}
