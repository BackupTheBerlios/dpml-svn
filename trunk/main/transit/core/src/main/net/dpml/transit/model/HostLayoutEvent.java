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
 * An event signalling the change to the layout model assigned to a host model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HostLayoutEvent extends HostEvent 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final LayoutModel m_layout;

   /**
    * Event signalliing the change to a host layout model.
    * @param host the host model that was changed
    * @param layout the layout model that was assinged to the host
    */
    public HostLayoutEvent( HostModel host, LayoutModel layout )
    {
        super( host );
        m_layout = layout;
    }
    
   /**
    * Return the layout model that was assigned to the host.
    * @return the assinged layout model
    */
    public LayoutModel getLayoutModel()
    {
        return m_layout;
    }
}
