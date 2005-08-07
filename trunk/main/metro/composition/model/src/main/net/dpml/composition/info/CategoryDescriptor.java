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

package net.dpml.composition.info;

import java.util.Properties;

/**
 * A descriptor describing an Avalon Logger
 * child instances that the component will create using the
 * <code>net.dpml.logging.Logger#getChildLogger</code>
 * method. The name of each category is relative to the component.  For
 * example, a component with an internal logging category named "data"
 * would aquire a logger for that category using the
 * <code>m_logger.getChildLogger( "data" );</code>. The establishment
 * of logging channels and targets for the returned channel is container
 * concern facilities by type-level category declarations.
 *
 * <p>Also associated with each Logger is a set of arbitrary
 * attributes that can be used to store extra information
 * about Logger requirements.</p>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: CategoryDescriptor.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
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
