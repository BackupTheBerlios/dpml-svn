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

import java.net.URI;

import net.dpml.part.control.ResourceUnavailableException;
import net.dpml.part.control.Component;
import net.dpml.part.state.State;

/**
 * The Manager interface is an interface representing a component controller.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public interface Manager
{
   /**
    * Return a uri identifiying this control.
    * @return the uri
    */
    URI getURI();

   /**
    * Return an instance of the component type represented 
    * by the supplied model.
    * 
    * @param component the component model
    * @param policy the proxy creation policy
    * @return the resolved instance
    */
    Object resolve( Component component, boolean policy ) throws Exception;

   /**
    * Applies a state transition identified by a supplied transition key.
    *
    * @param instance the component instance to which the transition is to be applied
    * @param key the key identifying the transition to apply to the component's controller
    * @return the state resulting from the transition
    * @exception if a transition error occurs
    */
    State apply( Component instance, String key ) throws Exception;

   /**
    * Executes an operation identified by a supplied operation key.
    *
    * @param instance the component instance to which the operation is to be executed against
    * @param key the key identifying the operation to execute 
    * @exception if a transition error occurs
    */
    void execute( Component instance, String key ) throws Exception;

   /**
    * Release a reference to an object managed by the instance.
    * 
    * @param instance the instance to release
    */
    void release( Object instance );

   /**
    * Initialize the component.  
    * @param component the component 
    */
    void initialize( Component component ) throws Exception;

   /**
    * Termination of the component.
    * @param component the component 
    */
    void terminate( Component component );

}
