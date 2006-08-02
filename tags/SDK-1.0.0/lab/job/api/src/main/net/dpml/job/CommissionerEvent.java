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

package net.dpml.job;

import java.util.EventObject;

/**
 * Event raised by a commissioner.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CommissionerEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    private final boolean m_flag;
    private final long m_duration;

   /**
    * Construct a new <code>CommissionerEvent</code>.
    *
    * @param target the commissionable target
    * @param flag if true then commissioning else decommissioning
    * @param duration the applicable duration relative to the stage
    */
    public CommissionerEvent( final Commissionable target, boolean flag, long duration )
    {
        super( target );
        m_flag = flag;
        m_duration = duration;
    }
    
   /**
    * Return true if the event is related to commissioning of a Commissionable
    * instance, otherwise the event is related to a decommissioning activity.
    * @return the commossion/decommission flag
    */
    public boolean isCommissioning()
    {
        return m_flag;
    }
    
   /**
    * Get the application duration.  If the event is related to the started, 
    * interrupted, terminated, or failed phase the value returned is the timeout
    * constraint. If the stage is normal completion the value is equal to the 
    * actual time taken to execute the commissioning or decommissioning process.
    * @return the applicable duration
    */
    public long getDuration()
    {
        return m_duration;
    }
}

