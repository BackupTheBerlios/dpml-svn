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

import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ModelRepository;
import org.apache.avalon.composition.model.DuplicateNameException;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;


/**
 * The model repository interface declares operations through which 
 * clients may resolve registered model instances relative to
 * a stage or service dependencies.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultModelRepository.java 46310 2004-09-19 02:16:05Z niclas $
 */
public class DefaultModelRepository 
    implements ModelRepository
{
    //------------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------------

    /**
     * The parent appliance repository.
     */
    private ModelRepository m_parent;
    
    private Logger m_logger;
    
    /**
     * Table of registered appliance instances keyed by name.
     */
    private final Map m_models = new Hashtable();

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

    public DefaultModelRepository( 
      ModelRepository parent, Logger logger )
    {
        m_parent = parent;
        m_logger = logger;
    }
    
    //------------------------------------------------------------------
    // ModelRepository
    //------------------------------------------------------------------

    /**
     * Locate an model meeting the supplied criteria.
     *
     * @param dependency a component service dependency
     * @return the model or null if no matching model is resolved
     */
    public DeploymentModel getModel( DependencyDescriptor dependency )
    {
        return getModel( dependency.getReference() );
    }

    /**
     * Locate an model meeting the supplied criteria.
     *
     * @param reference a component service reference
     * @return the model or null if no matching model is resolved
     */
    public DeploymentModel getModel( ReferenceDescriptor reference )
    {
        //
        // attempt to locate a solution locally
        //

        Iterator iterator = m_models.values().iterator();
        while( iterator.hasNext() )
        {
            DeploymentModel model = (DeploymentModel) iterator.next();
            if( model.isaCandidate( reference ) )
            {
                return model;
            }
        }

        //
        // attempt to locate a solution from the parent
        //

        if( m_parent != null )
        {
            return m_parent.getModel( reference );
        }

        return null;
    }

    /**
     * Locate all models meeting the supplied criteria.
     *
     * @param stage a component stage dependency
     * @return the candidate models
     */
    public DeploymentModel[] getCandidateProviders( StageDescriptor stage )
    {
        ArrayList list = new ArrayList();
        Iterator iterator = m_models.values().iterator();
        while( iterator.hasNext() )
        {
            DeploymentModel model = (DeploymentModel) iterator.next();
            if( model.isaCandidate( stage ) )
            {
                list.add( model );
            }
        }

        if( m_parent != null )
        {
            DeploymentModel[] models = m_parent.getCandidateProviders( stage );
            for( int i=0; i<models.length; i++ )
            {
                list.add( models[i] );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    /**
     * Locate all models meeting the supplied dependency criteria.
     *
     * @param dependency a component service dependency
     * @return the candidate models
     */
    public DeploymentModel[] getCandidateProviders( 
      DependencyDescriptor dependency )
    {
        ArrayList list = new ArrayList();
        Iterator iterator = m_models.values().iterator();
        while( iterator.hasNext() )
        {
            DeploymentModel model = (DeploymentModel) iterator.next();
            if( model.isaCandidate( dependency ) )
            {
                list.add( model );
            }
        }

        if( m_parent != null )
        {
            DeploymentModel[] models = m_parent.getCandidateProviders( dependency );
            for( int i=0; i<models.length; i++ )
            {
                list.add( models[i] );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    /**
     * Locate all models meeting the supplied service reference criteria.
     *
     * @param reference a service reference
     * @return the candidate models
     */
    public DeploymentModel[] getCandidateProviders( 
      ReferenceDescriptor reference )
    {
        ArrayList list = new ArrayList();
        Iterator iterator = m_models.values().iterator();
        while( iterator.hasNext() )
        {
            DeploymentModel model = (DeploymentModel) iterator.next();
            if( model.isaCandidate( reference ) )
            {
                list.add( model );
            }
        }

        if( m_parent != null )
        {
            DeploymentModel[] models = m_parent.getCandidateProviders( reference );
            for( int i=0; i<models.length; i++ )
            {
                list.add( models[i] );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    /**
     * Locate a model meeting the supplied criteria.
     *
     * @param stage a component stage dependency
     * @return the model
     */
    public DeploymentModel getModel( StageDescriptor stage )
    {
        Iterator iterator = m_models.values().iterator();
        while( iterator.hasNext() )
        {
            DeploymentModel model = (DeploymentModel) iterator.next();

            if( model.isaCandidate( stage ) )
            {
                return model;
            }
        }

        if( m_parent != null )
        {
            return m_parent.getModel( stage );
        }

        return null;
    }

    //------------------------------------------------------------------
    // implementation
    //------------------------------------------------------------------

    /**
     * Add an model to the repository.
     *
     * @param model the model to add
     */
    public void addModel( DeploymentModel model )
      throws DuplicateNameException
    {
        final String name = model.getName();
        addModel( name, model );
    }

    /**
     * Add an model to the repository.
     *
     * @param name the name to register the model under
     * @param model the model to add
     */
    public void addModel( String name, DeploymentModel model )
      throws DuplicateNameException
    {
        if( null != m_models.get( name ) )
        {
            throw new DuplicateNameException( name );
        }
        m_models.put( name, model );
    }

    /**
     * Remove a model from the repository.
     *
     * @param model the model to remove
     */
    public void removeModel( DeploymentModel model )
    {
        removeModel( model.getName() );
    }

    /**
     * Remove a named model from the repository.
     *
     * @param name the name of the model to remove
     */
    public void removeModel( String name )
    {
        m_models.remove( name );
    }

    /**
     * Return a sequence of all of the local models.
     *
     * @return the model sequence
     */
    public DeploymentModel[] getModels()
    {
        return (DeploymentModel[]) m_models.values().toArray( 
          new DeploymentModel[0] );
    }

    /**
     * Locate a local model matching the supplied name.
     *
     * @param name the model name
     * @return the model or null if the model name is unknown
     */
    public DeploymentModel getModel( String name )
    {
        DeploymentModel model = (DeploymentModel) m_models.get( name );
        if( model == null && m_logger != null )
        {
            m_logger.debug( 
              "Can't find '" 
              + name 
              + "' in model repository: " 
              + m_models );
        }
        return model;
    }
}
