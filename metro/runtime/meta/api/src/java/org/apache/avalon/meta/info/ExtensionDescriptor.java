/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.meta.info;

import java.util.Properties;

/**
 * A descriptor that describes a name and inteface of a lifecycle stage.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExtensionDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ExtensionDescriptor extends Descriptor
{
    /**
     * The extension identifier.
     */
    private final String m_urn;

    /**
     * Creation of an extension descriptor without attributes.
     * @param urn the extension identifier
     * @exception NullPointerException if the urn identifer is null
     */
    public ExtensionDescriptor( final String urn )
            throws NullPointerException
    {
        this( urn, null );
    }

    /**
     * Creation of a extension descriptor with attributes.
     * @param urn the extension identifier
     * @param attributes a set of attributes to associate with the extension
     * @exception NullPointerException if the supplied urn is null
     */
    public ExtensionDescriptor( final String urn,
                                final Properties attributes )
            throws NullPointerException
    {
        super( attributes );

        if ( null == urn )
        {
            throw new NullPointerException( "urn" );
        }

        m_urn = urn;
    }

    /**
     * Return the interface reference
     *
     * @return the reference.
     */
    public String getKey()
    {
        return m_urn;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        if( other instanceof ExtensionDescriptor )
        {
            if( super.equals( other ) )
            {
                return m_urn.equals( ((ExtensionDescriptor)other).m_urn );
            }
        }
        return false;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash >>>= 17;
        hash ^= m_urn.hashCode();
        return hash;
    }

   /**
    * Return a stringified representation of the instance.
    * @return the string representation
    */
    public String toString()
    {
        return "[extension " + getKey() + "]";
    }

}
