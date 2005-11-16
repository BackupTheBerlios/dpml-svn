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

import java.util.Properties;

/**
 * This is the Abstract class for all feature feature descriptors.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Descriptor.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public abstract class Descriptor
    implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private static final String[] EMPTY_SET = new String[0];

    /**
     * The arbitrary set of attributes associated with Component.
     */
    private final Properties m_attributes;

    /**
     * Creation of an abstract descriptor.
     * @param attributes the set of attributes to assign to the descriptor
     */
    protected Descriptor( final Properties attributes )
    {
        m_attributes = attributes;
    }

    /**
     * Return the attribute for specified key.
     *
     * @param key the attribute key to resolve
     * @return the attribute for specified key.
     */
    public String getAttribute( final String key )
    {
        if ( null == m_attributes )
        {
            return null;
        }
        else
        {
            return m_attributes.getProperty( key );
        }
    }

    /**
     * Return the attribute for specified key.
     *
     * @param key the attribute key to resolve
     * @param defaultValue the default value to use if the value is not defined
     * @return the attribute for specified key.
     */
    public String getAttribute( final String key,
                                final String defaultValue )
    {
        if ( null == m_attributes )
        {
            return defaultValue;
        }
        else
        {
            return m_attributes.getProperty( key, defaultValue );
        }
    }

    /**
     * Returns the set of attribute names available under this descriptor.
     *
     * @return an array of the properties names held by the descriptor.
     */
    public String[] getAttributeNames()
    {
        if ( null == m_attributes )
        {
            return EMPTY_SET;
        }
        else
        {
            return (String[]) m_attributes.keySet().toArray( EMPTY_SET );
        }
    }

    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object equivalent
     */
    public boolean equals( Object other )
    {
        if ( other instanceof Descriptor )
        {
            Descriptor descriptor = (Descriptor) other;
            if ( null == m_attributes )
                return null == descriptor.m_attributes;

            return m_attributes.equals( descriptor.m_attributes );
        }
        return false;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        if( m_attributes != null )
        {
            return m_attributes.hashCode();
        }
        else
        {
            return 132482349;
        }
    }

    /**
     * Returns the property set.
     *
     * @return the property set.
     */
    public Properties getProperties()
    {
        return m_attributes;
    }
}
