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

package org.apache.avalon.repository ;


import java.io.Serializable;
import java.io.IOException;
import java.net.URL;
import java.lang.Comparable;


/**
 * Defintion of a artifact that maintains a relative url
 * to some nominally remote file together with a set of assigned
 * properties.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Artifact.java 46177 2004-09-16 13:33:18Z niclas $
 */
public class Artifact implements Serializable, Comparable
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    public static final String SEP = "/";

    public static final String GROUP_KEY = "avalon.artifact.group";
    public static final String NAME_KEY = "avalon.artifact.name";
    public static final String VERSION_KEY = "avalon.artifact.version";
    public static final String TYPE_KEY = "avalon.artifact.type";

    public static Artifact createArtifact( String spec )
    {
        if ( null == spec ) throw new NullPointerException( "spec" );

        if( spec.startsWith( "artifact:" ) )
        {
            try
            {
                URL url = new URL( null, spec, new ArtifactHandler() );
                Artifact artifact = (Artifact) url.getContent();
                return artifact;
            }
            catch( IOException e )
            {
                final String error = 
                  "Bad artifact url [" + spec + "] " 
                  + e.getMessage();
                throw new IllegalArgumentException( error );
            }
        }
        else
        {
            String version = getVersion( spec );
            String group = getGroup( spec );
            String name = getName( spec );
            return createArtifact( group, name, version );
        }
    }

    public static Artifact createArtifact( 
      String group, String name, String version )
    {
        return createArtifact( group, name, version, "jar" );
    }

    public static Artifact createArtifact( 
      String group, String name, String version, String type )
    {
        if( group == null ) throw new NullPointerException( "group" );
        if( name == null ) throw new NullPointerException( "name" );

        return new Artifact( group, name, version, type );
    }

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /** 
    * the artifact base path 
    */
    private final String m_base;

   /** 
    * the name of the the artifact relative to the base 
    */
    private final String m_filename;

   /** 
    * the computed path
    */
    private final String m_path;

   /** 
    * The artifact group. 
    */
    private final String m_group;

   /** 
    * The artifact name. 
    */
    private final String m_name;

   /** 
    * The artifact type.
    */
    private final String m_type;

   /** 
    * The artifact version.
    */
    private final String m_version;


    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------
    
    /**
     * Creation of a new classified artifact.
     *
     * @param group the artifact group 
     * @param name the artifact name
     * @param type the artifact type
     * @param version the artifact version
     */
    private Artifact( 
      final String group, final String name, 
      final String version, final String type ) 
    {
        m_group = group;
        m_name = name;
        m_version = version;
        m_type = type;

        String base = createBase( group, type );
        m_base = getCleanPath( base );

        String filename = createFilename( name, version, type );
        if( filename.indexOf( SEP ) > 0 )
        {
            final String error = 
              "Invalid name - illegal character '/' in filename: " + filename;
            throw new IllegalArgumentException( error );
        }

        m_filename = filename;
        m_path = m_base + SEP + m_filename;
    }

    /**
     * Gets the relative artifact URL.
     * 
     * @return the relative url
     */
    public String getURL()
    {
        return getURL( null );
    }

    /**
     * Gets the URL to the artifact given a base URL for a 
     * remote repository.
     * 
     * @param host the base repository URL
     * @return the full URL to the artifact
     */
    public String getURL( final String host )
    {
        if( null == host )
        {
            return getURL( "" );
        }
        else
        {
            if( host.endsWith( SEP ) )
            {
                return host + getPath();
            }
            else
            {
                return host + SEP + getPath();
            }
        }
    }

    
    // ------------------------------------------------------------------------
    // accessors
    // ------------------------------------------------------------------------

   /**
    * Return the group identifier for this artifact.
    * @return the group
    */ 
    public String getGroup()
    {
        return m_group;
    }

   /**
    * Return the name of this artifact.
    * @return the name
    */ 
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the artifact type.
    * @return the type
    */ 
    public String getType()
    {
        return m_type;
    }

   /**
    * Return the artifact version.
    * @return the version
    */ 
    public String getVersion()
    {
        return m_version;
    }

    /**
     * Gets the artifact specification for this Artifact
     * in the form [group]/[name]#[version].
     * 
     * @return the artifact specification
     */
    public String getSpecification()
    {
        final String group = getGroup();
        final String name = getName();

        StringBuffer buffer = new StringBuffer() ;
        buffer.append( group );
        buffer.append( SEP );
        buffer.append( name );
        
        String version = getVersion();
        if( version != null )
        {
            buffer.append( '#' ) ;
            buffer.append( version ) ;
        }

        return buffer.toString() ;
    }

    /**
     * Return the base path to the artifact. This is equivelent to the 
     * a logic directory path without a leading or trailing seperator.
     *
     * @return the base path.
     */
    public String getBase()
    {
        return m_base;
    }
    
    /**
     * Return the filename of the artifact.
     * @return the name.
     */
    public String getFilename()
    {
        return m_filename ;
    }
    
    /**
     * Gets the artifact path.  The value returned is equal to 
     * the base path, seperator and filename.
     *
     * @return the artifact path
     */
    public String getPath()
    {
        return m_path;
    }

   /**
    * Return a stringified representation of the instance.
    * @return the string representation
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "artifact:" );
        buffer.append( getType() );
        buffer.append( ":" );
        buffer.append( getSpecification() );
        return buffer.toString();
    }

    // ------------------------------------------------------------------------
    // static private
    // ------------------------------------------------------------------------

    private static String createBase( String group, String type )
    {
        if( type == null ) 
            return group;
        return group + Artifact.SEP + type + "s";
    }

    private static String createFilename( String name, String version, String type )
    {
        if( name == null ) throw new NullPointerException( "name" );

        StringBuffer buffer = new StringBuffer( name );
        if( version != null )
        {
            buffer.append( "-" );
            buffer.append( version );
        }
        if( type != null )
        {
            buffer.append( "." );
            buffer.append( type );
        }
        return buffer.toString();
    }

    private static String getGroup( String spec )
    {
        int semiColon = spec.indexOf( ';' ) ;
        if ( -1 == semiColon )
        {
            int colon = spec.indexOf( ':' ) ;
            if( -1 == colon ) return spec;
            return spec.substring( 0, colon ); 
        }
        else
        {
            return getGroup( spec.substring( 0, semiColon-1 ) );
        }
    }

    private static String getName( String spec )
    {
        int semiColon = spec.indexOf( ';' ) ;
        if ( -1 == semiColon )
        {
            int colon = spec.indexOf( ':' ) ;
            if( -1 == colon ) return spec;
            return spec.substring( colon+1, spec.length() ); 
        }
        else
        {
            return getName( spec.substring( 0, semiColon ) );
        }
    }

    private static String getVersion( String spec )
    {
        int semiColon = spec.indexOf( ';' ) ;
        if ( -1 == semiColon )
        {
            return null;
        }
        else
        {
            return spec.substring( semiColon+1, spec.length() );
        }
    }

   /**
    * Remove leading and trailing seperators.
    * @param the path value to clean
    * @return the clean path
    */
    private String getCleanPath( final String path )
    {
        if( path.startsWith( SEP ) ) return getCleanPath( path.substring( 1, path.length() ) );
        if( path.endsWith( SEP ) ) return getCleanPath( path.substring( 0, path.length() -1 ) );
        return path;
    }

    public int compareTo( Object object )
    {
        String name = this.toString();
        String other = object.toString();
        return name.compareTo( other );
    }

}

