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

import java.util.ArrayList;

/**
 * <p>Utility class to aquire an ordered graph of
 * consumers and providers models.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DependencyGraph.java 46037 2004-09-14 21:04:40Z niclas $
 */
public class DependencyGraph
{
    /**
     * Parent Map. Components in the parent Map are potential Providers for
     * services if no model in the current graph satisfies a dependency.
     */
    private final DependencyGraph m_parent;

    /**
     * The set of models declared by the container as available. Used when
     * searching for providers/consumers.
     */
    private final ArrayList m_models = new ArrayList();

    /**
     * The child {@link DependencyGraph}objects. Possible consumers of services
     * in this assembly.
     */
    private final ArrayList m_children = new ArrayList();

     /**
      * holds the models assembled in order to track circular deps etc.
      */
     private ArrayList m_modelsInProgress = new ArrayList();
 
    /**
     * Creation of a new empty dependency graph.
     */
    public DependencyGraph()
    {
        this( null );
    }

    /**
     * Creation of a new dependecy graph holding a reference to a parent graph.
     * DeploymentModel instances in the parent graph are potential providers for
     * services if no model in current assembly satisfies a dependency.
     * 
     * @param parent
     *            the parent graph
     */
    public DependencyGraph( final DependencyGraph parent )
    {
        m_parent = parent;
    }

    /**
     * Addition of a consumer dependency graph.
     * 
     * @param child
     *            the child map
     */
    public void addChild( final DependencyGraph child )
    {
        m_children.add( child );
    }

    /**
     * Removal of a consumer dependency graph.
     * 
     * @param child
     *            the child map
     */
    public void removeChild( final DependencyGraph child )
    {
        m_children.remove( child );
    }

    /**
     * Add a model to current dependency graph.
     * 
     * @param model
     *            the model to add to the graph
     */
    public void add( final DeploymentModel model )
    {
        if( !m_models.contains( model ) )
        {
            m_models.add( model );
        }
    }

    /**
     * Remove a model from the dependency graph.
     * 
     * @param model
     *            the model to remove
     */
    public void remove( final DeploymentModel model )
    {
        m_models.remove( model );
    }

    /**
     * Get the serilized graph of {@link DeploymentModel}objects required when
     * starting up the target. This makes sure that all providers are
     * established before their coresponding consumers in the graph.
     * 
     * @return the ordered list of models
     */
    public DeploymentModel[] getStartupGraph()
    {
        try
        {
            return walkGraph( true );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving startup graph.";
            throw new ModelRuntimeException( error, e );
        }
    }

    /**
     * Get the serilized graph of {@link DeploymentModel}instances required
     * when shutting down all the components. This makes sure that all consumer
     * shutdown actions occur before their coresponding providers in graph.
     * 
     * @return the ordered list of model instances
     */
    public DeploymentModel[] getShutdownGraph()
    {
        try
        {
            return walkGraph( false );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving shutdown graph.";
            throw new ModelRuntimeException( error, e );
        }
    }

    /**
     * Get the serilized graph of {@link DeploymentModel}instances that use
     * services of the specified model.
     * 
     * @param model
     *            the model
     * @return the ordered list of consumer model instances
     */
    public DeploymentModel[] getConsumerGraph( final DeploymentModel model )
    {
        if( m_parent != null )
        {
            return m_parent.getConsumerGraph( model );
        }
        try
        {
            DeploymentModel[] graph = getComponentGraph( model, false );
            return referencedModels( model, graph );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving consumer graph for model: "
                    + model;
            throw new ModelRuntimeException( error, e );
        }
    }

    /**
     * Get the serilized graph of {@link DeploymentModel}istances that provide
     * specified model with services.
     * 
     * @param model
     *            the model
     * @return the ordered list of providers
     */
    public DeploymentModel[] getProviderGraph( final DeploymentModel model )
    {
        try
        {
            return referencedModels( model, getComponentGraph( model, true ) );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving provider graph for: "
                    + model;
            throw new ModelRuntimeException( error, e );
        }
    }

    /**
     * Return an model array that does not include the provided model.
     */
    private DeploymentModel[] referencedModels( final DeploymentModel model,
            DeploymentModel[] models )
    {
        ArrayList list = new ArrayList();
        for ( int i = 0; i < models.length; i++ )
        {
            if( !models[i].equals( model ) )
            {
                list.add( models[i] );
            }
        }
        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    /**
     * Get the graph of a single model.
     * 
     * @param model
     *            the target model
     * @param providers
     *            true if traversing providers, false if consumers
     * @return the list of models
     */
    private DeploymentModel[] getComponentGraph( final DeploymentModel model,
            final boolean providers )
    {
        final ArrayList result = new ArrayList();
        visitcomponent( model, providers, new ArrayList(), result );

        final DeploymentModel[] returnValue = new DeploymentModel[result.size()];
        return (DeploymentModel[]) result.toArray( returnValue );
    }

    /**
     * Method to generate an ordering of nodes to traverse. It is expected that
     * the specified components have passed verification tests and are well
     * formed.
     * 
     * @param direction
     *            true if forward dependencys traced, false if dependencies
     *            reversed
     * @return the ordered model list
     */
    private DeploymentModel[] walkGraph( final boolean direction )
    {
        final ArrayList result = new ArrayList();
        final ArrayList done = new ArrayList();

        final int size = m_models.size();
        for ( int i = 0; i < size; i++ )
        {
            final DeploymentModel model = (DeploymentModel) m_models.get( i );

            visitcomponent( model, direction, done, result );
        }

        final DeploymentModel[] returnValue = new DeploymentModel[result.size()];
        if( m_modelsInProgress.size() != 0 )
        {
            throw new RuntimeException( "there where non-assembled models: "
                    + m_modelsInProgress );
        }
        return (DeploymentModel[]) result.toArray( returnValue );
    }

    /**
     * Visit a model when traversing dependencies.
     * 
     * @param model
     *            the model
     * @param direction
     *            true if walking tree looking for providers, else false
     * @param done
     *            those nodes already traversed
     * @param order
     *            the order in which nodes have already been traversed
     */
    private void visitcomponent( final DeploymentModel model,
            final boolean direction, final ArrayList done, final ArrayList order )
    {
        //if circular dependency
        if( ( model instanceof ComponentModel )
                && m_modelsInProgress.contains( model ) )
        {
            throw new CyclicDependencyException(
                    "Cyclic dependency encoutered in assembly:" + model
                            + "is already in progress stack: "
                            + m_modelsInProgress );
        }
        //If already visited this model return

        if( done.contains( model ) )
        {
            return;
        }
        done.add( model );
        m_modelsInProgress.add( model );
        if( direction )
        {
            visitProviders( model, done, order );

        }
        else
        {
            visitConsumers( model, done, order );
        }

        m_modelsInProgress.remove( model );
        order.add( model );
    }

    /**
     * Traverse graph of components that provide services to the specified
     * model.
     * 
     * @param model
     *            the model to be checked
     * @param done
     *            the list of already checked models
     * @param order
     *            the order
     */
    private void visitProviders( final DeploymentModel model,
            final ArrayList done, final ArrayList order )
    {
        DeploymentModel[] providers = model.getProviders();
        for ( int i = ( providers.length - 1 ); i > -1; i-- )
        {
            visitcomponent( providers[i], true, done, order );
        }
    }

    /**
     * Traverse all consumers of a model. I.e. all models that use service
     * provided by the supplied model.
     * 
     * @param model
     *            the model to be checked
     * @param done
     *            the list of already checked models
     * @param order
     *            the order
     */
    private void visitConsumers( final DeploymentModel model,
            final ArrayList done, final ArrayList order )
    {
        final int size = m_models.size();
        for ( int i = 0; i < size; i++ )
        {
            final DeploymentModel other = (DeploymentModel) m_models.get( i );
            final DeploymentModel[] providers = other.getProviders();
            for ( int j = 0; j < providers.length; j++ )
            {
                DeploymentModel provider = providers[j];
                if( provider.equals( model ) )
                {
                    visitcomponent( other, false, done, order );
                }
            }
        }
        final int childCount = m_children.size();
        for ( int i = 0; i < childCount; i++ )
        {
            final DependencyGraph map = (DependencyGraph) m_children.get( i );
            map.visitConsumers( model, done, order );
        }
    }
}
