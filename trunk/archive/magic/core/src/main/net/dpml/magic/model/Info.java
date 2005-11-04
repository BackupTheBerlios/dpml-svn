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

package net.dpml.magic.model;

import java.net.URISyntaxException;

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.Artifact;

/**
 * Info datastructure containing the group, name and version of a resource.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public final class Info
{
   /**
    * The static immutable value of the artifact protocol.
    */
    public static final String PROTOCOL = "artifact";

   /**
    * Creation of a new info instance relative to a supplied home
    * and artifact specification.
    * @param id the artifact identifier
    * @return the immutable info descriptor
    * @exception URISyntaxException if an artifact uri cannot be constructed
    */
    public static Info create( final String id ) throws URISyntaxException
    {
        Artifact artifact = Artifact.createArtifact( id );
        final String group = artifact.getGroup();
        final String name = artifact.getName();
        final String version = artifact.getVersion();
        final String type = artifact.getType();
        return create( group, name, version, new Type[]{new Type( type )} );
    }

   /**
    * Creation of a new info instance relative to a supplied set of parameters.
    * @param group the artifact group
    * @param name the artifact name
    * @param version the artifact version
    * @param types the artifact types
    * @return the immutable info descriptor
    */
    public static Info create(
      final String group, final String name, final String version, Type[] types )
    {
        return new Info( group, name, version, types );
    }

    private final String m_name;
    private final String m_group;
    private final String m_version;
    private final Type[] m_types;

    private Info( String group, String name, String version, Type[] types )
    {
        assertNotNull( "group", group );
        assertNotNull( "name", name );

        m_group = group;
        m_name = name;
        m_version = version;
        m_types = types;

        if( m_types.length < 1 )
        {
            final String error =
              "At least one type is required.";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Return the name of the artifact group.
    * @return the group name
    */
    public String getGroup()
    {
        return m_group;
    }

   /**
    * Return the name of the artifact.
    * @return the artifact name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the version identifier.
    *
    * @return a string identifying the build version.
    */
    public String getVersion()
    {
        return m_version;
    }

   /**
    * Return a type identified by name.
    * @param name the name of the type
    * @return the artifact type
    */
    public Type getType( String name )
    {
        for( int i=0; i < m_types.length; i++ )
        {
            Type type = m_types[i];
            if( type.getName().equals( name ) )
            {
                return type;
            }
        }
        throw new IllegalArgumentException( "Invalid type argument [" + name + "]." );
    }

   /**
    * Return the array of the aritfact types.
    * @return the artifact types
    */
    public Type[] getTypes()
    {
        return m_types;
    }

   /**
    * Return a string array identifying the aritfact types.
    * @param type the type
    * @return TRUE if this instance matches the supplied type
    */
    public boolean isa( String type )
    {
        for( int i=0; i < m_types.length; i++ )
        {
            if( m_types[i].getName().equals( type ) )
            {
                return true;
            }
        }
        return false;
    }

   /**
    * Return the type at the supplied index position.
    * @param index the index position of the type
    * @return the artifact type
    */
    public Type getType( int index )
    {
        return m_types[ index ];
    }

   /**
    * Return the link filename for a given type.
    * @param type the type identifier
    * @return the link filename
    */
    public String getLinkFilename( Type type )
    {
        final StringBuffer buffer = new StringBuffer( getName() );
        if( !"".equals( type.getAlias() ) )
        {
            buffer.append( "-" );
            buffer.append( type.getAlias() );
        }
        String name = type.getName();
        buffer.append( "." );
        buffer.append( name );
        buffer.append( ".link" );
        return buffer.toString();
    }

   /**
    * Return a string corresponding to the name and version of this artifact
    * without type information.
    * @return the artifact short name
    */
    public String getShortFilename()
    {
        final StringBuffer buffer = new StringBuffer( getName() );
        if( null != getVersion() )
        {
            buffer.append( "-" );
            buffer.append( getVersion() );
        }
        return buffer.toString();
    }

   /**
    * Return the full filename of the artifact. The value returned is in the form
    * [name]-[version].[type] or in the case of a null version [name].[type].
    *
    * @param type the resource type
    * @return the artifact filename
    */
    public String getFilename( String type )
    {
        final String shortFilename = getShortFilename();
        final StringBuffer buffer = new StringBuffer( shortFilename );
        buffer.append( "." );
        buffer.append( type );
        return buffer.toString();
    }

   /**
    * Return the path to the artifact.  The path is returned in the
    * form [group]/[type]s/[filename].
    * @param type the resource type
    * @return the artifact relative path
    */
    public String getPath( String type )
    {
        final StringBuffer buffer = new StringBuffer( getGroup() );
        buffer.append( "/" );
        buffer.append( type );
        buffer.append( "s/" );
        final String filename = getFilename( type );
        buffer.append( filename );
        return buffer.toString();
    }

   /**
    * Return the path to the artifact.  The path is returned in the
    * form [group]/[type]s/[filename].
    * @param type the resource type
    * @return the artifact relative path
    */
    public String getPath( Type type )
    {
        return getPath( type, false );
    }

   /**
    * Return the path to the artifact.  The path is returned in the
    * form [group]/[type]s/[filename].
    * @param type the resource type
    * @param link if TRUE return the link filename
    * @return the artifact relative path
    */
    public String getPath( Type type, boolean link )
    {
        final StringBuffer buffer = new StringBuffer( getGroup() );
        buffer.append( "/" );
        buffer.append( type.getName() );
        buffer.append( "s/" );
        if( link )
        {
            final String filename = getLinkFilename( type );
            buffer.append( filename );
        }
        else
        {
            final String filename = getFilename( type.getName() );
            buffer.append( filename );
        }
        return buffer.toString();
    }

   /**
    * Return the artifact uri. The path is returned in the form "artifact:[type]:[spec].
    * @param type the resource type
    * @return the artifact uri
    */
    public String getURI( String type )
    {
        return getURI( type, null );
    }

   /**
    * Return the artifact uri. The path is returned in the form "artifact:[type]:[spec].
    * @param name the name
    * @param alias the link alias
    * @return the artifact uri
    */
    public String getURI( String name, String alias )
    {
        if( null == alias )
        {
            final StringBuffer buffer = new StringBuffer( PROTOCOL );
            buffer.append( ":" );
            buffer.append( name );
            buffer.append( ":" );
            buffer.append( getSpec() );
            return buffer.toString();
        }
        else
        {
            final StringBuffer buffer = new StringBuffer( "link" );
            buffer.append( ":" );
            buffer.append( name );
            buffer.append( ":" );
            buffer.append( m_group );
            buffer.append( "/" );
            buffer.append( getName() );
            Type type = getType( name );
            String version = type.getAlias();
            if( null != version )
            {
                buffer.append( "#" );
                buffer.append( version );
            }
            return buffer.toString();
        }
    }

   /**
    * Return the artifact specification. The path is retured in the form
    * [group]/[name]#[version].
    * @return the artifact spec
    */
    public String getSpec()
    {
        return getSpecification( "/", "#" );
    }

   /**
    * Return the doc path. The path is retured in the form
    * [group]/[name]/[version].
    * @return the artifact spec
    */
    public String getDocPath()
    {
        return getSpecification( "/", "/" );
    }

   /**
    * Return the api javadoc path. The path is retured in the form
    * /api/[group]/[version] for a module and /api/[group]/[name]/[version]
    * for other types.
    *
    * @return the javadoc api path
    */
    public String getJavadocPath()
    {
        StringBuffer buffer = new StringBuffer( "/api/" );
        buffer.append( getGroup() );
        if( !isa( "module" ) )
        {
            buffer.append( "/" + getName() );
        }
        if( getVersion() != null )
        {
            buffer.append( "/" + getVersion() );
        }
        return buffer.toString();
    }

   /**
    * Return the artifact specification using the supplied group and
    * version separators.
    * @param groupSeparator the group separator
    * @param versionSeparator the version separator
    * @return a derived specification
    */
    public String getSpecification(
      String groupSeparator, String versionSeparator )
    {
        final StringBuffer buffer = new StringBuffer( getGroup() );
        buffer.append( groupSeparator );
        buffer.append( getName() );
        if( ( null != m_version ) && !"".equals( m_version ) )
        {
            buffer.append( versionSeparator );
            buffer.append( getVersion() );
        }
        return buffer.toString();
    }

   /**
    * Return the string representation of this info instance.
    * @return a string representation
    */
    public String toString()
    {
        return getGroup() + "/" + getName();
    }

   /**
    * Return true if this info instance is equal to the supplied object.
    * @param other the object to compare against this instance
    * @return TRUE if equal
    */
    public boolean equals( final Object other )
    {
        if( !( other instanceof Info ) )
        {
            return false;
        }

        final Info info = (Info) other;
        if( !getName().equals( info.getName() ) )
        {
            return false;
        }
        if( !getGroup().equals( info.getGroup() ) )
        {
            return false;
        }

        // do not evaluate type equivelence

        if( null == getVersion() )
        {
            return ( null == info.getVersion() );
        }
        else
        {
            return getVersion().equals( info.getVersion() );
        }
    }

   /**
    * Return the hashcode for the info instance.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash;
        if( m_version == null )
        {
            hash = NULL_VERSION_HASH;
        }
        else
        {
            hash = getVersion().hashCode();
        }

        hash = hash ^ m_name.hashCode();
        hash = hash ^ m_group.hashCode();

        for( int i=0; i < m_types.length; i++ )
        {
            hash = hash ^ m_types[i].hashCode();
        }

        return hash;
    }

    //-------------------------------------------------------------------
    // internal
    //-------------------------------------------------------------------

    private void assertNotNull( final String key, final Object object )
        throws NullArgumentException
    {
        if( null == object )
        {
            throw new NullArgumentException( key );
        }
    }

    private static final int NULL_VERSION_HASH = 72367861;
}
