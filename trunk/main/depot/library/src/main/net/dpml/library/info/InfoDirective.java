/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.library.info;

import java.util.Properties;

import net.dpml.lang.AbstractDirective;

import net.dpml.library.Info;

/**
 * Info block descriptor.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class InfoDirective extends AbstractDirective implements Info
{
    private final String m_title;
    private final String m_description;
    
   /**
    * Creation of a new info directive.
    * @param title the title
    * @param description the description
    */
    public InfoDirective( final String title, final String description )
    {
        m_title = title;
        m_description = description;
    }
    
   /**
    * Return the resource title.
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
    }
    
    
   /**
    * Return the resource description.
    * @return the description
    */
    public String getDescription()
    {
        return m_description;
    }
    
   /**
    * Return the null status of the info block.
    * @return the null status
    */
    public boolean isNull()
    {
        return ( ( null == m_title ) && ( null == m_description ) );
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof InfoDirective ) )
        {
            InfoDirective info = (InfoDirective) other;
            if( !equals( m_title, info.m_title ) )
            {
                return false;
            }
            else
            {
                return equals( m_description, info.m_description );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_title );
        hash ^= super.hashValue( m_description );
        return hash;
    }
}
