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

import org.apache.avalon.composition.model.DependencyGraph;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.data.DeploymentProfile;

/**
 * Deployment context that is supplied to a deployment model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DeploymentContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface DeploymentContext extends Context 
{
    final String SEPARATOR = "/";

   /**
    * Return the deployment target name.
    * @return the name
    */
    String getName();

   /**
    * Return the deployment poartition.
    * @return the partition
    */
    String getPartitionName();

   /**
    * Return the model fully qualified name.
    * @return the fully qualified name
    */
    String getQualifiedName();

   /**
    * Return the mode of establishment.
    * @return the mode
    */
    Mode getMode();

   /**
    * Return the assigned logger.
    * @return the logging channel
    */
    Logger getLogger();

   /**
    * Return the system context.
    *
    * @return the system context
    */
    SystemContext getSystemContext();

   /**
    * Return the dependency graph used to construct 
    * deployment and decommissioning sequences.
    *
    * @return the dependency graph
    */
    DependencyGraph getDependencyGraph();

   /**
    * Return the deployment profile.
    *
    * @return the profile
    */
    DeploymentProfile getProfile();
}
