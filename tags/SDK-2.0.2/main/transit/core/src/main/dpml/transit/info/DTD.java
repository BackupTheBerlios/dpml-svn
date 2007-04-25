/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package dpml.transit.info;

import java.net.URI;

/**
 * DTD reference.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DTD
{
    /**
     * Public identifier.
     */
    private final String m_public;

    /**
     * The system identifier.
     */
    private final String m_system;

    /**
     * Local resource.
     */
    private final String m_resource;

    /**
     * Factory uri.
     */
    private final URI m_factory;

    public DTD( final String publicId, final String systemId, final String resource, URI factory )
    {
        m_public = publicId;
        m_system = systemId;
        m_resource = resource;
        m_factory = factory;
    }

    public String getPublicId()
    {
        return m_public;
    }

    public String getSystemId()
    {
        return m_system;
    }

    public String getResource()
    {
        return m_resource;
    }
    
    public URI getFactory()
    {
        return m_factory;
    }
}
