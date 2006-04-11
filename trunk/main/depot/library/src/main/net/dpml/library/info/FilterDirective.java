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

import net.dpml.library.Filter;
import net.dpml.library.Resource;
import net.dpml.library.ResourceNotFoundException;

/**
 * Base class for filter directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class FilterDirective extends AbstractDirective implements Filter
{
    private final String m_token;
    
   /**
    * Creation of a new anonymous resource directive.
    * @param token the filter token
    */
    public FilterDirective( String token )
    {
        m_token = token;
    }
    
   /**
    * Return the filter token.
    * @return the token
    */
    public String getToken()
    {
        return m_token;
    }
    
   /**
    * Return the filter value.
    * @param resource the enclosing resource
    * @return the resolved value
    * @exception ResourceNotFoundException if the feature references a 
    *  resource that is unknown
    */
    public abstract String getValue( Resource resource ) throws ResourceNotFoundException;
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof FilterDirective ) )
        {
            FilterDirective directive = (FilterDirective) other;
            return m_token.equals( directive.m_token );
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
        hash ^= super.hashValue( m_token );
        return hash;
    }
}
