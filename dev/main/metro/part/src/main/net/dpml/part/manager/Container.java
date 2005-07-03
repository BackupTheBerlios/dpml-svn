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

package net.dpml.part.manager;

import java.io.IOException;
import java.net.URI;

import net.dpml.part.control.DelegationException;
import net.dpml.part.control.HandlerNotFoundException;
import net.dpml.part.control.PartNotFoundException;

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
    Component addComponent( URI part, String key ) 
      throws IOException, ComponentException, PartNotFoundException, 
      HandlerNotFoundException, DelegationException;

   /**
    * Retrieve a component using a supplied key.
    * @param key the key
    * @return the component
    * @exception ComponentNotFoundException if the key is unknown
    */
    Component getComponent( String key ) throws ComponentNotFoundException;

}
