/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.part;

import java.io.Serializable;
import java.net.URI;

/**
 * Part info description.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Info implements Serializable
{
    private final String m_title;
    private final String m_description;
    
    public Info( String title, String description )
    {
        if( null == title )
        {
            m_title = "Untitled";
        }
        else
        {
            m_title = title;
        }
        if( null == description )
        {
            m_description = "";
        }
        else
        {
            m_description = description;
        }
    }
    
   /**
    * Get the part title.
    *
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
    }
    
   /**
    * Get the part description.
    *
    * @return the description
    */
    public String getDescription()
    {
        return m_description;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Info )
        {
            Info info = (Info) other;
            if( !m_title.equals( info.m_title ) )
            {
                return false;
            }
            else
            {
                return m_description.equals( info.m_description );
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = m_title.hashCode();
        hash ^= m_description.hashCode();
        return hash;
    }
}
