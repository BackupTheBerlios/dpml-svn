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

import java.net.URI;

import net.dpml.lang.DecodingException;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Immutable datastructure used to describe an deployment scenario.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class EntryDescriptor
{
    private final Element m_element;
    private final String m_key;
    private final URI m_uri;
    
    public EntryDescriptor( Element element, Resolver resolver ) throws DecodingException
    {
        m_element = element;
        m_key = ElementHelper.getAttribute( element, "key", null, resolver );
        if( null == m_key )
        {
            final String error = 
              "Missing 'key' attribute in entry descriptor.";
            throw new DecodingException( error, element );
        }
        String spec = ElementHelper.getAttribute( element, "uri", null, resolver );
        m_uri = URI.create( spec );
        if( null == m_uri )
        {
            final String error = 
              "Missing 'uri' attribute in entry descriptor.";
            throw new DecodingException( error, element );
        }
    }
    
   /**
    * Return the element defining the entry descriptor.
    * @return the defining element
    */
    public Element getElement()
    {
        return m_element;
    }
    
   /**
    * Get the uri.
    * 
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }
    
   /**
    * Get the entry key.
    * 
    * @return the key
    */
    public String getKey()
    {
        return m_key;
    }
    
    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object is equivalent
     */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( getClass() != other.getClass() )
        {
            return false;
        }
        else
        {
            EntryDescriptor entry = (EntryDescriptor) other;
            if( !m_key.equals( entry.m_key ) )
            {
                return false;
            }
            else if( !m_uri.equals( entry.m_uri ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    
   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= m_uri.hashCode();
        hash ^= m_key.hashCode();
        return hash;
    }
}
