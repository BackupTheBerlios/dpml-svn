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
package org.apache.avalon.jmx;

import org.apache.avalon.composition.model.ComponentModel;

/** An interface to register and unregister Merlin Composition ComponentModels.
 *
 * @author <a href="dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
public interface ComponentRegistrationManager
{
    /**
     * Adds a component for management with this ComponentRegistrationManager.
     *
     * @param componentModel the ComponentModel of the component to manage
     * @throws ComponentRegistrationException the the component cannot be managed
     */
    void register( ComponentModel componentModel ) throws ComponentRegistrationException;

    /**
     * Removes a component from this ComponentRegistrationManager.
     *
     * @param componentModel the ComponentModel of the component to remove
     * @throws ComponentRegistrationException if the component does not exist or an error occurs unregistering it.
     */
    void unregister( ComponentModel componentModel ) throws ComponentRegistrationException;

}
