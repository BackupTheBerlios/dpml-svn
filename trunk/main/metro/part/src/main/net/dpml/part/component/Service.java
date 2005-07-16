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

package net.dpml.part.component;

import java.net.URI;

import net.dpml.part.state.State;
import net.dpml.part.state.StateEvent;
import net.dpml.part.state.StateListener;

/**
 * A Service is a local interface implemented by an object that 
 * is capable of providing services conformant with a published 
 * service descriptions.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Service
{
   /**
    * Return an array of service descriptors corresponding to 
    * the service contracts that the service publishes.
    * @return the service descriptor array
    */
    ServiceDescriptor[] getDescriptors();

}
