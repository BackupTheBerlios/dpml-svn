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

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A Class to help to resolve Entitys for items such as DTDs or Schemas.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 *
 * TODO: update JavaDoc
 */

public class DTDResolver implements EntityResolver
{
    /**
     * The list of DTDs that can be resolved by this class.
     */
    private final DTDInfo[] m_dtdInfos;

    /**
     * The ClassLoader to use when loading resources for DTDs.
     */
    private final ClassLoader m_classLoader;

    /**
     * Construct a resolver using specified DTDInfos where resources are loaded
     * from specified ClassLoader.
     */
    public DTDResolver( final DTDInfo[] dtdInfos, final ClassLoader classLoader )
    {
        m_dtdInfos = dtdInfos;
        m_classLoader = classLoader;
    }

    /**
     * Resolve an entity in the XML file.
     * Called by parser to resolve DTDs.
     */
    public InputSource resolveEntity( final String publicId, final String systemId ) throws
        IOException, SAXException
    {
        for ( int i = 0; i < m_dtdInfos.length; i++ )
        {
            final DTDInfo info = m_dtdInfos[i];

            if ( ( publicId != null && publicId.equals( info.getPublicId() ) )
                 || ( systemId != null && systemId.equals( info.getSystemId() ) ) )
            {
                final ClassLoader classLoader = getClassLoader();
                final InputStream inputStream = classLoader.getResourceAsStream( info.getResource() );
                return new InputSource( inputStream );
            }
        }

        return null;
    }

    /**
     * Return CLassLoader to load resource from.
     * If a ClassLoader is specified in the constructor use that,
     * else use ContextClassLoader unless that is null in which case
     * use the current classes ClassLoader.
     */
    private ClassLoader getClassLoader()
    {
        ClassLoader classLoader = m_classLoader;
        if ( null == classLoader )
        {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if ( null == classLoader )
        {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }
}
