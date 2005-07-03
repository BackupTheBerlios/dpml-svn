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
import java.util.EventObject;

/**
 * An event pertaining to the change in a plugin uri.
 */
public class CodeBaseEvent extends EventObject
{
    private final URI m_plugin;

    public CodeBaseEvent( CodeBaseModel source, URI plugin )
    {
        super( source );

        m_plugin = plugin;
    }
    
    public CodeBaseModel getCodeBaseModel()
    {
        return (CodeBaseModel) getSource();
    }

    public URI getCodeBaseURI()
    {
        return m_plugin;
    }
}
