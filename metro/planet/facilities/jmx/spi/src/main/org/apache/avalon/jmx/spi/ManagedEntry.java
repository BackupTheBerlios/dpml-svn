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
package org.apache.avalon.jmx.spi;

/** A class to hold the management information for a managed component.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
final class ManagedEntry
{
    ///Object passed in for management
    private final Object m_object;

    ///Interfaces object wants to be managed through (can be null)
    private final Class[] m_interfaces;

    ///Object representation when exported (usually a proxy)
    private Object m_exportedObject;

    ManagedEntry( final Object object, final Class[] interfaces, final Object exportedObject )
    {
        m_object = object;
        m_interfaces = interfaces;
        m_exportedObject = exportedObject;
    }

    /** Returns the managed object.
     */
    Object getObject()
    {
        return m_object;
    }

    Class[] getInterfaces()
    {
        return m_interfaces;
    }

    Object getExportedObject()
    {
        return m_exportedObject;
    }
}
