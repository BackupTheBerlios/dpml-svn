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

package org.apache.avalon.tools.model;

import org.apache.tools.ant.BuildException;

/**
 * Project info.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Info 
{
   /**
    * The static immutable element value to declare a SNAPSHOT artifact.
    */
    public static final String SNAPSHOT = "SNAPSHOT";
 
   /**
    * The static immutable value of the artifact protocol.
    */
    public static final String PROTOCOL = "artifact";

   /**
    * Creation of a new info instance relative to a supplied home 
    * and artifact specification.
    * @param home the home
    * @param id the artiact identifier
    * @return the immutable info descriptor
    */
    public static Info create( Home home, final String id )
    {
        final int i = id.indexOf( ":" );
        if( i<0 )
        {
            final String error =
              "Missing protocol in id [" + id + "]";
            throw new BuildException( error );
        }
        final String protocol = id.substring( 0, i );
        final String spec = id.substring( i+1 );
        return Info.create( home, protocol, spec );
    }

   /**
    * Creation of a new info instance relative to a supplied home, 
    * type, and artifact specification.
    * @param home the home
    * @param type the artifact type
    * @param id the artiact identifier
    * @return the immutable info descriptor
    */
    public static Info create( Home home, final String type, final String id )
    {
        final int n = getGroupIndex( id );
        final String group = getGroupFromId( id, n );
        final String name = getNameFromId( id, n );
        final String version = getVersionFromId( id );
        return Info.create( 
          home, group, name, version, type, 
          SNAPSHOT.equalsIgnoreCase( version ) );
    }

   /**
    * Creation of a new info instance relative to a supplied set of parameters.
    * @param home the home
    * @param group the artifact group
    * @param name the artifact name
    * @param version the artifact version
    * @param type the artifact type
    * @param snapshot the artiact snapshot status
    * @return the immutable info descriptor
    */
    public static Info create(
      final Home home, final String group, final String name, final String version, 
      final String type, boolean snapshot )
    {
        if( home.isGump() )
        {
            final String signature = home.getGumpSignature();
            return new Info( group, name, signature, type, false );
        }
        else
        {
            return new Info( group, name, version, type, snapshot );
        }
    }

    private String m_name;
    private String m_group;
    private String m_version;
    private String m_type;
    private boolean m_snapshot;

    private Info( 
      final String group, final String name, final String version, 
      final String type, boolean snapshot )
    {
        assertNotNull( "group", group );
        assertNotNull( "name", name );

        m_group = group;
        m_name = name;
        m_version = version;
        m_type = type;
        m_snapshot = snapshot;
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
    * Return the version identifier. If the build policy is SNAPSHOT
    * the version value returned is replaced with "SNAPSHOT".
    *
    * @return a string identifying the build version. 
    */
    public String getVersion()
    {
        if( isaSnapshot() )
        {
            return "SNAPSHOT";
        }
        else
        {
            return m_version;
        }
    }

   /**
    * Return the snapshot staus of this artifact.
    * @return true if this artifact is marked as a snapshot
    */
    public boolean isaSnapshot()
    {
        return m_snapshot;
    }

   /**
    * Return a string identifying the aritfact type.
    * @return the artifact type
    */
    public String getType()
    {
        if( null == m_type )
        {   
            return "jar";
        }
        else
        {
            return m_type;
        } 
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
    * @return the artifact filename
    */
    public String getFilename()
    {
        final String shortFilename = getShortFilename();
        final StringBuffer buffer = new StringBuffer( shortFilename );
        buffer.append( "." );
        buffer.append( getType() );
        return buffer.toString();
    }

   /**
    * Return the path to the artifact.  The path is returned in the 
    * form [group]/[type]s/[filename].
    * @return the artifact relative path
    */
    public String getPath()
    {
        final String filename = getFilename();
        final StringBuffer buffer = new StringBuffer( getGroup() );
        buffer.append( "/" );
        buffer.append( getType() );
        buffer.append( "s/" );
        buffer.append( filename );
        return buffer.toString();
    }

   /**
    * Return the artifact uri. The path is returned in the form "artifact:[type]:[spec].
    * @return the artifact uri
    */
    public String getURI()
    {
        final StringBuffer buffer = new StringBuffer( PROTOCOL );
        buffer.append( ":" );
        buffer.append( getType() );
        buffer.append( ":" );
        buffer.append( getSpec() );
        return buffer.toString();
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
        if(( null != m_version ) && !"".equals( m_version ))
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
        return getURI();
    }

   /**
    * Return true if this info instance is equal to the supplied object.
    * @param other the object to compare against this instance
    * @return TRUE if equal
    */
    public boolean equals( final Object other )
    {
        if( ! ( other instanceof Info ) )
            return false;
            
        final Info info = (Info) other;
        if( isaSnapshot() != info.isaSnapshot() ) 
            return false;
        if( ! getName().equals( info.getName() ) ) 
            return false;
        if( ! getGroup().equals( info.getGroup() ) ) 
            return false;
        if( ! getType().equals( info.getType() ) ) 
            return false;
            
        if( null == getVersion() ) 
        {
            return ( null == info.getVersion() );
        }
        else
        {
            return getVersion().equals( info.getVersion() );
        }
    }
    
    public int hashCode()
    {

        int hash;
        if( m_version == null )
            hash = 72367861;
        else
            hash = getVersion().hashCode();
            
        if( isaSnapshot() )
            hash >>>= 7;
        else
            hash >>>= 13;
        
        hash = hash ^ m_name.hashCode();
        hash = hash ^ m_group.hashCode();
        hash = hash ^ m_type.hashCode();
        
        return hash;
    }

    //-------------------------------------------------------------------
    // internal
    //-------------------------------------------------------------------

    private void assertNotNull( final String key, final Object object )
    {
        if( null == object ) throw new NullPointerException( key );
    }

    private static int getGroupIndex( final String id )
    {
        final int n = id.lastIndexOf( "/" );
        if( n < 0 )
        {
            final String error = 
              "Invalid resource identifier \"" + id + "\". "
              + "A resource identifier must be in for for [group]/[name]#[version]";
            throw new BuildException( error );
        }
        else
        {
            return n;
        }
    }

    private static String getGroupFromId( final String id, final int n )
    {
        return id.substring( 0, n );
    }

    private static String getNameFromId( final String id, final int n )
    {
        final int j = id.indexOf( "#" );
        if( j < 0 )
        {
            return id.substring( n+1 );
        }
        else
        {
            return id.substring( n+1, j );
        }
    }

    private static String getVersionFromId( final String id )
    {
        final int j = id.indexOf( "#" );
        if( j < 0 )
        {
            return null;
        }
        else
        {
            return id.substring( j+1 );
        }
    }
}
