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

package org.apache.avalon.composition.provider;

import java.io.File;

import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.Type;

/**
 * Defintion of a component deployment context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ComponentContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ComponentContext extends DeploymentContext
{
    /**
     * The standard context entry key for the partition name.
     */
    static final String PARTITION_KEY = ContextDescriptor.PARTITION_KEY;

    /**
     * The standard context entry key for the partition name.
     */
    static final String NAME_KEY = ContextDescriptor.NAME_KEY;

    /**
     * The standard context entry key for the partition name.
     */
    static final String CLASSLOADER_KEY = ContextDescriptor.CLASSLOADER_KEY;

    /**
     * The standard context entry key for the partition name.
     */
    static final String HOME_KEY = ContextDescriptor.HOME_KEY;

    /**
     * The standard context entry key for the partition name.
     */
    static final String TEMP_KEY = ContextDescriptor.TEMP_KEY;

   /**
    * Return the system context.
    *
    * @return the system context
    */
    SystemContext getSystemContext();

   /**
    * Return the working directory for the component.
    *
    * @return the working directory
    */
    File getHomeDirectory();

   /**
    * Return the temporary directory for the component.
    *
    * @return the temporary directory
    */
    File getTempDirectory();

   /**
    * Return the deployment profile.
    *
    * @return the profile
    */
    ComponentProfile getComponentProfile();

   /**
    * Return the component type.
    *
    * @return the type defintion
    */
    Type getType();

   /**
    * Return the component class.
    *
    * @return the class
    */
    Class getDeploymentClass();

   /**
    * Return the classloader for the component.
    *
    * @return the classloader
    */
    ClassLoader getClassLoader();

   /**
    * Return the enclosing containment model.
    * @return the containment model that component is within
    */
    ContainmentModel getContainmentModel();

   /**
    * Add a context entry model to the deployment context.
    * @param model the entry model
    */
    //public void register( EntryModel model );

   /**
    * Get a context entry from the deployment context.
    * @param alias the entry lookup key
    * @return value the corresponding value
    * @exception ContextException if a key corresponding to the supplied alias is unknown
    */
    //Object resolve( String alias ) throws ContextException;

}
