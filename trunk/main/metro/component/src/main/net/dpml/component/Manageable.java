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

package net.dpml.part.service;

import java.net.URI;

//import net.dpml.component.state.State;
import net.dpml.component.state.StateListener;
import net.dpml.component.state.StateEvent;

import net.dpml.state.State;

/**
 * The Initializable interface is implemented by components that support an 
 * explicit establishment process.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Manageable
{
   /**
    * Initialize the component.  
    */
    void initialize() throws Exception;

   /**
    * Return the current state of the component.
    * @return the current state
    */
    State getState();

   /**
    * Add a state listener to the provider.
    * @param listener the state listener
    */
    void addStateListener( StateListener listener );

   /**
    * Remove a state listener from the provider.
    * @param listener the state listener
    */
    void removeStateListener( StateListener listener );

   /**
    * Applies a state transition identified by a supplied transition key.
    *
    * @param key the key identifying the transition to apply to the component's controller
    * @return the state resulting from the transition
    * @exception if a transition error occurs
    */
    State apply( String key ) throws Exception;

   /**
    * Executes an operation identified by a supplied operation key.
    *
    * @param key the key identifying the operation to execute 
    * @exception if a transition error occurs
    */
    void execute( String key ) throws Exception;

   /**
    * Terminate the component and its associated resources.
    */
    void terminate();
}

