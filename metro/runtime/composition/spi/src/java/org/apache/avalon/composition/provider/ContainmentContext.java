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

import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.model.ClassLoaderModel;
import org.apache.avalon.composition.model.ModelRepository;
import org.apache.avalon.composition.model.ContainmentModel;

/**
 * Defintion of a working context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContainmentContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ContainmentContext extends DeploymentContext
{

   /**
    * Return the classloader model.
    *
    * @return the type manager assigned to the containment model.
    */
    ClassLoaderModel getClassLoaderModel();

   /**
    * Return the working directory for a container.
    *
    * @return the working directory
    */
    File getHomeDirectory();

   /**
    * Return the temporary directory for a container. 
    *
    * @return the temporary directory
    */
    File getTempDirectory();

   /**
    * Return the containment profile.
    *
    * @return the containment profile
    */
    ContainmentProfile getContainmentProfile();

   /**
    * Return the containment classloader.
    *
    * @return the classloader model
    */
    ClassLoader getClassLoader();

   /**
    * Return the model repository.
    *
    * @return the model repository
    */
    ModelRepository getModelRepository();

   /**
    * Return the parent container model. If the container is a root
    * container, the operation shall return a null value.
    *
    * @return the parent containment model
    */
    ContainmentModel getParentContainmentModel();

}
