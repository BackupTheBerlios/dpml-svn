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

package net.dpml.composition.runtime;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.Map;
import java.util.Hashtable;
import java.util.Collections;
import java.util.HashSet;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.data.ComponentProfile;

import net.dpml.part.control.DelegationException;
import net.dpml.part.control.HandlerNotFoundException;
import net.dpml.part.control.PartNotFoundException;

import net.dpml.part.manager.Component;
import net.dpml.part.manager.ComponentException;
import net.dpml.part.manager.ComponentNotFoundException;
import net.dpml.part.manager.Container;

import net.dpml.part.state.NoSuchOperationException;
import net.dpml.part.state.NoSuchTransitionException;
import net.dpml.part.state.State;
import net.dpml.part.part.Part;

import net.dpml.logging.Logger;

/**
 * The CompositionHandler class manages the containment aspects of a component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class CompositionHandler extends ComponentHandler implements Container
{ 
    private final Map m_components = new Hashtable();

    public CompositionHandler(
      Logger logger, CompositionController controller, ClassLoader classloader, URI uri, 
      ComponentProfile profile, Component parent ) 
      throws ComponentException, HandlerNotFoundException, DelegationException
    {
        super( logger, controller, classloader, uri, profile, parent );
    }

   /**
    * Add a component to the collection of components managed by the container.
    *
    * @param uri a part uri
    * @param key the key under which the component will be referenced
    * @return the component
    */
    public Component addComponent( URI uri, String key ) 
      throws IOException, ComponentException, PartNotFoundException, 
      DelegationException, HandlerNotFoundException
    {
        CompositionController controller = getController();
        Part part = controller.loadPart( uri );
        Component component = controller.newComponent( this, part, key );
        getPartsTable().addComponent( key, component );
        return component;
    }

   /**
    * Retrieve a component using a supplied key.
    * @param key the key
    * @return the component
    * @exception ComponentNotFoundException if the key is unknown
    */
    public Component getComponent( String key ) throws ComponentNotFoundException
    {
        Component component = getPartsTable().getComponent( key );
        if( null == component )
        {
            throw new ComponentNotFoundException( key );
        }
        else
        {
            return component;
        }
    }
}
