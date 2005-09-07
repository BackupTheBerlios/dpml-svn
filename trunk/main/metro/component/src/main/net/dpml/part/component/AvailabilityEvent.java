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

package net.dpml.part.component;

import java.util.EventObject;

/** 
 * Event raised to signal a change in the availability of a component.
 */
public class AvailabilityEvent extends EventObject
{   
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final boolean m_available;

    /**
     * Creation of a new availability event.
     * @param source the service instance
     * @param available the service availability status
     */
    public AvailabilityEvent( Service source, boolean available )
    {
        super( source );
        m_available = available;
    }

    public boolean isAvailable()
    {
        return m_available;
    }
}
