/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.component.info;

import java.io.Serializable;

/**
 * This ServiceDescriptor defines the interface and service version
 * published by a service instance.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ServiceDescriptor implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * The name of service class.
     */
    private final String m_classname;

    /**
     * The version of service class.
     */
    private final Version m_version;

    /**
     * Construct a service with specified type. The type argument will be
     * parsed for a classname and version in the form [classname]#[version].
     * If not version is present a default 1.0.0 version will be assigned.
     *
     * @param spec the service specification
     * @exception NullPointerException if the spec is null
     */
    public ServiceDescriptor( final String spec ) throws NullPointerException
    {
        this( parseClassname( spec ), parseVersion( spec ) );
    }

    /**
     * Construct a service with specified name, version.
     *
     * @param classname the name of the service
     * @param version the version of service
     * @exception NullPointerException if the classname or version is null
     * @exception IllegalArgumentException if the classname string is invalid
     */
    public ServiceDescriptor( final String classname,
                                final Version version )
        throws NullPointerException
    {
        if ( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( classname.equals( "" ) )
        {
            throw new NullPointerException( "classname" );
        }

        m_classname = classname;

        if ( null == version )
        {
            m_version = Version.getVersion( "" );
        }
        else
        {
            m_version = version;
        }
    }

    /**
     * Return classname of service specification.
     *
     * @return the classname of the service specification
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the service version.
     *
     * @return the version of interface
     */
    public Version getVersion()
    {
        return m_version;
    }

    /**
     * Determine if specified service will match this service.
     * To match a service has to have same name and must comply with version.
     *
     * @param other the other ServiceInfo
     * @return true if matches, false otherwise
     */
    public boolean matches( final ServiceDescriptor other )
    {
        if( !m_classname.equals( other.m_classname ) ) return false;
        if( other.getVersion().complies( getVersion() ) ) return true;
        return false;
    }

    /**
     * Convert to a string of format name:version
     *
     * @return string describing service
     */
    public String toString()
    {
        return getClassname() + ":" + getVersion();
    }

    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object is a reference, service, or service
     *   descriptor that matches this objct in terms of classname and version
     */
    public boolean equals( Object other )
    {
        if( false == other instanceof ServiceDescriptor )
        {
            return false;
        }

        ServiceDescriptor service = (ServiceDescriptor) other;
        if( false == getClassname().equals( service.getClassname() ) )
        {
            return false;
        }
        else if( false == getVersion().equals( service.getVersion() ) )
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Returns the cashcode.
     * @return the hascode value
     */
    public int hashCode()
    {
        return getClassname().hashCode() ^ getVersion().hashCode();
    }

    private static final String parseClassname( final String type )
        throws NullPointerException
    {
        if( type == null )
        {
            throw new NullPointerException( "type" );
        }

        int index = type.indexOf( "#" );
        if( index == -1 )
        {
            return type;
        }
        else
        {
            return type.substring( 0, index );
        }
    }

    private static final Version parseVersion( final String type )
    {
        int index = type.indexOf( "#" );
        if( index == -1 )
        {
            return Version.getVersion( "1" );
        }
        else
        {
            String value = type.substring( index + 1 );
            return Version.getVersion( value );
        }
    }
}
