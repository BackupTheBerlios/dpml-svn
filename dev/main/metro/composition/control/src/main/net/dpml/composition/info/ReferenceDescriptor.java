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

/**
 * This reference defines the type of interface required
 * by a component. The type corresponds to the class name of the
 * interface implemented by component. Associated with each
 * classname is a version object so that different versions of same
 * interface can be represented.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ReferenceDescriptor.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public final class ReferenceDescriptor
        implements Serializable
{
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
     * parsed for a classname and version in the form [classname]:[version].
     * If not version is present a default 1.0.0 version will be assigned.
     *
     * @param type the service type spec
     * @exception NullPointerException if the classname is null
     */
    public ReferenceDescriptor( final String type ) throws NullPointerException
    {
        this( parseClassname(type), parseVersion(type) );
    }

    /**
     * Construct a service with specified name, version and attributes.
     *
     * @param classname the name of the service
     * @param version the version of service
     * @exception NullPointerException if the classname or version is null
     * @exception IllegalArgumentException if the classname string is invalid
     */
    public ReferenceDescriptor( final String classname,
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
        if( classname.indexOf( "/" ) > -1 )
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
     * Return classname of interface this reference refers to.
     *
     * @return the classname of the Service
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the version of interface.
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
    public boolean matches( final ReferenceDescriptor other )
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
        boolean match = false;

        //
        // TODO: check validity of the following - this is
        // assuming the equality is equivalent to compliance
        // which is not true
        //

        if ( other instanceof ReferenceDescriptor )
        {
            match = ( (ReferenceDescriptor) other ).matches( this );
        }
        else if ( other instanceof Service )
        {
            match = ( (Service) other ).matches( this );
        }
        else if ( other instanceof ServiceDescriptor )
        {
            match = ( (ServiceDescriptor) other ).getReference().matches( this );
        }

        return match;
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

        int index = type.indexOf( ":" );
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
        if( type.indexOf( ":" ) == -1 )
        {
            return Version.getVersion( "1.0" );
        }
        else
        {
            return Version.getVersion( type.substring( getColonIndex( type ) + 1) );
        }
    }

    private static final int getColonIndex( final String type )
        throws NullPointerException 
    {
        if ( null == type )
        {
            throw new NullPointerException( "type" );
        }

        return Math.min( type.length(), Math.max( 0, type.indexOf( ":" ) ) );
    }
}
