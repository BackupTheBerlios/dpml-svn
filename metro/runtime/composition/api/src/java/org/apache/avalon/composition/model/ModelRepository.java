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

import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;

/**
 * The model repository interface declares operations through which 
 * clients may resolve new or existing model instances relative to
 * a stage or service dependency.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ModelRepository.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ModelRepository
{
    /**
     * Locate an model matching the supplied name.
     *
     * @param name the model name
     * @return the model or null if the model name is unknown
     */
    DeploymentModel getModel( String name );

    /**
     * Locate a model meeting the supplied criteria.
     *
     * @param dependency a component service dependency
     * @return the model
     */
    DeploymentModel getModel( DependencyDescriptor dependency );

    /**
     * Locate a model meeting the supplied service criteria.
     *
     * @param reference a version interface descriptor
     * @return the model
     */
    DeploymentModel getModel( ReferenceDescriptor reference );

    /**
     * Locate all models meeting the supplied dependency criteria.
     *
     * @param dependency a component service dependency
     * @return the candidate models
     */
    DeploymentModel[] getCandidateProviders( 
      DependencyDescriptor dependency );

    /**
     * Locate all models meeting the supplied criteria.
     *
     * @param stage a component stage dependency
     * @return the candidate models
     */
    DeploymentModel[] getCandidateProviders( 
      StageDescriptor stage );

    /**
     * Locate all models meeting the supplied service reference criteria.
     *
     * @param reference a service reference
     * @return the candidate models
     */
    public DeploymentModel[] getCandidateProviders( 
      ReferenceDescriptor reference );

    /**
     * Locate a model meeting the supplied criteria.
     *
     * @param stage a component stage dependency
     * @return the model
     */
    DeploymentModel getModel( StageDescriptor stage );

    /**
     * Add an model to the repository.
     *
     * @param model the model to add
     * @exception DuplicateNameException if the name is already bound
     */
    void addModel( DeploymentModel model ) throws DuplicateNameException;

    /**
     * Add an model to the repository.
     *
     * @param name the name to register the model under
     * @param model the model to add
     */
    void addModel( String name, DeploymentModel model ) throws DuplicateNameException;

    /**
     * Remove a named model from the repository.
     *
     * @param name the name of the model to remove
     */
    void removeModel( String name );

    /**
     * Remove a model from the repository.
     *
     * @param model the model to remove
     */
    void removeModel( DeploymentModel model );

    /**
     * Locate an model meeting the supplied criteria.
     *
     * @return the model
     */
    public DeploymentModel[] getModels();

}
