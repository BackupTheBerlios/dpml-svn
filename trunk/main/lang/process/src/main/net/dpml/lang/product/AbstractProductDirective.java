/*
 * Copyright 2006 Stephen J. McConnell
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

package net.dpml.lang.process;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.lang.AbstractDirective;

/**
 * The AbstractProductDirective class describes a product instance such as a file or directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractProductDirective extends AbstractDirective
{
    private final String m_name;
    private final String m_description;
    
    public AbstractProductDirective( final String name, final String description )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        m_description = description;
    }
    
   /**
    * Get the product name.
    * @return the product name.
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Get the product description.
    * @return the product description.
    */
    public String getDescription()
    {
        return m_description;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof AbstractProductDirective ) )
        {
            AbstractProductDirective object = (AbstractProductDirective) other;
            if( !m_name.equals( object.m_name ) )
            {
                return false;
            }
            else
            {
                return equals( m_description, object.m_description );
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
        hash ^= hashValue( m_name );
        hash ^= hashValue( m_description );
        return hash;
    }
}
