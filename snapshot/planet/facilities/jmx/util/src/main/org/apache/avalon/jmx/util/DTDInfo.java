/*
 * Copyright 2004 Apache Software Foundation
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
package org.apache.avalon.jmx.util;

/**
 * Holds information about a given DTD.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
class DTDInfo
{
    /**
     * The public identifier. Null if unknown.
     */
    private final String m_publicId;

    /**
     * The system identifier.  Null if unknown.
     */
    private final String m_systemId;

    /**
     * The resource name, if a copy of the document is available.
     */
    private final String m_resource;

    /**
     * Constructs a new DTDInfo.
     * @param publicId The public identifier
     * @param systemId The system identifier
     * @param resource The resource name, if a copy of the document is available
     */
    public DTDInfo( final String publicId, final String systemId, final String resource )
    {
        m_publicId = publicId;
        m_systemId = systemId;
        m_resource = resource;
    }

    /**
     * Retrieves the public identifier.
     * @return the public identifier
     */
    public String getPublicId()
    {
        return m_publicId;
    }

    /**
     * Retrieves the system identifier.
     * @return the system identifier
     */
    public String getSystemId()
    {
        return m_systemId;
    }

    /**
     * Retrieves the resource name.
     * @return the resource name
     */
    public String getResource()
    {
        return m_resource;
    }
}
