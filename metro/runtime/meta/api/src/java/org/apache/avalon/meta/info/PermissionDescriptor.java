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

import java.io.Serializable;

/**
 * A descriptor that describes a value that must be placed
 * in components Context. It contains information about;
 * <ul>
 *   <li>key: the key that component uses to look up entry</li>
 *   <li>classname: the class/interface of the entry</li>
 *   <li>isOptional: true if entry is optional rather than required</li>
 * </ul>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PermissionDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class PermissionDescriptor
        implements Serializable
{

    private static final String[] EMPTY_ACTIONS = 
      new String[0];

    /**
     * The permission classname.
     */
    private final String m_classname;

    /**
     * The permission name (saantics relative to the permission classname).
     */
    private final String m_name;

    /**
     * Permission actions.
     */
    private final String[] m_actions;

    /**
     * Construct a new PermissionDescriptor
     * @param classname the permission class name
     * @param name the permission name
     * @param actions the permission actions
     */
    public PermissionDescriptor( 
      final String classname, final String name, final String[] actions )
    {
        if ( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        m_classname = classname;
        m_name = name;
        if( null == actions )
        {
           m_actions = EMPTY_ACTIONS;
        }
        else
        {
            m_actions = actions;
        }
    }

    /**
     * Return the classname of the permission.
     * 
     * @return the classname
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the permission name.  The value returned is relative to
     * the permission class.  If no permission name is declared a null 
     * value will be returned.
     *
     * @return the name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the set of actions associated with the permission.
     *
     * @return a string array representing the actions assigned to 
     *   this permission
     */
    public String[] getActions()
    {
        return m_actions;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        boolean isEqual = other instanceof PermissionDescriptor;

        if ( isEqual )
        {
            PermissionDescriptor permission = (PermissionDescriptor) other;
            isEqual = isEqual && m_classname.equals( permission.m_classname );
            if ( null == m_name )
            {
                isEqual = isEqual && null == permission.m_name;
            }
            else
            {
                isEqual = isEqual && m_name.equals( permission.m_name );
            }

            isEqual = isEqual && m_actions.length ==  permission.m_actions.length;
            if( isEqual )
            {
                for( int i=0; i<m_actions.length; i++ )
                {
                    String action = m_actions[i];
                    if( !action.equals( permission.m_actions[i] ) ) return false;
                }        
            }
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
        hash >>>= 13;
        hash ^= m_classname.hashCode();
        hash >>>= 13;
        hash ^= ( null != m_name ) ? m_name.hashCode() : 0;
        hash >>>= 13;
        for( int i=0; i<m_actions.length; i++ )
        {
            hash ^= m_actions[i].hashCode();
            hash >>>= 13;
        }
        return hash;
    }

    public String toString()
    {
        String list = "";
        for( int i=0; i<m_actions.length; i++ )
        {
            list = list + m_actions[i];
            if( i < (m_actions.length - 1 ))
            {
                list = list + ", ";
            }
        } 
        return "[permission class=" 
          + getClassname()
          + " name=" + getName()
          + " actions=" + list
          + "]";
    }
}
