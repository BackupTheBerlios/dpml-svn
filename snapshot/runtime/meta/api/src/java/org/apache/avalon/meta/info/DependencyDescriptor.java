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

package org.apache.avalon.meta.info;

import java.util.Properties;

import org.apache.avalon.framework.Version;

/**
 * A descriptor that describes dependency information for
 * a particular Component. This class contains information
 * about;
 * <ul>
 *   <li>role: the name component uses to look up dependency</li>
 *   <li>service: the class/interface that the dependency must provide</li>
 * </ul>
 *
 * <p>Also associated with each dependency is a set of arbitrary
 * attributes that can be used to store extra information
 * about dependency. See {@link InfoDescriptor} for example
 * of how to declare the container specific attributes.</p>
 *
 * <p>Possible uses for the attributes are to declare container
 * specific constraints of component. For example a dependency on
 * a Corba ORB may also require that the Corba ORB contain the
 * TimeServer and PersistenceStateService at initialization. Or it
 * may require that the componenet be multi-thread safe or that
 * it is persistent etc. These are all container specific
 * demands.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DependencyDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class DependencyDescriptor extends Descriptor
{

    private static final Version DEFAULT_VERSION = Version.getVersion( "1.0" );

    /**
     * The name the component uses to lookup dependency.
     */
    private final int m_position;

    /**
     * The name the component uses to lookup dependency.
     */
    private final String m_key;

    /**
     * The service class/interface that the dependency must provide.
     */
    private final ReferenceDescriptor m_service;

    /**
     * True if dependency is optional, false otherwise.
     */
    private final boolean m_optional;

    /**
     * Creation of a new dependency descriptor using the default 1.0 version
     * @param role the role name that will be used by the type when looking up a service
     * @param service the interface service
     */
    public DependencyDescriptor( final String role, String service )
    {
        this( role, service, DEFAULT_VERSION );
    }

    /**
     * Creation of a new dependency descriptor.
     * @param role the role name that will be used by the type when looking up a service
     * @param service the version insterface service reference
     */
    public DependencyDescriptor( final String role, String service, Version version )
    {
        this( role, new ReferenceDescriptor( service, version ), false, null );
    }

    /**
     * Creation of a new dependency descriptor.
     * @param role the role name that will be used by the type when looking up a service
     * @param service the version insterface service reference
     */
    public DependencyDescriptor( final String role,
                                 final ReferenceDescriptor service )
    {
        this( role, service, false, null );
    }

    /**
     * Creation of a new dependency descriptor.
     * @param role the role name that will be used by the type when looking up a service
     * @param service the version insterface service reference
     * @param optional TRUE if this depedency is optional
     * @param attributes a set of attributes to associate with the dependency
     */
    public DependencyDescriptor( final String role,
                                 final ReferenceDescriptor service,
                                 final boolean optional,
                                 final Properties attributes )
    {
        this( role, service, optional, attributes, -1 );
    }

    /**
     * Creation of a new dependency descriptor.
     * @param role the role name that will be used by the type when looking up a service
     * @param service the version insterface service reference
     * @param optional TRUE if this depedency is optional
     * @param attributes a set of attributes to associate with the dependency
     * @param position constructor position
     */
    public DependencyDescriptor( final String role,
                                 final ReferenceDescriptor service,
                                 final boolean optional,
                                 final Properties attributes, 
                                 final int position )
    {
        super( attributes );

        if ( null == role )
        {
            throw new NullPointerException( "role" );
        }

        if ( null == service )
        {
            throw new NullPointerException( "service" );
        }

        m_key = role;
        m_service = service;
        m_optional = optional;
        m_position = position;
    }

    /**
     * Return the name the component uses to lookup the dependency.
     *
     * @return the name the component uses to lookup the dependency.
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Return the service class/interface descriptor that describes the
     * dependency that the provider provides.
     *
     * @return a reference to service reference that describes the fulfillment
     *  obligations that must be met by a service provider.
     * @deprecated use getReference()
     */
    public ReferenceDescriptor getService()
    {
        return m_service;
    }

    /**
     * Return the service class/interface descriptor that describes the
     * dependency must fulfilled by a provider.
     *
     * @return a reference to service reference that describes the fulfillment
     *  obligations that must be met by a service provider.
     */
    public ReferenceDescriptor getReference()
    {
        return m_service;
    }

    /**
     * Return true if dependency is optional, false otherwise.
     *
     * @return true if dependency is optional, false otherwise.
     */
    public boolean isOptional()
    {
        return m_optional;
    }

    /**
     * Return true if dependency is required, false otherwise.
     *
     * @return true if dependency is required, false otherwise.
     */
    public boolean isRequired()
    {
        return !isOptional();
    }

    /**
     * Return the constructor position.
     *
     * @return -1 if not constructor else the value 
     *   indicates the n'th public constructor
     */
    public int getPosition()
    {
        return m_position;
    }

    public String toString()
    {
        return "[" + getKey() + "] " + getReference();
    }

    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object is a reference, service, or service
     *   descriptor that matches this objct in terms of classname and version
     */
    public boolean equals( Object other )
    {
        boolean isEqual = super.equals( other ) && other instanceof DependencyDescriptor;
        if ( other instanceof DependencyDescriptor )
        {
            DependencyDescriptor dep = (DependencyDescriptor) other;

            isEqual = isEqual && m_optional == dep.m_optional;
            isEqual = isEqual && m_service.equals( dep.m_service );
            isEqual = isEqual && m_position == dep.m_position;
        }

        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash >>>= 13;
        hash ^= m_service.hashCode();
        hash >>>= ( m_optional ) ? 1 : 0;
        hash >>>= m_position;

        return hash;
    }

}
