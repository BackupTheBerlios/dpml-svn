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

package net.dpml.runtime;

/**
 * Event triggered as a result of a state change.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProviderEvent extends ComponentEvent
{
    private final Provider m_provider;
    private final Status m_action;
    
   /**
    * Construct a new <code>StateEvent</code>.
    *
    * @param source the source component 
    * @param provider the provider instance initiating the event
    * @param action the modified Status enumeration
    */
    public ProviderEvent( final Component source, Provider provider, Status action )
    {
        super( source );
        
        m_provider = provider;
        m_action = action;
    }
    
    public Provider getProvider()
    {
        return m_provider;
    }
    
    public Status getStatus()
    {
        return m_action;
    }
    
    public String toString()
    {
        String id = super.toString();
        return id + "#" + getStatus();
    }
}

