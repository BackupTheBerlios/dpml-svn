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

import org.apache.avalon.composition.model.DeploymentModel;

import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.repository.Repository;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;

/**
 * Defintion of a system context that exposes a system wide set of parameters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SystemContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface SystemContext extends Context
{
   /**
    * Return the model factory.
    *
    * @return the factory
    */
    ModelFactory getModelFactory();

   /**
    * Return the base directory from which relative references 
    * should be resolved.
    *
    * @return the base directory
    */
    File getBaseDirectory();

   /**
    * Return the home directory from which containers may establish
    * persistent content.
    *
    * @return the working directory
    */
    File getHomeDirectory();

   /**
    * Return the temp directory from which containers may establish
    * non-persistent content.
    *
    * @return the temp directory
    */
    File getTempDirectory();

   /**
    * Return the anchor directory from which a container 
    * may use to resolve relative classpath references.
    *
    * @return the anchor directory
    */
    File getAnchorDirectory();
    
   /**
    * Return the application repository from which resource 
    * directives can be resolved.
    *
    * @return the repository
    */
    Repository getRepository();

   /**
    * Return the system trace flag.
    *
    * @return the trace flag
    */
    boolean isTraceEnabled();

   /**
    * Return the system proxy enablement flag.
    *
    * @return the proxy flag
    */
    boolean isProxyEnabled();

   /**
    * Return the SPI classloader.
    *
    * @return the SPI classloader
    */
    ClassLoader getSPIClassLoader();

   /**
    * Return the API classloader.
    *
    * @return the API classloader
    */
    ClassLoader getAPIClassLoader();

   /**
    * Return the logging manager.
    *
    * @return the logging manager.
    */
    LoggingManager getLoggingManager();

   /**
    * Return the system logging channel.
    *
    * @return the system logging channel
    */
    Logger getLogger();

   /**
    * Return the default deployment phase timeout value.
    * @return the timeout value
    */
    long getDefaultDeploymentTimeout();

   /**
    * Return the enabled status of the code security policy.
    * @return the code security enabled status
    */
    boolean isCodeSecurityEnabled();

    //------------------------------------------------------------------
    // runtime operations
    //------------------------------------------------------------------

   /**
    * Request the commissioning of a runtime for a supplied deployment 
    * model.
    * @param model the deployment model 
    * @exception Exception of a commissioning error occurs
    */
    void commission( DeploymentModel model ) throws Exception;

   /**
    * Request the decommissioning of a runtime for a supplied deployment 
    * model.
    * @param model the deployment model 
    * @exception Exception of a commissioning error occurs
    */
    void decommission( DeploymentModel model );

   /**
    * Request resolution of an object from the runtime.
    * @param model the deployment model
    * @exception Exception if a deployment error occurs
    */
    Object resolve( DeploymentModel model ) throws Exception;

   /**
    * Request resolution of an object from the runtime.
    * @param model the deployment model
    * @param proxy if TRUE the return value will be proxied if the 
    *   underlying component typoe suppports proxy representation 
    * @exception Exception if a deployment error occurs
    */
    Object resolve( DeploymentModel model, boolean proxy ) throws Exception;

   /**
    * Request the release of an object from the runtime.
    * @param model the deployment model
    * @param instance the object to release
    * @exception Exception if a deployment error occurs
    */
    void release( DeploymentModel model, Object instance );

   /**
    * Prepare a string representation of an object for presentation.
    * @param object the object to parse
    * @return the presentation string
    */
    String toString( Object object );

   /**
    * Prepare a string representation of an object array for presentation.
    * @param objects the array of objects
    * @return the presentation string
    */
    String toString( Object[] objects );

}
