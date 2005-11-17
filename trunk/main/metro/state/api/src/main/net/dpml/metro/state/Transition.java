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

package net.dpml.metro.state;

import java.net.URI;

/**
 * Interface describing a transition that may be performed under an activate 
 * state.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Transition extends Action
{  
   /**
    * Set the state that this transition is a part of.
    * @param state the owning state
    */
    void setState( State state );
    
   /**
    * Return the state that this transition is a part of.
    * @return the owning state
    */
    State getState();
    
   /**
    * Return the transition target state name
    * @return the target state name
    */
    String getTargetName();
    
   /**
    * Return the handler uri associated with this state transition.
    * @return the handler uri
    */
    URI getHandlerURI();
}
