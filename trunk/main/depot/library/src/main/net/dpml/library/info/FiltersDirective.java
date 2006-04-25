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

import java.util.Arrays;

/**
 * Datatype describing a collection of filters.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FiltersDirective extends DataDirective
{
    private final FilterDirective[] m_filters;
    
   /**
    * Creation of a filter collection.
    * @param filters the array of filters
    */
    public FiltersDirective( FilterDirective[] filters )
    {
        super( "filters" );
        
        m_filters = filters;
    }
    
   /**
    * Return the filter array.
    * @return the filters
    */
    public FilterDirective[] getFilterDirectives()
    {
        return m_filters;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof FiltersDirective ) )
        {
            FiltersDirective directive = (FiltersDirective) other;
            return Arrays.equals( m_filters, directive.m_filters );
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
        hash ^= super.hashArray( m_filters );
        return hash;
    }
}
