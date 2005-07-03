/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.transit.tools;

import java.net.URI;

/**
 * Utility class that declares a map beterrn a urn and a plugin uri.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class MapDataType
{
    private String m_urn;
    private URI m_uri;

    public void setUrn( String urn )
    {
        m_urn = urn;
    }

    public String getURN()
    {
        if( null == m_urn )
        {
            throw new IllegalStateException( "urn" );
        }
        return m_urn;
    }

    public void setUri( URI uri )
    {
        m_uri = uri;
    }

    public URI getURI()
    {
        if( null == m_urn )
        {
            throw new IllegalStateException( "uri" );
        }
        return m_uri;
    }

    public String toString()
    {
        return "[map urn:" + m_urn + " uri:" + m_uri + "]";
    }
}

