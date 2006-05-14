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

import net.dpml.lang.AbstractDirective;

import org.w3c.dom.Element;

/**
 * Base class for a data directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DataDirective extends AbstractDirective
{
    private final Element m_element;
    
   /**
    * Creation of a new data directive using a DOM element as the 
    * datastructure definition.
    * @param element the DOM element
    */
    public DataDirective( Element element )
    {
        if( null == element )
        {
            throw new NullPointerException( "element" );
        }
        m_element = element;
    }
    
   /**
    * Return the datatype element.
    * @return the DOM element
    */
    public String getElement()
    {
        return m_element;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof DataDirective ) )
        {
            DataDirective object = (DataDirective) other;
            return m_element.equals( object.m_element );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hascode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_element.hashCode();
        return hash;
    }
}
