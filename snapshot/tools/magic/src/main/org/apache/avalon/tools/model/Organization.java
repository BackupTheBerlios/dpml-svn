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

package org.apache.avalon.tools.model;


/**
 * Organization descriptor (not used at this time).
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Organization 
{
    private final String m_key;

    private final String m_name;

    private final int m_inception;

    public Organization( final String key, final String name, final int year )
    {
        m_inception = year;
        m_key = getKey( key );
        m_name = getName( name );
    }

    public String getKey()
    {
        return m_key;
    }

    public String getName()
    {
        return m_name;
    }

    public int getInceptionYear()
    {
        return m_inception;
    }

    private String getKey( final String key )
    {
        if( null == key )
        {
            return "unknown";
        }
        return key;
    }

    private String getName( final String name )
    {
        if( null == name )
        {
            return "unknown";
        }
        return name;
    }
}
