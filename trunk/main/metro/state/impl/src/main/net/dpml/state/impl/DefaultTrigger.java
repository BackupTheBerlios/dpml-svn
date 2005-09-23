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
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultTrigger implements Trigger, Serializable
{
    private final TriggerEvent m_event;
    private final Action m_action;
    
    public DefaultTrigger( final TriggerEvent event, final Action action )
    {
        m_event = event;
        m_action = action;
    }
    
    public TriggerEvent getEvent()
    {
        return m_event;
    }
    
    public Action getAction()
    {
        return m_action;
    }
    
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
            else if( !m_action.equals( trigger.getAction() ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    public String toString()
    {
        return "trigger:" + m_event.getName();
    }
}
