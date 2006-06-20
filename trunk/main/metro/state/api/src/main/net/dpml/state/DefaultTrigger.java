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

package net.dpml.state.impl;

import java.io.Serializable;

import net.dpml.state.Trigger;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.Action;

/**
 * Default implementation of trigger.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultTrigger implements Trigger, Serializable
{
    private final TriggerEvent m_event;
    private final Action m_action;
    
   /**
    * Creation of a new trigger.
    * @param event the trigger event constant
    * @param action the action fired by the trigger
    */
    public DefaultTrigger( final TriggerEvent event, final Action action )
    {
        if( null == event )
        {
            throw new NullPointerException( "event" );
        }
        if( null == action )
        {
            throw new NullPointerException( "action" );
        }
        m_event = event;
        m_action = action;
    }
    
   /**
    * Return the event enumneration that this trigger is associated with.
    * @return the triggering event class
    */
    public TriggerEvent getEvent()
    {
        return m_event;
    }
    
   /**
    * Return the actions  that this trigger initiates.
    * @return the triggered action
    */
    public Action getAction()
    {
        return m_action;
    }
    
   /**
    * Compare this object to another for equality.
    * @param other the other object
    * @return true if the object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof DefaultTrigger )
        {
            DefaultTrigger trigger = (DefaultTrigger) other;
            if( !m_event.equals( trigger.getEvent() ) )
            {
                return false;
            }
            else
            {
                return m_action.equals( trigger.getAction() );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hashcode for this instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = m_event.hashCode();
        hash ^= m_action.hashCode();
        return hash;
    }

   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "trigger:" + m_event.getName();
    }
}
