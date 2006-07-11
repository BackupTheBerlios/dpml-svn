/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.info;

import java.io.Serializable;
import java.util.Arrays;

import net.dpml.component.Directive;

import net.dpml.lang.AbstractDirective;

/**
 * Abstract base class for composite entities.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Composite extends AbstractDirective implements Serializable
{
    static final long serialVersionUID = 1L;
    
    private final PartReference[] m_parts;

   /**
    * Creation of a new Composite instance.
    *
    * @param parts an array of part descriptors
    */
    public Composite( final PartReference[] parts )
    {
        if( null == parts )
        {
            m_parts = new PartReference[0];
        }
        else
        {
            m_parts = parts;
        }
    }
    
    /**
     * Returns the parts declared by this component type.
     *
     * @return the part descriptors
     */
    public PartReference[] getPartReferences()
    {
        return m_parts;
    }

    /**
     * Retrieve an identified directive.
     *
     * @param key the directive key
     * @return the directive or null if the directive key is unknown
     */
    public Directive getDirective( final String key )
    {
        for ( int i = 0; i < m_parts.length; i++ )
        {
            PartReference reference = m_parts[i];
            if( reference.getKey().equals( key ) )
            {
                return reference.getDirective();
            }
        }
        return null;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the other object
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        if( !( other instanceof Composite ) )
        {
            return false;
        }
        Composite t = (Composite) other;
        return Arrays.equals( m_parts, t.m_parts );
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= hashArray( m_parts );
        return hash;
    }
}
