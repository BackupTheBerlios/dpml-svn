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

import org.apache.avalon.composition.data.DeploymentProfile;

import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;

/**
 * Interface defining the contract for profile selection.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ProfileSelector.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ProfileSelector
{
    /**
     * Returns the preferred profile from an available selection of
     * candidates profiles.
     *
     * @param profiles the set of candidate profiles
     * @param dependency a service dependency
     * @return the preferred profile or null if no satisfactory profile 
     *    can be established
     */
    DeploymentProfile select( DeploymentProfile[] profiles, DependencyDescriptor dependency );

    /**
     * Returns the preferred profile from an available selection of
     * candidates profiles.
     *
     * @param profiles the set of candidate profiles
     * @param stage a stage dependency
     * @return the preferred extension provider profile or null if 
     *    no satisfactory profile can be established
     */
    DeploymentProfile select( DeploymentProfile[] profiles, StageDescriptor stage );

    /**
     * Returns the preferred profile from an available selection of
     * candidates profiles.
     *
     * @param profiles the set of candidate profiles
     * @param reference a service reference
     * @return the preferred profile or null if 
     *    no satisfactory profile can be established
     */
    DeploymentProfile select( DeploymentProfile[] profiles, ReferenceDescriptor reference );

}
