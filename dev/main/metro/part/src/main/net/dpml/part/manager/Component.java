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

import java.net.URI;
import java.util.Map.Entry;

import net.dpml.part.state.State;
import net.dpml.part.state.StateEvent;
import net.dpml.part.state.StateListener;

/**
 * The Component interface is implemented by objects that handle the runtime
 * state of a component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Component 
{
   /**
    * Return the short name of this component.
    * @return the component name
    */
    String getName();

   /**
    * Returns the identity of the component.
    * @return a uri identifying the component
    */
    URI getURI();

   /**
    * Return the operational status of the component.
    * @return the operational status
    */
    boolean isOperational();

   /**
    * Add an availability listener to the provider.
    * @param listener the availability listener
    */
    void addAvailabilityListener( AvailabilityListener listener );

   /**
    * Remove an availability listener from the provider.
    * @param listener the availability listener
    */
    void removeAvailabilityListener( AvailabilityListener listener );

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
    * Return an initialized instance of the component.
    * @return the resolved instance
    */
    Object resolve() throws Exception;

   /**
    * Return an initialized instance of the component using a supplied isolation policy.
    * If the isolation policy is TRUE an implementation shall make best efforts to isolate
    * implementation concerns under the object that is returned.  Typically isolation 
    * involves the creation of a proxy of a component implementation instance that 
    * exposes a component's service interfaces to a client.  If the isolation policy if
    * FALSE the implementation shall return the component implementation instance.
    * 
    * @param isolation the isolation policy
    * @return the resolved instance
    */
    Object resolve( boolean isolation ) throws Exception;

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
    * Release a reference to an object managed by the instance.
    * 
    * @param instance the instance to release
    */
    void release( Object instance );

   /**
    * Terminate the component and its associated instance.
    */
    void terminate();

}

