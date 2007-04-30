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
 * Include datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class IncludeDescriptor extends EntryDescriptor
{
    private final URI m_uri;
    
   /**
    * Creation of an include descriptor.
    * @param element the element defining the included description
    * @param resolver the symbol resolver
    * @exception DecodingException if an error occurs during the decoding phase
    */
    public IncludeDescriptor( Element element, Resolver resolver ) throws DecodingException
    {
        super( element, resolver );
        
        if( null == getKey() )
        {
            final String error = 
              "Missing 'key' attribute in entry descriptor.";
            throw new DecodingException( error, element );
        }
        
        String spec = ElementHelper.getAttribute( element, "uri", null, resolver );
        if( null == spec )
        {
            final String error = 
              "Missing 'uri' attribute in entry descriptor.";
            throw new DecodingException( error, element );
        }
        
        Element[] params = ElementHelper.getChildren( element, "param" );
        for( int i=0; i<params.length; i++ )
        {
            Element e = params[i];
            String key = ElementHelper.getAttribute( e, "key", null, resolver );
            String value = ElementHelper.getAttribute( e, "value", null, resolver );
            if( i==0 )
            {
                spec = spec + "?" + key + "=" + value;
            }
            else
            {
                spec = spec + "&" + key + "=" + value;
            }
        }
        m_uri = URI.create( spec );
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
            IncludeDescriptor entry = (IncludeDescriptor) other;
            if( !getKey().equals( entry.getKey() ) )
            {
                return false;
            }
            else
            {
                return m_uri.equals( entry.m_uri );
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
        if( null != getKey() )
        {
            hash ^= getKey().hashCode();
        }
        return hash;
    }
}
