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

import net.dpml.library.Resource;
import net.dpml.library.ResourceNotFoundException;

import net.dpml.util.PropertyResolver;

/**
 * Simple value filter.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SimpleFilterDirective extends FilterDirective
{
    private final String m_value;
    
   /**
    * Creation of a new anonymous resource directive.
    * @param token the filter token
    * @param value the substitution value
    */
    public SimpleFilterDirective( String token, String value )
    {
        super( token );
        m_value = value;
    }
    
   /**
    * Return the filter value.
    * @param resource the enclosing resource
    * @return the resolved value
    */
    public String getValue( Resource resource ) throws ResourceNotFoundException
    {
        return resource.resolve( m_value );
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof SimpleFilterDirective ) )
        {
            SimpleFilterDirective directive = (SimpleFilterDirective) other;
            return m_value.equals( directive.m_value );
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
        hash ^= super.hashValue( m_value );
        return hash;
    }
}
