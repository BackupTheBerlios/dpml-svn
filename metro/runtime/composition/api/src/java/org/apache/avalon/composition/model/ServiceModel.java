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

import org.apache.avalon.composition.data.ServiceDirective;

/**
 * Service model manages service exported by a container.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ServiceModel
{
   /**
    * Return the service directive for the model.
    *
    * @return the directive declaring the service export
    */
    ServiceDirective getServiceDirective();

   /**
    * Return the service class.  
    * @return the service class
    */
    Class getServiceClass();

   /**
    * Return the service provider.  
    * @return the model identifying the provider implementation
    */
    public DeploymentModel getServiceProvider();
}
