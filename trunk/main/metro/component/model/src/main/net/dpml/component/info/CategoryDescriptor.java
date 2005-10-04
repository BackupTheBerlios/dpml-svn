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

package net.dpml.component.info;;

import java.util.Properties;

/**
 * A category descriptor describes a logging channel that 
 * a component type uses.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class CategoryDescriptor
    extends Descriptor
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    public static final String SEPERATOR = ".";

    private final String m_name;

    /**
     * Create a descriptor for logging category.
     *
     * @param name the logging category name
     * @param attributes a set of attributes associated with the declaration
     *
     * @exception NullPointerException if name argument is null
     */
    public CategoryDescriptor( final String name, final Properties attributes )
        throws NullPointerException
    {
        super( attributes );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }

        m_name = name;
    }

    /**
     * Return the name of logging category.
     *
     * @return the category name.
     */
    public String getName()
    {
        return m_name;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        boolean isEqual = other instanceof CategoryDescriptor;
        if ( isEqual )
        {
            isEqual = isEqual && m_name.equals( ((CategoryDescriptor)other).m_name );
        }
        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_name.hashCode();
        return hash;
    }
}
