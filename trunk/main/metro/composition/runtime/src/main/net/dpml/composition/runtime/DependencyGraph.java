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
import java.rmi.RemoteException;

import net.dpml.component.Consumer;
import net.dpml.component.Component;
import net.dpml.component.ComponentRuntimeException;

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
     * services if no component in the current graph satisfies a dependency.
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
     * Component instances in the parent graph are potential providers for
     * services if no component in current assembly satisfies a dependency.
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
     * Add a component to current dependency graph.
     * 
     * @param component the component to add to the graph
     */
    public void add( final Component component )
    {
        if( !m_components.contains( component ) )
        {
            m_components.add( component );
        }
    }

    /**
     * Remove a component from the dependency graph.
     * 
     * @param component the component to remove
     */
    public void remove( final Component component )
    {
        m_components.remove( component );
    }

    /**
     * Get the serilized graph of objects required when
     * starting up the target. This makes sure that all providers are
     * established before their coresponding consumers in the graph.
     * 
     * @return the ordered list of components
     */
    public Component[] getStartupGraph()
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
     * @return the ordered list of component instances
     */
    public Component[] getShutdownGraph()
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
     * Get the serilized graph of instances that use services of the specified component.
     * 
     * @param provider the provider component
     * @return the ordered list of consumer components
     */
    public Component[] getConsumerGraph( final Component provider )
    {
        if( m_parent != null )
        {
            return m_parent.getConsumerGraph( provider );
        }
        try
        {
            Component[] graph = getComponentGraph( provider, false );
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
     * specified component with services.
     * 
     * @param consumer the consumer component
     * @return the ordered list of providers
     */
    public Component[] getProviderGraph( final Component consumer )
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
     * Return an component array that does not include the provided component.
     */
    private Component[] referencedComponents( final Component component, Component[] components )
    {
        ArrayList list = new ArrayList();
        for ( int i = 0; i < components.length; i++ )
        {
            if( !components[i].equals( component ) )
            {
                list.add( components[i] );
            }
        }
        return (Component[]) list.toArray( new Component[0] );
    }

    /**
     * Get the graph of a single component.
     * 
     * @param component the target component
     * @param providers true if traversing providers, false if consumers
     * @return the list of components
     */
    private Component[] getComponentGraph( final Component component,
            final boolean providers )
    {
        final ArrayList result = new ArrayList();
        visitcomponent( component, providers, new ArrayList(), result );
        final Component[] returnValue = new Component[result.size()];
        return (Component[]) result.toArray( returnValue );
    }

    /**
     * Method to generate an ordering of nodes to traverse. It is expected that
     * the specified components have passed verification tests and are well
     * formed.
     * 
     * @param direction true if forward dependencys traced, false if 
     *     dependencies reversed
     * @return the ordered component list
     */
    private Component[] walkGraph( final boolean direction )
    {
        final ArrayList result = new ArrayList();
        final ArrayList done = new ArrayList();

        final int size = m_components.size();
        for ( int i = 0; i < size; i++ )
        {
            final Component component = (Component) m_components.get( i );

            visitcomponent( component, direction, done, result );
        }

        final Component[] returnValue = new Component[result.size()];
        if( m_inProgress.size() != 0 )
        {
            throw new RuntimeException( "there where non-assembled components: "
                    + m_inProgress );
        }
        return (Component[]) result.toArray( returnValue );
    }

    /**
     * Visit a component when traversing dependencies.
     * 
     * @param component the component 
     * @param direction true if walking tree looking for providers, else false
     * @param done those nodes already traversed
     * @param order the order in which nodes have already been traversed
     */
    private void visitcomponent( final Component component,
            final boolean direction, final ArrayList done, final ArrayList order )
    {
        //if circular dependency
        if( m_inProgress.contains( component ) )
        {
            throw new RuntimeException(
                    "Cyclic dependency encoutered in:" + component 
                            + "is already in progress stack: "
                            + m_inProgress );
        }

        //If already visited this component return

        if( done.contains( component ) )
        {
            return;
        }
        done.add( component );
        m_inProgress.add( component );
        if( direction )
        {
            visitProviders( component, done, order );
        }
        else
        {
            visitConsumers( component, done, order );
        }

        m_inProgress.remove( component );
        order.add( component );
    }

    /**
     * Traverse graph of components that provide services to the specified
     * component.
     * 
     * @param component the component to be checked
     * @param done the list of already checked component
     * @param order the order
     */
    private void visitProviders( final Component component,
            final ArrayList done, final ArrayList order )
    {
        if( component instanceof Consumer )
        {
            Consumer consumer = (Consumer) component;
            try
            {
                Component[] providers = consumer.getProviders();
                for ( int i = ( providers.length - 1 ); i > -1; i-- )
                {
                     visitcomponent( providers[i], true, done, order );
                }
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Component raised a remote exception while querying providers."
                  + "\nComponent: " + component.getClass().getName();
                throw new ComponentRuntimeException( error, e );
            }
        }
    }

    /**
     * Traverse all consumers of a component. I.e. all components that use component
     * provided by the supplied component.
     * 
     * @param component the component to be checked
     * @param done the list of already checked components
     * @param order the order
     */
    private void visitConsumers( final Component component,
            final ArrayList done, final ArrayList order )
    {
        final int size = m_components.size();
        for ( int i = 0; i < size; i++ )
        {
            final Component other = (Component) m_components.get( i );
            if( other instanceof Consumer )
            {
                Consumer consumer = (Consumer) other;
                try
                {
                    final Component[] providers = consumer.getProviders();
                    for ( int j = 0; j < providers.length; j++ )
                    {
                        Component provider = providers[j];
                        if( provider.equals( component ) )
                        {
                            visitcomponent( other, false, done, order );
                        }
                    }
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Component raised a remote exception while querying providers."
                      + "\nComponent: " + component.getClass().getName();
                    throw new ComponentRuntimeException( error, e );
                }
            }
        }
        final int childCount = m_children.size();
        for ( int i = 0; i < childCount; i++ )
        {
            final DependencyGraph map = (DependencyGraph) m_children.get( i );
            map.visitConsumers( component, done, order );
        }
    }
}
