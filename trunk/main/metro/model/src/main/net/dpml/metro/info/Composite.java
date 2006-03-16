/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

import net.dpml.component.Directive;

/**
 * Abstract base class for composite entities.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Composite implements Serializable
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
        if( null == other )
        {
            return false;
        }
        if( !( other instanceof Composite ) )
        {
            return false;
        }
        Composite t = (Composite) other;
        if( m_parts.length != t.m_parts.length )
        {
            return false;
        }
        else
        {
            for( int i=0; i<m_parts.length; i++ )
            {
                if( !m_parts[i].equals( t.m_parts[i] ) )
                {
                    return false;
                }
            }
        }
        return true;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        for( int i = 0; i < m_parts.length; i++ )
        {
            hash ^= m_parts[i].hashCode();
            hash = hash - 163611323;
        }
        return hash;
    }

   /**
    * Utility to compare two object for equality.
    * @param a the first object
    * @param b the second object
    * @return true if the objects are equal
    */
    protected boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return ( null == b );
        }
        else
        {
            return a.equals( b );
        }
    }
}
