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

package net.dpml.transit.model;

/**
 * An event signalling the change in priority of a host.
 */
public class HostPriorityEvent extends HostEvent 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final int m_priority;

   /**
    * Creation of a new host priority change event.
    * @param host the host that was changed
    * @param priority the new host prority value
    */
    public HostPriorityEvent( HostModel host, int priority )
    {
        super( host );
        m_priority = priority;
    }
    
   /**   
    * Return the new host priority value.
    * @return the assigned host prority
    */
    public int getPriority()
    {
        return m_priority;
    }
}
