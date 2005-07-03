/*
 * Copyright 2005 Stephen McConnell
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

import java.util.EventObject;

/** 
 * Exception thrown if a requested resource is unavailable.
 *
 */
public class AvailabilityEvent extends EventObject
{   
    private final boolean m_available;

    /**
     * Creation of a new availability event.
     * @param model the component model
     * @param available the component model availability status
     */
    public AvailabilityEvent( Component model, boolean available )
    {
        super( model );
        m_available = available;
    }

    public boolean isAvailable()
    {
        return m_available;
    }
}
