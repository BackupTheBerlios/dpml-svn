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

import org.apache.avalon.composition.model.DeploymentModel;

/**
 * Defintion of runtime services.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Runtime.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Runtime
{
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
}
