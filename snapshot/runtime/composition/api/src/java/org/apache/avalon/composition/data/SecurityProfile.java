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

package org.apache.avalon.composition.data;

import java.io.Serializable;

import org.apache.avalon.meta.info.PermissionDescriptor;

/**
 * A security profile descriptor. The descriptor declares an immutable
 * set of permissions that are associated under a named profile.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SecurityProfile.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class SecurityProfile implements Serializable
{
    //----------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------

   /**
    * The assigned profile name.
    */
    private final String m_name;

    /**
     * The permission descriptors
     */
    private PermissionDescriptor[] m_permissions;

    //----------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------

   /**
    * Creation of a new security profile using an asupplied name and set of 
    * permissions.
    * @param name the profile name
    * @param permissions the assigned permissions
    */
    public SecurityProfile( final String name, PermissionDescriptor[] permissions )
    {
        if( name == null )
        {
            throw new NullPointerException( "name" );
        }
        if( permissions == null )
        {
            throw new NullPointerException( "permissions" );
        }

        m_name = name;
        m_permissions = permissions;
    }

    //----------------------------------------------------------------------
    // implementation
    //----------------------------------------------------------------------

    /**
     * Return the name of the security profile.
     *
     * @return the profile name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the set of permission directives.
     *
     * @return the permission directives
     */
    public PermissionDescriptor[] getPermissionDescriptors()
    {
        return m_permissions;
    }
}
