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
 * @version $Id: StageDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class StageDescriptor extends Descriptor
{

    /**
     * The stage identifier.
     */
    private final String m_urn;

    /**
     * Constructor a stage descriptor without attributes.
     * @param urn the stage identifier
     * @exception NullPointerException if the classname argument is null
     */
    public StageDescriptor( final String urn )
            throws NullPointerException
    {
        this( urn, null );
    }

    /**
     * Constructor a stage descriptor with attributes.
     * @param urn the stage identifier
     * @param attributes a set of attribute values to associated with the stage
     * @exception NullPointerException if the reference argument is null
     */
    public StageDescriptor( final String urn,
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
     * Return the stage identifier.
     *
     * @return the urn identifier
     */
    public String getKey()
    {
        return m_urn;
    }

    /**
     * Return the hashcode for the instance
     * @return the instance hashcode
     */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash >>>= 17;
        hash ^= m_urn.hashCode();
        return hash;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        if( other instanceof StageDescriptor )
        {
            if( super.equals( other ) )
            {
                return m_urn.equals( ((StageDescriptor)other).m_urn );
            }
        }
        return false;
    }

   /**
    * Return a stringified representation of the instance.
    * @return the string representation
    */
    public String toString()
    {
        return "[stage " + getKey() + "]";
    }
}
