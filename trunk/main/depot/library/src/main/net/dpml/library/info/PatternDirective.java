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

/**
 * Abstract base class for includes and excludes used within collections.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class PatternDirective extends AbstractDirective
{
    private final String m_name;
    
   /**
    * Creation of a new include directive.
    * @param mode the include mode
    * @param category the runtime category
    * @param value the value (key or reference address depending on mode)
    * @param properties supplimentary properties
    */
    public PatternDirective( String name )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
    }
    
   /**
    * Return the include value.
    * @return the value
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof PatternDirective ) )
        {
            PatternDirective include = (PatternDirective) other;
            return equals( m_name, include.m_name );
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
        hash ^= super.hashValue( m_name );
        return hash;
    }
   
   /**
    * Return a string representation of the include.
    * @return the string value
    */
    public String toString()
    {
        return "include:" + m_name;
    }
}
