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

package net.dpml.component;

import java.util.EventObject;

/**
 * Event triggered as a result of change to the value of a context entry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StatusEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final Status m_new;
    private final Status m_old;

   /**
    * Construct a new <code>StatusEvent</code>.
    *
    * @param source the source provider
    * @param oldStatus the initial provider status
    * @param newStatus the new provider status
    */
    public StatusEvent( final Provider source, Status oldStatus, Status newStatus )
    {
        super( source );
        
        m_new = newStatus;
        m_old = oldStatus;
    }

   /**
    * Return the prior status value.
    * @return the provider status prior to the status change
    */
    public Status getOldStatus()
    {
        return m_old;
    }

   /**
    * Return the status value.
    * @return the provider status
    */
    public Status getNewStatus()
    {
        return m_new;
    }

   /**
    * Return the component model that initiating the event.
    * @return the source model
    */
    public Provider getProvider()
    {
        return (Provider) super.getSource();
    }
}

