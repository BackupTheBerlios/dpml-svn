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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.Type;

/**
 * Deployment model defintion.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ComponentModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ComponentModel extends DeploymentModel
{
    static final String PROXY_KEY = "urn:composition:proxy";

   /**
    * Return the deployment type.
    * 
    * @return the type
    */
    Type getType();

   /**
    * Return the proxy enabled policy for the model. If the system wide
    * proxy enabled is disabled the operation will return false otherwise the 
    * value returned is true unless overriden by the "urn:composition:proxy"
    * attribute.
    *
    * @return the proxy policy
    */
    boolean getProxyPolicy();

   /**
    * Return the activation policy for the model. 
    * @return the activaltion policy
    */
    boolean getActivationPolicy();

   /**
    * Return the collection policy for the model. If a profile
    * does not declare a collection policy, the collection policy 
    * declared by the type will be used.
    *
    * @return the collection policy
    * @see org.apache.avalon.meta.info.InfoDescriptor#WEAK
    * @see org.apache.avalon.meta.info.InfoDescriptor#SOFT
    * @see org.apache.avalon.meta.info.InfoDescriptor#HARD
    */
    int getCollectionPolicy();

   /**
    * Set the collection policy to a supplied value.
    *
    * @param policy the collection policy
    * @see org.apache.avalon.meta.info.InfoDescriptor#WEAK
    * @see org.apache.avalon.meta.info.InfoDescriptor#SOFT
    * @see org.apache.avalon.meta.info.InfoDescriptor#HARD
    * @see org.apache.avalon.meta.info.InfoDescriptor#UNDEFINED
    */
    void setCollectionPolicy( int policy );

   /**
    * Set the activation policy for the model. 
    * @param policy the activaltion policy
    */
    void setActivationPolicy( boolean policy );

   /**
    * Return the class for the deployable target.
    * @return the class
    */
    Class getDeploymentClass();

   /**
    * Set the configuration to the supplied value.  The supplied 
    * configuration will replace the existing configuration.
    *
    * @param config the supplied configuration
    */
    void setConfiguration( Configuration config );

   /**
    * Set the configuration to the supplied value.  The supplied 
    * configuration may suppliment or replace the existing configuration.
    *
    * @param config the supplied configuration
    * @param policy if TRUE the supplied configuration replaces the current
    *   configuration otherwise the resoved configuration shall be layed above
    *   the configuration supplied with the profile which in turn is layer above 
    *   the type default configuration (if any)
    */
    void setConfiguration( Configuration config, boolean policy );

   /**
    * Return the configuration to be applied to the component.
    * The implementation returns the current configuration state.
    * If the the component type does not implementation the 
    * Configurable interface, the implementation returns null. 
    *
    * @return the qualified configuration
    */
    Configuration getConfiguration();

   /**
    * Test if the component type backing the model is 
    * parameterizable.
    *
    * @return TRUE if the component type is parameterizable
    *   otherwise FALSE
    */
    boolean isParameterizable();

   /**
    * Set the parameters to the supplied value.  The supplied 
    * parameters value will replace the existing parameters value.
    *
    * @param parameters the supplied parameters value
    */
    void setParameters( Parameters parameters );

   /**
    * Set the parameters to the supplied value.  The supplied 
    * parameters value may suppliment or replace the existing 
    * parameters value.
    *
    * @param parameters the supplied parameters
    * @param policy if TRUE the supplied parameters replaces the current
    *   parameters value otherwise the existing and supplied values
    *   are aggregrated
    */
    void setParameters( Parameters parameters, boolean policy );

   /**
    * Return the parameters to be applied to the component.
    * If the the component type does not implementation the 
    * Parameterizable interface, the implementation returns null. 
    *
    * @return the assigned parameters
    */
    Parameters getParameters();

   /**
    * Return the context model for this deployment model.
    * 
    * @return the context model
    * @see ContextModel#isEnabled
    */
    ContextModel getContextModel();

   /**
    * Return the dependency models for this deployment model.
    *
    * @return the dependency models
    */
    DependencyModel[] getDependencyModels();

   /**
    * Return a dependency model for a supplied descriptor or null
    * if no match found.
    *
    * @return the dependency model
    */
    DependencyModel getDependencyModel( DependencyDescriptor dependency );

   /**
    * Return the stage models for this deployment model.
    *
    * @return the stage models
    */
    StageModel[] getStageModels();

   /**
    * Return a stage model matching the supplied descriptor or null
    * if no match found.
    *
    * @param stage the stage descriptor
    * @return the matching stage model
    */
    StageModel getStageModel( StageDescriptor stage );

   /**
    * Return the set of services produced by the model as a array of classes.
    *
    * @return the service classes
    */
    Class[] getInterfaces();

}
