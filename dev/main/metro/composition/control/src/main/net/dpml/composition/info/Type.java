/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.composition.info;

import java.io.Serializable;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;

import net.dpml.configuration.Configuration;

import net.dpml.part.state.State;
import net.dpml.part.manager.ComponentException;

/**
 * This class contains the meta information about a particular
 * component type. It describes;
 *
 * <ul>
 *   <li>Human presentable meta data such as name, version, description etc
 *   useful when assembling the system.</li>
 *   <li>the context object capabilities that this component requires</li>
 *   <li>the services that this component type is capable of providing</li>
 *   <li>the services that this component type requires to operate (and the
 *   names via which services are accessed)</li>
 *   <li>extended lifecycle stages that this component uses</li>
 * </ul>
 *
 * <p><b>UML</b></p>
 * <p><image src="doc-files/Type.gif" border="0"/></p>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Type.java 2958 2005-07-03 08:11:07Z mcconnell@dpml.net $
 */
public class Type implements Serializable
{
    static final long serialVersionUID = 1L;

    public static Type loadType( Class clazz ) throws ComponentException
    {
        String path = clazz.getName().replace( '.', '/' ) + ".type";
        URL url = clazz.getClassLoader().getResource( path );
        if( null == url )
        {
            return null;
        }
        try
        {
            InputStream input = url.openStream();
            ObjectInputStream stream = new ObjectInputStream( input );
            TypeHolder holder = (TypeHolder) stream.readObject();
            byte[] bytes = holder.getByteArray();
            ByteArrayInputStream byteinput = new ByteArrayInputStream( bytes );
            ObjectInputStream bytestream = new ObjectInputStream( byteinput );
            return (Type) bytestream.readObject();
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error occured while attempting to load a type."
              + "\nResource path: " + path;
            throw new ComponentException( error, e );
        }
    }

    private final State m_graph;
    private final InfoDescriptor m_descriptor;
    private final ContextDescriptor m_context;
    private final Configuration m_configuration;
    private final ServiceDescriptor[] m_services;
    private final CategoryDescriptor[] m_loggers;
    private final PartDescriptor[] m_parts;

    /**
     * Creation of a new Type instance using a supplied component descriptor,
     * logging, cotext, services, and part descriptors.
     *
     * @param graph the component state graph
     * @param descriptor a component descriptor that contains information about
     *   the component type
     * @param loggers a set of logger descriptors the declare the logging channels
     *   required by the type
     * @param context a component context descriptor that declares the context type
     *   and context entry key and value classnames
     * @param services a set of service descriprors that detail the service that
     *   this component type is capable of supplying
     * @param defaults the static configuration defaults
     * @param parts an array of part descriptors
     * @exception NullPointerException if the graph, descriptor, loggers, context, services
     *   or part argument are null
     */
    public Type( final State graph, 
                 final InfoDescriptor descriptor,
                 final CategoryDescriptor[] loggers,
                 final ContextDescriptor context,
                 final ServiceDescriptor[] services,
                 final Configuration defaults,
                 final PartDescriptor[] parts )
            throws NullPointerException 
    {
        if ( null == descriptor )
        {
            throw new NullPointerException( "descriptor" );
        }
        if ( null == loggers )
        {
            throw new NullPointerException( "loggers" );
        }
        if ( null == context )
        {
            throw new NullPointerException( "context" );
        }
        if ( null == services )
        {
            throw new NullPointerException( "services" );
        }

        m_graph = graph;
        m_descriptor = descriptor;
        m_loggers = loggers;
        m_context = context;
        m_services = services;
        m_configuration = defaults;

        if( null == parts )
        {
            m_parts = new PartDescriptor[0];
        }
        else
        {
            m_parts = parts;
        }
    }

    /**
     * Return the Component descriptor.
     *
     * @return the Component descriptor.
     */
    public InfoDescriptor getInfo()
    {
        return m_descriptor;
    }

    /**
     * Return the Component descriptor.
     *
     * @return the Component descriptor.
     */
    public State getStateGraph()
    {
        return m_graph;
    }

    /**
     * Return the set of Logger that this Component will use.
     *
     * @return the set of Logger that this Component will use.
     */
    public CategoryDescriptor[] getCategories()
    {
        return m_loggers;
    }

    /**
     * Return TRUE if the set of Logger descriptors includes the supplied name.
     *
     * @param name the logging subcategory name
     * @return TRUE if the logging subcategory is declared.
     */
    public boolean isaCategory( String name )
    {
        CategoryDescriptor[] loggers = getCategories();
        for( int i = 0; i < loggers.length; i++ )
        {
            CategoryDescriptor logger = loggers[ i ];
            if( logger.getName().equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the ContextDescriptor for component, may be null.
     * If null then this component does not implement Contextualizable.
     *
     * @return the ContextDescriptor for component, may be null.
     */
    public ContextDescriptor getContext()
    {
        return m_context;
    }

    /**
     * Return the set of Services that this component is capable of providing.
     *
     * @return the set of Services that this component is capable of providing.
     */
    public ServiceDescriptor[] getServices()
    {
        return m_services;
    }

    /**
     * Retrieve a service with a particular reference.
     *
     * @param reference a service reference descriptor
     * @return the service descriptor or null if it does not exist
     */
    public ServiceDescriptor getService( final ReferenceDescriptor reference )
    {
        for ( int i = 0; i < m_services.length; i++ )
        {
            final ServiceDescriptor service = m_services[i];
            if ( service.getReference().matches( reference ) )
            {
                return service;
            }
        }
        return null;
    }

    /**
     * Retrieve a service with a particular classname.
     *
     * @param classname the service classname
     * @return the service descriptor or null if it does not exist
     */
    public ServiceDescriptor getService( final String classname )
    {
        for ( int i = 0; i < m_services.length; i++ )
        {
            final ServiceDescriptor service = m_services[i];
            if ( service.getReference().getClassname().equals( classname ) )
            {
                return service;
            }
        }
        return null;
    }

    /**
     * Returns the default configuration supplied with the type.
     *
     * @return the default configuration or null if no packaged defaults
     */
    public Configuration getConfiguration()
    {
        return m_configuration;
    }

    /**
     * Returns the parts declared by this compoent type.
     *
     * @return the part descriptors
     */
    public PartDescriptor[] getPartDescriptors()
    {
        return m_parts;
    }

    /**
     * Retrieve an identified part.
     *
     * @param key the part key
     * @return the part (possibly null)
     */
    public PartDescriptor getPartDescriptor( final String key )
    {
        for ( int i = 0; i < m_parts.length; i++ )
        {
            PartDescriptor part = m_parts[i];
            if ( part.getKey().equals( key ) )
            {
                return part;
            }
        }
        return null;
    }

    /**
     * Return a string representation of the type.
     * @return the stringified type
     */
    public String toString()
    {
        return getInfo().toString();
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        if( null == other )
            return false;

        if( ! (other instanceof Type ) )
            return false;

        Type t = (Type) other;

        if( ! m_descriptor.equals( t.m_descriptor ) )
        {
            return false;
        }
        if( null == m_graph )
        {
            if( ! ( null == t.m_graph ) )
            {
                return false;
            }
        }
        else
        {
            if( ! m_graph.equals( t.m_graph ) )
            {
                return false;
            }
        }
        if( null == m_configuration )
        {
            if( ! ( null == t.m_configuration ) )
            {
                return false;
            }
        }
        else
        {
            if( ! m_configuration.equals( t.m_configuration ) )
            {
                return false;
            }
        }
        if( m_parts.length != t.m_parts.length )
        {
            return false;
        }
        else
        {
            for( int i=0; i<m_parts.length; i++ )
            {
                if( ! m_parts[i].equals( t.m_parts[i] ) )
                {
                    return false;
                }
            }
        }

        if( ! m_context.equals( t.m_context ) )
        {
            return false;
        }
        for( int i=0; i<m_loggers.length; i++ )
        {
            if( ! m_loggers[i].equals( t.m_loggers[i] ) )
            {
                return false;
            }
        }
        for( int i=0; i<m_services.length; i++ )
        {
            if( ! m_services[i].equals( t.m_services[i] ) )
            {
                return false;
            }
        }
        return true;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = m_descriptor.hashCode();
        if( m_graph != null )
        {
            hash ^= m_graph.hashCode();
        }
        hash ^= m_context.hashCode();
        if( m_configuration != null )
        {
            hash ^= m_configuration.hashCode();
        }
        for( int i = 0; i < m_parts.length; i++ )
        {
            hash ^= m_parts[i].hashCode();
            hash = hash - 163611323;
        }
        for( int i = 0; i < m_services.length; i++ )
        {
            hash ^= m_services[i].hashCode();
            hash = hash - 163611323;
        }
        for( int i = 0; i < m_loggers.length; i++ )
        {
            hash ^= m_loggers[i].hashCode();
            hash = hash + 471312761;
        }
        return hash;
    }
}
