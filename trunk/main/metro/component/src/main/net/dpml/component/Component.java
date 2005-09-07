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

package net.dpml.component;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.part.Part;
import net.dpml.component.Service;
import net.dpml.component.Identifiable;
import net.dpml.component.Resolvable;

/**
 * The Component interface is implemented by objects that handle the runtime
 * state of a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Component extends Service, Identifiable, Resolvable, Remote
{
   /**
    * Return the short name of this component.
    * @return the component name
    */
    String getName() throws RemoteException;

   /**
    * Return the part that defines this component.
    * @return the component part definition
    */
    Part getDefinition() throws RemoteException;

}

