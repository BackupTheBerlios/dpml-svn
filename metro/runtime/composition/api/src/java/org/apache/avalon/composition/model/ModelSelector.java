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
 * Interface implemented by a service selection implementation mechanism.  Classes
 * implementing the selector interface may be activated during the selection of
 * candidate service providers in an automated assembly process. 
 * A component author may declare a selection class explicitly via a
 * service dependency attribute with the attribute name of 
 * <code>urn:avalon:profile.selector</code> (but this will change to a model
 * driven approach).
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ModelSelector.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ModelSelector
{
    /**
     * Returns the preferred model from an available selection of
     * candidates capable of fulfilling a supplied service dependency.
     *
     * @param models the set of candidate models
     * @param dependency a service dependency
     * @return the preferred model or null if no satisfactory provider 
     *    can be established
     */
    DeploymentModel select( DeploymentModel[] models, DependencyDescriptor dependency );

    /**
     * Returns the preferred model from an available selection of candidates
     * @param models the set of candidate models 
     * @param stage the stage dependency
     * @return the preferred provider or null if no satisfactory provider 
     *    can be established
     */
    DeploymentModel select( DeploymentModel[] models, StageDescriptor stage );

    /**
     * Returns the preferred model from an available selection of candidates
     * @param models the set of candidate models 
     * @param reference the versioned service reference
     * @return the preferred provider or null if no satisfactory provider 
     *    can be established
     */
    DeploymentModel select( DeploymentModel[] models, ReferenceDescriptor reference );
}
