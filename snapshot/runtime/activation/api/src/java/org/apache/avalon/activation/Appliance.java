/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.activation;

import org.apache.avalon.composition.model.Commissionable;
import org.apache.avalon.composition.model.Resolver;

/**
 * An Appliance is the basic tool merlin wraps around a component to
 * provide support for lifecycle and lifestyle management. Different
 * implementations of Appliance can be plugged into the merlin system
 * to allow merlin to manage a variety of components.
 *
 * The name appliance is used to call up an association with a kitchen
 * utility like a microwave. Merlin acts as a chef in his kitchen, and uses
 * various appliances to "cook up" various components as the restaurant
 * customers (which can be other components or systems on the other end
 * on the planet) ask for them.
 *
 * An appliance manages the establishment of a component
 * type relative to a deployment criteria. Once established, an appliance
 * provides support for the deployment of component instances on request.
 * An appliance is responsible for component lifestyle and lifecycle
 * management during the deployment and decommission cycles.
 *
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Appliance.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Appliance extends Commissionable, Resolver
{
}
