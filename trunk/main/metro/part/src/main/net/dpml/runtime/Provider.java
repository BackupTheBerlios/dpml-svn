/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.runtime;

import net.dpml.state.State;
import net.dpml.state.StateListener;

/**
 * Component interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Provider
{
   /**
    * Add a state change listener to the state machine.
    * @param listener the state listener
    */
    void addStateListener( final StateListener listener );
    
   /**
    * Remove a state listener from the state machine.
    * @param listener the state listener
    */
    void removeStateListener( final StateListener listener );
    
   /**
    * Return the current state of the instance.
    * @return the current state
    */
    State getState();

   /**
    * Return a fully commissioned instance.
    * @return the instance
    */
    <T>T getInstance( Class<T> type );
    
}

