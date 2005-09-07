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

import net.dpml.component.state.State;
import net.dpml.component.state.StateEvent;
import net.dpml.component.state.StateListener;

/**
 * The Available interface declares a set of operations implemented by 
 * components that are available aware.  Available awareness is the notion 
 * that a component may declare changes in availability over its lifetime.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Available
{
   /**
    * Issue a request to the service to prepare for operations.
    * @exception AvailabilityException if the service cannot be made available
    */
    void prepare() throws AvailabilityException;

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

}
