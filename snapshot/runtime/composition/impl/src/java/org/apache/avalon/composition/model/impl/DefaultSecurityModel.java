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

package org.apache.avalon.composition.model.impl;

import java.security.Permission;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permissions;

import org.apache.avalon.composition.data.SecurityProfile;
import org.apache.avalon.composition.provider.SecurityModel;
import org.apache.avalon.composition.provider.SystemRuntimeException;

import org.apache.avalon.meta.info.PermissionDescriptor;

/**
 * <p>Implementation of the default security model.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultSecurityModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class DefaultSecurityModel implements SecurityModel
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Permission[] EMPTY_PERMISSIONS = new Permission[0];

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final Permissions m_permissions;
    private final String m_name;
 
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Creation of a disabled security model.
    */
    public DefaultSecurityModel()
    {
        m_permissions = new Permissions();
        m_name = "default";
    }

   /**
    * Creation of a new security model.
    * 
    * @param profile the security profile
    */
    public DefaultSecurityModel( SecurityProfile profile )
    {
        m_name = profile.getName();
        PermissionDescriptor[] permissions = 
          profile.getPermissionDescriptors();
        m_permissions = buildPermissions( permissions );
    }

    //-------------------------------------------------------------------
    // SecurityModel
    //-------------------------------------------------------------------

   /**
    * Return the name of the security profile backing the model.
    * @return the profile name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the set of default permissions.
    * 
    * @return the permissions
    */
    public Permissions getPermissions()
    {
        return m_permissions;
    }

    //-------------------------------------------------------------------
    // internals
    //-------------------------------------------------------------------

    private Permissions buildPermissions( 
      PermissionDescriptor[] descriptors ) throws SystemRuntimeException
    {
        Permissions permissions = new Permissions();
        for( int i=0; i<descriptors.length; i++ )
        {
            PermissionDescriptor descriptor = descriptors[i];
            try
            {
                Permission permission = createPermission( descriptors[i] );
                permissions.add( permission );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Invalid permission descriptor " + descriptor + ".";
                throw new SystemRuntimeException( error, e );
            }
        }
        return permissions;
    }

    private Permission createPermission( 
      PermissionDescriptor descriptor ) throws Exception
    {
        String classname = descriptor.getClassname();
        String name = descriptor.getName();
        String actions = getActions( descriptor );
        return createPermission( classname, name, actions );
    }

    private String getActions( PermissionDescriptor descriptor )
    {
        String[] actions = descriptor.getActions();
        if( actions.length == 0 ) return null;
        String result = "";
        for( int i=0 ; i < actions.length ; i ++ )
        {
            if( i > 0 )
            {
                result = result + "," + actions[i];
            }
            else
            {
                result = result + actions[i];
            }
        }
        return result;
    }

    /**
     * Utility method to create a Permission instance.
     *
     * @param classname Permission class
     * @param name The name associated with the permission.
     * @param action The action associated with the permission. Note that some
     *        Permissions doesn't support actions.
     * @throws InstantiationException if the class could not be instantiated.
     * @throws IllegalAccessException, if the class does not have a 
     *         public constructor
     * @throws ClassNotFoundException, if the class could not be reached by the
     *         classloader.
     * @throws ClassCastException, if the class is not a subclass of 
     *         java.security.Permission
     * @throws InvocationTargetException, if the constructor in the Permission
     *         class throws an exception.
     */
    private static Permission createPermission( 
      String classname, String name, String action )
      throws InstantiationException, IllegalAccessException, ClassNotFoundException,
          ClassCastException, InvocationTargetException
    {
        if( classname == null )
        {
            throw new NullPointerException( "classname" );
        }

        ClassLoader trustedClassloader = DefaultSecurityModel.class.getClassLoader();
        
        Class clazz = trustedClassloader.loadClass( classname );
        Constructor[] constructors = clazz.getConstructors();
        if( name == null )
        {
            return (Permission) clazz.newInstance();   
        }
        else if( action == null )
        {
            Constructor cons = getConstructor( constructors, 1 );
            Object[] arg = new Object[] { name };
            return (Permission) cons.newInstance( arg );
        }
        else
        {
            Constructor cons = getConstructor( constructors, 2 );
            Object[] args = new Object[] { name, action };
            return (Permission) cons.newInstance( args );
        }
    }

    private static Constructor getConstructor( 
      Constructor[] constructors, int noOfParameters )
    {
        for ( int i=0 ; i < constructors.length ; i++ )
        {
            Class[] params = constructors[i].getParameterTypes();
            if( params.length == noOfParameters )
                return constructors[i];
        }
        return null;
    }

}
