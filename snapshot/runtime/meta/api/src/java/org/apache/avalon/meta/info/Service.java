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
 * This class contains the meta information about a particular
 * service. It contains a set of attributes qualifying the service;
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Service.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Service extends Descriptor
{
    //=========================================================================
    // state
    //=========================================================================

    /**
     * The service reference.
     */
    private final ReferenceDescriptor m_reference;

    /**
     * The optional context entry criteria.
     */
    private final EntryDescriptor[] m_entries;

    //=========================================================================
    // constructor
    //=========================================================================

    /**
     * Creation of a new Service instance using a classname and
     * supplied properties argument.
     *
     * @param reference the versioned classname
     */
    public Service( final ReferenceDescriptor reference )
    {
        this( reference, null, null );
    }

    /**
     * Creation of a new Service instance using a classname and
     * supplied properties argument.
     *
     * @param reference the versioned classname
     * @param entries the set of attributes to assign to the descriptor
     */
    public Service(
            final ReferenceDescriptor reference,
            final EntryDescriptor[] entries )
    {
        this( reference, entries, null );
    }

    /**
     * Creation of a new Service instance using a classname and
     * supplied properties argument.
     *
     * @param reference the versioned classname
     * @param attributes the set of attributes to assign to the descriptor
     */
    public Service(
            final ReferenceDescriptor reference,
            final Properties attributes )
    {
        this( reference, null, attributes );
    }

    /**
     * Creation of a new Service instance using a classname and
     * supplied properties argument.
     *
     * @param reference the versioned classname
     * @param entries the set of optional context entries
     * @param attributes the set of attributes to assign to the descriptor
     */
    public Service(
            final ReferenceDescriptor reference,
            final EntryDescriptor[] entries,
            final Properties attributes )
    {
        super( attributes );
        if ( reference == null )
        {
            throw new NullPointerException( "reference" );
        }
        m_reference = reference;
        if ( entries == null )
        {
            m_entries = new EntryDescriptor[0];
        }
        else
        {
            m_entries = entries;
        }
    }

    //=========================================================================
    // implementation
    //=========================================================================

    /**
     * Return the service classname key.
     * @return the service classname
     */
    public String getClassname()
    {
        return m_reference.getClassname();
    }

    /**
     * Return the service version.
     * @return the version
     */
    public Version getVersion()
    {
        return m_reference.getVersion();
    }

    /**
     * Return the service reference.
     * @return the reference
     */
    public ReferenceDescriptor getReference()
    {
        return m_reference;
    }

    /**
     * Return the entries declared by the service.
     *
     * @return the entry descriptors
     */
    public EntryDescriptor[] getEntries()
    {
        return m_entries;
    }

    /**
     * Determine if supplied reference will match this service.
     * To match a service has to have same classname and must comply with version.
     *
     * @param reference the reference descriptor
     * @return true if matches, false otherwise
     */
    public boolean matches( final ReferenceDescriptor reference )
    {
        return m_reference.matches( reference );
    }

    /**
     * Return the hashcode for this service defintion.
     * @return the hashcode value
     */
    public int hashCode()
    {
        return m_reference.hashCode();
    }

    /**
     * Compare this object to the supplied object for equality.
     * @param other the object to compare to this object
     * @return true if this object matches the supplied object
     *    in terms of service classname and version
     */
    public boolean equals( Object other )
    {
        boolean match = false;

        if ( other instanceof ReferenceDescriptor )
        {
            match = matches( (ReferenceDescriptor) other );
        }
        else if ( other instanceof Service )
        {
            Service ref = (Service) other;
            match = ref.getClassname().equals( getClassname() )
                    && ref.getVersion().complies( getVersion() );
        }

        return match;
    }

    /**
     * Returns a string representation of the service.
     * @return a string representation
     */
    public String toString()
    {
        return getReference().toString();
    }

}
