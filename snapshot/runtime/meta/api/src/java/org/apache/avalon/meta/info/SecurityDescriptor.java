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
 * This class is used to provide security information to assembler
 * and administrator about the component type. 
 *
 * <p>The SecurityDescriptor also includes an arbitrary set
 * of attributes about component. Usually these are container
 * specific attributes that can store arbitrary information.
 * The attributes should be stored with keys based on package
 * name of container.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SecurityDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class SecurityDescriptor extends Descriptor
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final PermissionDescriptor[] EMPTY_PERMISSIONS =
      new PermissionDescriptor[0];

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    /**
     * The set of permissions that this component type requires in order 
     * to execute.
     */
    private final PermissionDescriptor[] m_permissions;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    /**
     * Creation of a new info descriptor using a supplied name, key, version
     * and attribute set.
     *
     * @param permissions the set of permissions
     * @param attributes supplimentary security attributes
     * @since 1.4
     */
    public SecurityDescriptor( 
      final PermissionDescriptor[] permissions,
      final Properties attributes )
    {
        super( attributes );

        if ( null == permissions )
        {
            m_permissions = EMPTY_PERMISSIONS;
        }
        else
        {
            m_permissions = permissions;
        }
    }

    /**
     * Return the set of permissions requested by the component type.
     *
     * @return the permissions
     */
    public PermissionDescriptor[] getPermissions()
    {
        return m_permissions;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        boolean isEqual = super.equals(other) && other instanceof SecurityDescriptor;

        if (isEqual)
        {
            SecurityDescriptor security = (SecurityDescriptor) other;
            PermissionDescriptor[] permissions = security.getPermissions();
            if( m_permissions.length != permissions.length ) return false;
            for( int i=0; i<permissions.length; i++ )
            {
                if( !permissions[i].equals( m_permissions[i] ) )return false;
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

        hash >>>= 7;
        for( int i=0; i<m_permissions.length; i++ )
        {
            hash ^= m_permissions[i].hashCode();
            hash >>>= 13;
        }

        return hash;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "[security " );
        for( int i=0; i<m_permissions.length; i++ )
        {
            buffer.append( m_permissions[i].toString() );
        }
        buffer.append( "]" );
        return buffer.toString();
    }
}
