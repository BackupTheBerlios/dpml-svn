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

import net.dpml.component.Directive;

/**
 * A <code>PartReference</code> is a serializable object that contains a key and 
 * an associated part.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartReference implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * The key.
     */
    private final String m_key;

    /**
     * The supplied argument.
     */
    private final Directive m_directive;

    /**
     * Creation of a new part reference.
     *
     * @param key the key identifying this part within the scope of its container
     * @param directive the directive
     */
    public PartReference( final String key, Directive directive )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        m_key = key;
        m_directive = directive;
    }

    /**
     * Return the key.
     * @return the key
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Return the directive.
     * @return the directive
     */
    public Directive getDirective()
    {
        return m_directive;
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            if( !( other instanceof PartReference ) )
            {
                return false;
            }
            else
            {
                PartReference reference = (PartReference) other;
                if( !m_key.equals( reference.getKey() ) )
                {
                    return false;
                }
                else
                {
                    return m_directive.equals( reference.m_directive );
                }
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = m_key.hashCode();
        hash ^= m_directive.hashCode();
        return hash;
    }
    
   /**
    * Return a string representation of the instance.
    * @return the string representation
    */
    public String toString()
    {
        return "[reference: key=" + m_key + "directive=" + m_directive + "]";
    }
}
