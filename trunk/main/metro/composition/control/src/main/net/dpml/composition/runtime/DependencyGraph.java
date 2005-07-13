/* 
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Apache Software Foundation
 *
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

package net.dpml.composition.runtime;

import java.util.ArrayList;

import net.dpml.part.control.Consumer;
import net.dpml.part.service.Service;

/**
 * <p>Utility class to aquire an ordered graph of
 * consumers and providers services.</p>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: DependencyGraph.java 259 2004-10-30 07:24:40Z mcconnell $
 */
public class DependencyGraph
{
    /**
     * Parent Map. Services in the parent Map are potential Providers for
     * services if no service in the current graph satisfies a dependency.
     */
    private final DependencyGraph m_parent;

    /**
     * The set of services declared by the container as available. Used when
     * searching for providers/consumers.
     */
    private final ArrayList m_components = new ArrayList();

    /**
     * The child {@link DependencyGraph}objects. Possible consumers of services
     * in this assembly.
     */
    private final ArrayList m_children = new ArrayList();

     /**
      * holds the services assembled in order to track circular dependencies
      */
     private ArrayList m_inProgress = new ArrayList();
 
    /**
     * Creation of a new empty dependency graph.
     */
    public DependencyGraph()
    {
        this( null );
    }

    /**
     * Creation of a new dependecy graph holding a reference to a parent graph.
     * Service instances in the parent graph are potential providers for
     * services if no service in current assembly satisfies a dependency.
     * 
     * @param parent the parent graph
     */
    public DependencyGraph( final DependencyGraph parent )
    {
        m_parent = parent;
    }

    /**
     * Addition of a consumer dependency graph.
     * 
     * @param child the child map
     */
    public void addChild( final DependencyGraph child )
    {
        m_children.add( child );
    }

    /**
     * Removal of a consumer dependency graph.
     * 
     * @param child the child map
     */
    public void removeChild( final DependencyGraph child )
    {
        m_children.remove( child );
    }

    /**
     * Add a service to current dependency graph.
     * 
     * @param service the service to add to the graph
     */
    public void add( final Service service )
    {
        if( !m_components.contains( service ) )
        {
            m_components.add( service );
        }
    }

    /**
     * Remove a service from the dependency graph.
     * 
     * @param service the service to remove
     */
    public void remove( final Service service )
    {
        m_components.remove( service );
    }

    /**
     * Get the serilized graph of objects required when
     * starting up the target. This makes sure that all providers are
     * established before their coresponding consumers in the graph.
     * 
     * @return the ordered list of components
     */
    public Service[] getStartupGraph()
    {
        try
        {
            return walkGraph( true );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving startup graph.";
            throw new RuntimeException( error, e );
        }
    }

    /**
     * Get the serilized graph of instances required
     * when shutting down all the components. This makes sure that all consumer
     * shutdown actions occur before their coresponding providers in graph.
     * 
     * @return the ordered list of service instances
     */
    public Service[] getShutdownGraph()
    {
        try
        {
            return walkGraph( false );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving shutdown graph.";
            throw new RuntimeException( error, e );
        }
    }

    /**
     * Get the serilized graph of instances that use services of the specified service.
     * 
     * @param provider the provider service
     * @return the ordered list of consumer components
     */
    public Service[] getConsumerGraph( final Service provider )
    {
        if( m_parent != null )
        {
            return m_parent.getConsumerGraph( provider );
        }
        try
        {
            Service[] graph = getComponentGraph( provider, false );
            return referencedComponents( provider, graph );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving consumer graph for compoent: "
                    + provider;
            throw new RuntimeException( error, e );
        }
    }

    /**
     * Get the serilized graph of istances that provide
     * specified service with services.
     * 
     * @param consumer the consumer service
     * @return the ordered list of providers
     */
    public Service[] getProviderGraph( final Service consumer )
    {
        try
        {
            return referencedComponents( consumer, getComponentGraph( consumer, true ) );
        }
        catch ( Throwable e )
        {
            final String error = "Unexpect error while resolving provider graph for: "
                    + consumer;
            throw new RuntimeException( error, e );
        }
    }

    /**
     * Return an service array that does not include the provided service.
     */
    private Service[] referencedComponents( final Service service, Service[] components )
    {
        ArrayList list = new ArrayList();
        for ( int i = 0; i < components.length; i++ )
        {
            if( !components[i].equals( service ) )
            {
                list.add( components[i] );
            }
        }
        return (Service[]) list.toArray( new Service[0] );
    }

    /**
     * Get the graph of a single service.
     * 
     * @param service the target service
     * @param providers true if traversing providers, false if consumers
     * @return the list of components
     */
    private Service[] getComponentGraph( final Service service,
            final boolean providers )
    {
        final ArrayList result = new ArrayList();
        visitcomponent( service, providers, new ArrayList(), result );
        final Service[] returnValue = new Service[result.size()];
        return (Service[]) result.toArray( returnValue );
    }

    /**
     * Method to generate an ordering of nodes to traverse. It is expected that
     * the specified components have passed verification tests and are well
     * formed.
     * 
     * @param direction true if forward dependencys traced, false if 
     *     dependencies reversed
     * @return the ordered service list
     */
    private Service[] walkGraph( final boolean direction )
    {
        final ArrayList result = new ArrayList();
        final ArrayList done = new ArrayList();

        final int size = m_components.size();
        for ( int i = 0; i < size; i++ )
        {
            final Service service = (Service) m_components.get( i );

            visitcomponent( service, direction, done, result );
        }

        final Service[] returnValue = new Service[result.size()];
        if( m_inProgress.size() != 0 )
        {
            throw new RuntimeException( "there where non-assembled components: "
                    + m_inProgress );
        }
        return (Service[]) result.toArray( returnValue );
    }

    /**
     * Visit a service when traversing dependencies.
     * 
     * @param service the service 
     * @param direction true if walking tree looking for providers, else false
     * @param done those nodes already traversed
     * @param order the order in which nodes have already been traversed
     */
    private void visitcomponent( final Service service,
            final boolean direction, final ArrayList done, final ArrayList order )
    {
        //if circular dependency
        if( m_inProgress.contains( service ) )
        {
            throw new RuntimeException(
                    "Cyclic dependency encoutered in:" + service 
                            + "is already in progress stack: "
                            + m_inProgress );
        }

        //If already visited this service return

        if( done.contains( service ) )
        {
            return;
        }
        done.add( service );
        m_inProgress.add( service );
        if( direction )
        {
            visitProviders( service, done, order );
        }
        else
        {
            visitConsumers( service, done, order );
        }

        m_inProgress.remove( service );
        order.add( service );
    }

    /**
     * Traverse graph of components that provide services to the specified
     * service.
     * 
     * @param service the service to be checked
     * @param done the list of already checked service
     * @param order the order
     */
    private void visitProviders( final Service service,
            final ArrayList done, final ArrayList order )
    {
        if( service instanceof Consumer )
        {
            Consumer consumer = (Consumer) service;
            Service[] providers = consumer.getProviders();
            for ( int i = ( providers.length - 1 ); i > -1; i-- )
            {
                visitcomponent( providers[i], true, done, order );
            }
        }
    }

    /**
     * Traverse all consumers of a service. I.e. all components that use service
     * provided by the supplied service.
     * 
     * @param service the service to be checked
     * @param done the list of already checked components
     * @param order the order
     */
    private void visitConsumers( final Service service,
            final ArrayList done, final ArrayList order )
    {
        final int size = m_components.size();
        for ( int i = 0; i < size; i++ )
        {
            final Service other = (Service) m_components.get( i );
            if( other instanceof Consumer )
            {
                Consumer consumer = (Consumer) other;
                final Service[] providers = consumer.getProviders();
                for ( int j = 0; j < providers.length; j++ )
                {
                    Service provider = providers[j];
                    if( provider.equals( service ) )
                    {
                        visitcomponent( other, false, done, order );
                    }
                }
            }
        }
        final int childCount = m_children.size();
        for ( int i = 0; i < childCount; i++ )
        {
            final DependencyGraph map = (DependencyGraph) m_children.get( i );
            map.visitConsumers( service, done, order );
        }
    }
}
