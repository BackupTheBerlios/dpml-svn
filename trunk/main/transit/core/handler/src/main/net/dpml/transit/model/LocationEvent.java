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

import java.net.URI;

/**
 * An event pertaining to a change in a plugin uri assigned to 
 * a codebase model.
 */
public class LocationEvent extends CodeBaseEvent
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final URI m_plugin;

   /**
    * Construction of a new codebase change event.
    * @param source the codebase model initiating the event
    * @param plugin the uri assigned as the codebase
    */
    public LocationEvent( CodeBaseModel source, URI plugin )
    {
        super( source );
        m_plugin = plugin;
    }
    
   /**
    * Return the new codebase plugin uri.
    * @return the codebase uri
    */
    public URI getCodeBaseURI()
    {
        return m_plugin;
    }
}
