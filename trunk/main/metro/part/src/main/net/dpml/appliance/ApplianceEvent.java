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

package net.dpml.appliance;

import java.util.EventObject;

import net.dpml.runtime.Status;

/**
 * Event triggered as a result of a state change within an appliance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ApplianceEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final Appliance m_appliance;
    private final Status m_status;

   /**
    * Construct a new <code>StateEvent</code>.
    *
    * @param source the source appliance
    * @param status the appliance state
    */
    public ApplianceEvent( final Appliance source, Status status )
    {
        super( source );
        m_appliance = source;
        m_status = status;
    }

   /**
    * Return the appliance that initiated the event.
    * @return the source appliance
    */
    public Appliance getAppliance()
    {
        return m_appliance;
    }
    
   /**
    * Return the appliance status.
    * @return the status enumeration
    */
    public Status getStatus()
    {
        return m_status;
    }
}

