/*
 * Copyright 2007 Stephen J. McConnell.
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

package dpml.station.info;

import dpml.util.ElementHelper;
import dpml.util.ObjectUtils;

import net.dpml.util.Resolver;
import net.dpml.lang.DecodingException;

import org.w3c.dom.Element;

/**
 * Appliance info description.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class InfoDescriptor
{
    private final Element m_element;
    private final String m_title;
    private final String m_description;
    private final String m_name;
    
   /**
    * Creation of a new application info descriptor.
    * @param element the DOM element definining the info descriptor
    * @param resolver symbolic reference resolver
    * @exception DecodingException if a decoding exception occurs
    */
    public InfoDescriptor( Element element, Resolver resolver ) throws DecodingException
    {
        m_element = element;
        if( null == element )
        {
            m_name = null;
            m_description = null;
            m_title = null;
        }
        else
        {
            m_name = ElementHelper.getAttribute( element, "name", null, resolver );
            m_title = ElementHelper.getAttribute( element, "title", null, resolver );
            Element elem = ElementHelper.getChild( element, "description" );
            if( null != elem )
            {
                m_description = ElementHelper.getValue( elem );
            }
            else
            {
                m_description = null;
            }
        }
    }
    
   /**
    * Creation of a new info descriptor.
    * @param name the name
    * @param title a short title
    * @param description a description
    */
    public InfoDescriptor( String name, String title, String description )
    {
        m_name = name;
        m_title = title;
        m_description = description;
        m_element = null;
    }
    
   /**
    * Return the element defining the scenario descriptor.
    * @return the defining element
    */
    public Element getElement()
    {
        return m_element;
    }
    
   /**
    * Get the application name.
    *
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Get the application title.
    *
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
    }
    
   /**
    * Get the application description.
    *
    * @return the description
    */
    public String getDescription()
    {
        return m_description;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        if( getClass() != other.getClass() )
        {
            return false;
        }
        InfoDescriptor info = (InfoDescriptor) other;
        if( !ObjectUtils.equals( m_name, info.m_name ) )
        {
            return false;
        }
        else if( !ObjectUtils.equals( m_title, info.m_title ) )
        {
            return false;
        }
        else
        {
            return ObjectUtils.equals( m_description, info.m_description );
        }
    }
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= ObjectUtils.hashValue( m_name );
        hash ^= ObjectUtils.hashValue( m_title );
        hash ^= ObjectUtils.hashValue( m_description );
        return hash;
    }
}
