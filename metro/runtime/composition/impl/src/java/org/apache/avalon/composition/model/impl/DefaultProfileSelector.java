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

package org.apache.avalon.composition.model.impl;

import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.model.ProfileSelector;

import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;

/**
 * Default profile selector class. The default selector selects profiles based
 * of ranking of profile relative to EXPLICIT, PACKAGED and IMPLICIT
 * status. For each category, if a supplied profile matches the category
 * the first profile matching the category is returned.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultProfileSelector.java 30977 2004-07-30 08:57:54Z niclas $
 */
class DefaultProfileSelector implements ProfileSelector
{
    //==============================================================
    // ProfileSelector
    //==============================================================

    /**
     * Returns the preferred profile from an available selection of
     * candidates profiles.
     *
     * @param profiles the set of candidate profiles
     * @param dependency a service dependency
     * @return the preferred profile or null if no satisfactory profile 
     *    can be established
     */
    public DeploymentProfile select( DeploymentProfile[] profiles, DependencyDescriptor dependency )
    {
        return select( profiles );
    }

    /**
     * Returns the preferred profile from an available selection of
     * candidates profiles.
     *
     * @param profiles the set of candidate profiles
     * @param dependency a stage dependency
     * @return the preferred extension provider profile or null if 
     *    no satisfactory profile can be established
     */
    public DeploymentProfile select( DeploymentProfile[] profiles, StageDescriptor stage )
    {
        return select( profiles );
    }

    /**
     * Returns the preferred profile from an available selection of
     * candidates profiles.
     *
     * @param profiles the set of candidate profiles
     * @param reference a service reference
     * @return the preferred profile or null if 
     *    no satisfactory profile can be established
     */
    public DeploymentProfile select( DeploymentProfile[] profiles, ReferenceDescriptor reference )
    {
        return select( profiles );
    }

    //==============================================================
    // implementation
    //==============================================================

    /**
     * Select a profile from a set of profiles based on a priority ordering
     * of EXPLICIT, PACKAGE and lastly IMPLICIT.  If multiple candidates
     * exist for a particular mode, return the first candidate.
     *
     * @param profiles the set of candidate profiles
     * @param dependency the service dependency
     * @return the preferred profile or null if no satisfactory 
     *   provider can be established
     */
    private DeploymentProfile select( DeploymentProfile[] profiles )
    {
        if( profiles.length == 0 )
        {
            return null;
        }

        for( int i=0; i<profiles.length; i++ )
        {
            if( profiles[i].getMode().equals( Mode.EXPLICIT ) )
            {
                return profiles[i];
            }
        }

        for( int i=0; i<profiles.length; i++ )
        {
            if( profiles[i].getMode().equals( Mode.PACKAGED ) )
            {
                return profiles[i];
            }
        }

        for( int i=0; i<profiles.length; i++ )
        {
            if( profiles[i].getMode().equals( Mode.IMPLICIT ) )
            {
                return profiles[i];
            }
        }

        return null;
    }
}
