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

import java.util.Properties;

/**
 * A category descriptor describes a logging channel that 
 * a component type uses.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoryDescriptor
    extends Descriptor
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Constant category separator.
    */
    public static final String SEPERATOR = ".";

   /**
    * Default priority value.
    */
    public static final Priority PRIORITY = Priority.DEBUG;


    private final String m_name;
    
    private final Priority m_priority;

    /**
     * Create a descriptor for logging category.
     *
     * @param name the logging category name
     * @param priority the default priority value
     * @param attributes a set of attributes associated with the declaration
     *
     * @exception NullPointerException if name argument is null
     */
    public CategoryDescriptor( final String name, final Priority priority, final Properties attributes )
        throws NullPointerException
    {
        super( attributes );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        if( null == priority )
        {
            m_priority = PRIORITY;
        }
        else
        {
            m_priority = priority;
        }
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
     * Return the default logging priority.
     *
     * @return the default priority.
     */
    public Priority getDefaultPriority()
    {
        return m_priority;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the other object
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof CategoryDescriptor ) )
        {
            CategoryDescriptor descriptor = (CategoryDescriptor) other;
            if( !equals( m_name, descriptor.m_name ) )
            {
                return false;
            }
            else
            {
                return equals( m_priority, descriptor.m_priority );
            }
        }
        else
        {
            return false;
        }
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_name.hashCode();
        hash ^= m_priority.hashCode();
        return hash;
    }
}
