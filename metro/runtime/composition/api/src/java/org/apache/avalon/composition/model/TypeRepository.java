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

package org.apache.avalon.composition.model;

import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.model.ProfileUnknownException;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.Type;

/**
 * A type manager implemetation provides support for the creation,
 * storage and retrival of component types.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeRepository.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface TypeRepository
{
    /**
     * Return all availble types.
     * @return the array of types
     */
    Type[] getTypes();

    /**
     * Return all the types available within the repository.
     * @param policy if TRUE, return all available types, if FALSE
     *   return only the locally established types.
     * @return the array of types
     */
    Type[] getTypes( boolean policy );

    /**
     * Locate a {@link Type} instances associated with the
     * supplied implementation classname.
     * @param clazz the component type implementation class.
     * @return the type matching the supplied implementation classname.
     * @exception UnknownTypeException if a matching type cannot be found
     */
    Type getType( Class clazz ) throws TypeUnknownException;

    /**
     * Locate a {@link Type} instances associated with the
     * supplied implementation classname.
     * @param classname the component type implementation class name.
     * @return the type matching the supplied implementation classname.
     * @exception UnknownTypeException if a matching type cannot be found
     */
    Type getType( String classname ) throws TypeUnknownException;

    /**
     * Locate the set of component types capable of services the supplied
     * dependency.
     * @param dependency a service dependency descriptor
     * @return a set of types capable of servicing the supplied dependency
     */
    Type[] getTypes( DependencyDescriptor dependency );

   /**
    * Locate the set of component types capable of services the supplied
    * dependency.
    * @param dependency a service dependency descriptor
    * @param search if TRUE then search for solution using the parent repository
    * @return a set of types capable of servicing the supplied dependency
    */
    Type[] getTypes( DependencyDescriptor dependency, boolean search );

   /**
    * Locate the set of component types capable of services the supplied
    * reference.
    * @param reference a service reference descriptor
    * @return a set of types capable of servicing the supplied service reference
    */
    Type[] getTypes( ReferenceDescriptor reference );

    /**
     * Locate the set of component types capable of services the supplied
     * reference.
     * @param reference a service reference descriptor
     * @return a set of types capable of servicing the supplied service reference
     */
    Type[] getTypes( ReferenceDescriptor reference, boolean search );

    /**
     * Locate the set of component types that provide the supplied extension.
     * @param stage a stage descriptor
     * @return a set of types that support the supplied stage
     */
    Type[] getTypes( StageDescriptor stage );

   /**
    * Return the set of deployment profiles for the supplied type. An 
    * implementation is required to return a array of types > 0 in length
    * or throw a TypeUnknownException.
    * @param type the type
    * @return a profile array containing at least one deployment profile
    * @exception TypeUnknownException if the supplied type is unknown
    */
    ComponentProfile[] getProfiles( Type type ) throws TypeUnknownException;

   /**
    * Return a deployment profile for the supplied type and key.
    * @param type the type
    * @param key the profile name
    * @return a profile matching the supplied key
    * @exception TypeUnknownException if the supplied type is unknown
    * @exception ProfileUnknownException if the supplied key is unknown
    */
    ComponentProfile getProfile( Type type, String key ) 
      throws TypeUnknownException, ProfileUnknownException;

   /**
    * Attempt to locate a packaged deployment profile meeting the 
    * supplied dependency description.
    *
    * @param dependency the dependency description 
    * @param search include profiles from parent repository in selection
    * @return the deployment profile (possibly null) 
    */
    DeploymentProfile getProfile( 
      DependencyDescriptor dependency, boolean search );

   /**
    * Return a set of local deployment profile for the supplied dependency.
    * @param dependency the dependency descriptor
    * @param search include profiles from parent repository in selection
    * @return a set of profiles matching the supplied dependency
    */
    DeploymentProfile[] getProfiles( DependencyDescriptor dependency, boolean search );


}
