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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;

/**
 * Description of classpath.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PermissionDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class PermissionDirective implements Serializable
{
    /** The Permission object  **/
    private Permission m_permission;
    
    /**
     * Create a PermissionDirective instance.
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
    public PermissionDirective( 
        final String classname,  
        final String name,
        final String action
    )
        throws 
          InstantiationException, 
          IllegalAccessException, 
          ClassNotFoundException,
          ClassCastException,
          InvocationTargetException
    {
        if( classname == null )
        {
            throw new NullPointerException( "classname" );
        }
        ClassLoader trustedClassloader = this.getClass().getClassLoader();
        
        Class clazz = trustedClassloader.loadClass( classname );
        Constructor[] constructors = clazz.getConstructors();
        if( name == null )
        {
            m_permission = (Permission) clazz.newInstance();   
        }
        else if( action == null )
        {
            Constructor cons = getConstructor( constructors, 1 );
            Object[] arg = new Object[] { name };
            m_permission = (Permission) cons.newInstance( arg );
        }
        else
        {
            Constructor cons = getConstructor( constructors, 2 );
            Object[] args = new Object[] { name, action };
            m_permission = (Permission) cons.newInstance( args );
        }
    }

    /**
     * Return the Permission.
     *
     * @return the fileset directives
     */
    public Permission getPermission()
    {
        return m_permission;
    }
    
    private Constructor getConstructor( Constructor[] constructors, int noOfParameters )
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
