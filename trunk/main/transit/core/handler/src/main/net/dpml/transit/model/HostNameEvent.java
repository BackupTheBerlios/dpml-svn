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
 * An event signalling the change of name of a host.
 */
public class HostNameEvent extends HostEvent 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final String m_name;

   /**
    * Creation of a new host name change event.
    * @param host the host that was changed
    * @param name the new host name
    */
    public HostNameEvent( HostModel host, String name )
    {
        super( host );
        m_name = name;
    }
    
   /**   
    * Return the new host name.
    * @return the host name that was assingned to the host
    */
    public String getName()
    {
        return m_name;
    }
}
