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

import java.util.ArrayList;

import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ModelSelector;

import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;


/**
 * Default selector class. The default selector selcts profiles based
 * of ranking of profile relative to EXPLICIT, PACKAGED and IMPLICIT
 * status. For each category, if a supplied profile matches the category
 * the first profile matching the category is returned.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultModelSelector.java 30977 2004-07-30 08:57:54Z niclas $
 */
class DefaultModelSelector implements ModelSelector
{
    //==============================================================
    // ModelSelector
    //==============================================================

    /**
     * Returns the preferred model from an available selection of
     * candidates capable of fulfilling a supplied service dependency.
     *
     * @param models the set of candidate models
     * @param dependency a service dependency
     * @return the preferred model or null if no satisfactory 
     *    provider can be established
     */
    public DeploymentModel select( DeploymentModel[] models, DependencyDescriptor dependency )
    {
        DeploymentModel[] candidates = filterCandidateProviders( models, dependency );
        return select( candidates );
    }

    /**
     * Returns the preferred model from an available selection of candidates
     * @param models the set of candidate models 
     * @param stage the stage dependency
     * @return the preferred provider or null if no satisfactory 
     *    provider can be established
     */
    public DeploymentModel select( DeploymentModel[] models, StageDescriptor stage )
    {
        DeploymentModel[] candidates = filterCandidateProviders( models, stage );
        return select( candidates );
    }

    /**
     * Returns the preferred model from an available selection of candidates
     * @param models the set of candidate models 
     * @param reference the versioned service reference
     * @return the preferred provider or null if no satisfactory provider 
     *    can be established
     */
    public DeploymentModel select( DeploymentModel[] models, ReferenceDescriptor reference )
    {
        DeploymentModel[] candidates = filterCandidateProviders( models, reference );
        return select( candidates );
    }


    //==============================================================
    // implementation
    //==============================================================

    private DeploymentModel[] filterCandidateProviders( 
      DeploymentModel[] models, ReferenceDescriptor reference )
    {
        ArrayList list = new ArrayList();
        for( int i = 0; i < models.length; i++ )
        {
            DeploymentModel model = models[i];
            if( model.isaCandidate( reference ) )
            {
                list.add( model );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    private DeploymentModel[] filterCandidateProviders( 
      DeploymentModel[] models, DependencyDescriptor dependency )
    {
        ArrayList list = new ArrayList();
        for( int i = 0; i < models.length; i++ )
        {
            DeploymentModel model = models[i];
            if( model.isaCandidate( dependency ) )
            {
                list.add( model );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    private DeploymentModel[] filterCandidateProviders( 
      DeploymentModel[] models, StageDescriptor stage )
    {
        ArrayList list = new ArrayList();
        for( int i = 0; i < models.length; i++ )
        {
            DeploymentModel model = models[i];
            if( model.isaCandidate( stage ) )
            {
                list.add( model );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    /**
     * Select a model from a set of models based on a priority ordering
     * of EXPLICIT, PACKAGE and lastly IMPLICIT.  If multiple candidates
     * exist for a particulr mode, return the first candidate.
     *
     * @param profiles the set of candidate profiles
     * @param dependency the service dependency
     * @return the preferred profile or null if no satisfactory 
     *   provider can be established
     */
    private DeploymentModel select( DeploymentModel[] models )
    {
        if( models.length == 0 )
        {
            return null;
        }

        for( int i=0; i<models.length; i++ )
        {
            if( models[i].getMode().equals( Mode.EXPLICIT ) )
            {
                return models[i];
            }
        }

        for( int i=0; i<models.length; i++ )
        {
            if( models[i].getMode().equals( Mode.PACKAGED ) )
            {
                return models[i];
            }
        }

        for( int i=0; i<models.length; i++ )
        {
            if( models[i].getMode().equals( Mode.IMPLICIT ) )
            {
                return models[i];
            }
        }

        return null;
    }
}
