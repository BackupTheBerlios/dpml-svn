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

package org.apache.avalon.extension;

/**
 * Enum used in {@link Extension} to indicate the compatability
 * of one extension to another. See {@link Extension} for instances
 * of object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Compatability.java 30977 2004-07-30 08:57:54Z niclas $
 * @see Extension
 */
public final class Compatability
{
    /**
     * A string representaiton of compatability level.
     */
    private final String m_name;

    /**
     * Create a compatability enum with specified name.
     *
     * @param name the name of compatability level
     */
    Compatability( final String name )
    {
        m_name = name;
    }

    /**
     * Return name of compatability level.
     *
     * @return the name of compatability level
     */
    public String toString()
    {
        return m_name;
    }
}
