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
import java.net.URL;

import org.apache.avalon.composition.data.ClassLoaderDirective;
import org.apache.avalon.composition.model.TypeRepository;
import org.apache.avalon.composition.model.ServiceRepository;

import org.apache.avalon.extension.manager.OptionalPackage;
import org.apache.avalon.extension.manager.ExtensionManager;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.repository.Repository;

/**
 * Defintion of a working context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ClassLoaderContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ClassLoaderContext extends Context
{
   /**
    * Return the logging channel to be applied to the 
    * classloader model.
    *
    * @return the system logging channel
    */
    Logger getLogger();

   /**
    * Return the local repository.
    *
    * @return the repository
    */
    Repository getRepository();

   /**
    * Return the base directory from which relative library directives
    * and fileset directory paths may be resolved.
    *
    * @return the base directory
    */
    File getBaseDirectory();

   /**
    * Return the classloader to be assigned as the parent classloader
    * of the classloader created by the model.
    *
    * @return the classloader
    */
    ClassLoader getClassLoader();

   /**
    * Return the optional packages already establised relative to 
    * the parent classloader.
    *
    * @return the array of established optional packages
    */
    OptionalPackage[] getOptionalPackages();

   /**
    * Return the extension manager established by the parent 
    * classloader model.
    *
    * @return the extension manager
    */
    ExtensionManager getExtensionManager();

   /**
    * Return the classloader directive to be applied to the 
    * classloader model.
    *
    * @return the classloader directive
    */
    ClassLoaderDirective getClassLoaderDirective();

   /**
    * Return the type repository established by the parent classloader.
    *
    * @return the type repository
    */
    TypeRepository getTypeRepository();

   /**
    * Return the service repository established by the parent classloader.
    *
    * @return the service repository
    */
    ServiceRepository getServiceRepository();

   /**
    * Return any implied urls to include in the classloader.
    *
    * @return the implied urls
    */
    URL[] getImplicitURLs();

   /**
    * Return the system context.
    *
    * @return the system context
    */
    SystemContext getSystemContext();
}
