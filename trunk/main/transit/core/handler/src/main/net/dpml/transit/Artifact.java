/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit;

import java.io.Serializable;
import java.net.URLStreamHandler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A utility class the handles validation of <code>artifact</code> style uri
 * strings.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Artifact implements Serializable, Comparable
{
    static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    /**
     * Creation of a new artifact instance using a supplied uri specification.
     * An artifact uri contains the protocol identifier, a type, a group
     * designator, a name, and an optional version identifier.
     * <p>The following represent valid artifact uri examples:</p>
     *
     * <ul>
     * <li>artifact:jar:dpml/transit/dpml-transit-main#1234</li>
     * <li>artifact:jar:dpml/transit/dpml-transit-main</li>
     * <li>link:jar:dpml/transit/dpml-transit-main#1.0</li>
     * </ul>
     *
     * <p>
     *  If there is a internal reference identifier which is marked by the
     *  exclamation mark followed by slash (!/) it will be stripped. The
     *  version part can be either before or after such identifier. Example;
     *  <pre>
     *   artifact:war:jmx-html/jmx-html#1.3!/images/abc.png
     *   artifact:war:jmx-html/jmx-html!/images/abc.png#1.3
     *  </pre>
     *  The above uris will both be referencing
     *  <code>artifact:war:jmx-html/jmx-html#1.3</code>
     * </p>
     * @param uri the artifact uri
     * @return the new artifact
     * @exception java.net.URISyntaxException if the supplied uri is not valid.
     * @exception UnsupportedSchemeException if the URI does not have "artifact"
     *         or "link" as its <strong>scheme</strong>.
     */
    public static final Artifact createArtifact( String uri )
        throws URISyntaxException, UnsupportedSchemeException
    {
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }
        int asterix = uri.indexOf( "!" );
        if( asterix == -1 )
        {
            return createArtifact( new URI( uri ) );
        }
        else
        {
            String path = uri.substring( 0, asterix );
            int versionPos = uri.indexOf( "#" );
            if( versionPos < asterix )
            {
                return createArtifact( path );
            }
            else
            {
                path = path + uri.substring( versionPos );
                return createArtifact( path );
            }
        }
    }

    /**
     * Creation of a new artifact instance using a supplied uri specification. An
     * artifact uri contains the protocol identifier, an optional type, a group
     * designator, a name, and an optional version identifier.
     * <p>The following represent valid artifact uri examples:</p>
     *
     * <ul>
     * <li>artifact:jar:metro/cache/dpml-cache-main#1.0.0</li>
     * <li>artifact:metro/cache/dpml-cache-main#1.0.0</li>
     * <li>artifact:metro/cache/dpml-cache-main</li>
     * </ul>
     *
     * @param uri the artifact uri
     * @return the new artifact
     * @exception UnsupportedSchemeException if the URI does not have "artifact"
     *     or "link" as its <strong>scheme</strong>.
     */
    public static final Artifact createArtifact( URI uri )
        throws UnsupportedSchemeException
    {
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }
        String scheme = uri.getScheme();
        if( null == scheme )
        {
            final String error = 
              "URI does not declare a scheme: " + uri;
            throw new UnsupportedSchemeException( error );
        }
        if( !scheme.equals( "artifact" ) && !scheme.equals( "link" ) && !scheme.equals( "local" ) )
        {
            final String error = 
              "URI contains a scheme that is not recognized: " + uri;
            throw new UnsupportedSchemeException( error );
        }
        return new Artifact( uri );
    }

    /**
     * Creation of a new artifact instance using a supplied group, name,
     * version and type arguments.
     *
     * @param group the artifact group identifier
     * @param name the artifact name
     * @param version the version
     * @param type the type
     * @return the new artifact
     * @exception NullArgumentException if any of the <code>group</code>,
     *            <code>name</code> or <code>type</code> arguments are
     *            <code>null</code>.
     */
    public static Artifact createArtifact( String group, String name, String version, String type )
        throws NullArgumentException
    {
        if( group == null )
        {
            throw new NullArgumentException( "group" );
        }
        if( name == null )
        {
            throw new NullArgumentException( "name" );
        }
        if( type == null )
        {
            throw new NullArgumentException( "type" );
        }
        if( version == null )
        {
            version = "";
        }
        String composite = "artifact:" + type + ":" + group + "/" + name + "#" + version;
        try
        {
            URI uri = new URI( composite );
            return new Artifact( uri );
        } 
        catch( URISyntaxException e )
        {
            // Can not happen.
            final String error =
              "An internal error has occurred. "
              + "The following URI could not be constructed: " + composite;
            throw new TransitRuntimeException( error );
        }
    }

   /**
    * Construct a new URL form a given URI.  If the URI is a Transit URI the 
    * returned URL will be associated with the appropriate handler.
    * @param uri the uri to convert
    * @return the converted url
    * @exception MalformedURLException if the url could not be created
    */
    public static URL toURL( URI uri ) throws MalformedURLException
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( uri );
            return artifact.toURL();
        }
        catch( UnsupportedSchemeException e )
        {
            return uri.toURL();
        }
    }

   /**
    * Test if the supplied uri is from the artifact family.  Specificially
    * the test validates that the supplied uri has a scheme corresponding to 
    * 'artifact', link', or 'local'.
    *
    * @return true if thie uri is artifact based
    */
    public static boolean isRecognized( URI uri )
    {
        String scheme = uri.getScheme();
        if( "artifact".equals( scheme ) )
        {
            return true;
        }
        else if( "link".equals( scheme ) )
        {
            return true;
        }
        else if( "local".equals( scheme ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    /**
     * The artifact uri.
     */
    private final URI m_uri;

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

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Creation of a new Artifact using a supplied uri.
     * @param uri a uri of the form [scheme]:[type]:[group]/[name]#[version]
     *   where [scheme] is one of 'link', 'artifact' or 'local'.
     */
    private Artifact( URI uri )
        throws IllegalArgumentException
    {
        m_uri = uri;
        String ssp = uri.getSchemeSpecificPart();

        if( ssp.indexOf( "//" ) > -1
          || ssp.indexOf( ":/" ) > -1
          || ssp.endsWith( "/" ) )
        {
            final String error =
              "Invalid character sequence in uri ["
              + uri + "].";
            throw new IllegalArgumentException( error );
        }

        int lastSeparatorIndex = ssp.lastIndexOf( "/" );
        if( lastSeparatorIndex > -1 )
        {
            m_name = ssp.substring( lastSeparatorIndex + 1 );
            ssp = ssp.substring( 0, lastSeparatorIndex );
        }
        else
        {
            final String error =
              "Artifact specification ["
              + uri + "] does not contain a group.";
            throw new MissingGroupException( error );
        }

        //
        // the remainder now contains the type and the group
        //

        int typeIndex = ssp.indexOf( ':' );
        if( typeIndex > -1 )
        {
            String type = ssp.substring( 0, typeIndex );
            String group = ssp.substring( typeIndex + 1 );
            m_group = group;
            m_type = type;
        }
        else
        {
            final String error = "Supplied artifact specification ["
              + uri + "] does not contain a type.";
            throw new IllegalArgumentException( error );
        }

        String ver = uri.getFragment();
        if( ver != null )
        {
            if( ver.indexOf( '/' ) >= 0
              || ver.indexOf( '%' ) >= 0
              || ver.indexOf( '\\' ) >= 0
              || ver.indexOf( '*' ) >= 0
              || ver.indexOf( '!' ) >= 0
              || ver.indexOf( '(' ) >= 0
              || ver.indexOf( '@' ) >= 0
              || ver.indexOf( ')' ) >= 0
              || ver.indexOf( '+' ) >= 0
              || ver.indexOf( '\'' ) >= 0
              || ver.indexOf( '{' ) >= 0
              || ver.indexOf( '}' ) >= 0
              || ver.indexOf( '[' ) >= 0
              || ver.indexOf( '}' ) >= 0
              || ver.indexOf( '?' ) >= 0
              || ver.indexOf( ',' ) >= 0
              || ver.indexOf( '#' ) >= 0
              || ver.indexOf( '=' ) >= 0
            )
            {
                final String error =
                  "Supplied artifact specification ["
                    + uri
                    + "] contains illegal characters in the Version part.";
                throw new IllegalArgumentException( error );
            }
        }
    }

    // ------------------------------------------------------------------------
    // public
    // ------------------------------------------------------------------------

    /**
     * Return the protocol for the artifact.
     *
     * @return the protocol scheme
     */
    public final String getScheme()
    {
        return m_uri.getScheme();
    }

    /**
     * Return the group identifier for the artifact.  The group identifier
     * is composed of a sequence of named separated by the '/' character.
     *
     * @return the group identifier
     */
    public final String getGroup()
    {
        return m_group;
    }

    /**
     * Return the name of the artifact.
     *
     * @return the artifact name
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Return the type of the artifact.
     *
     * @return the artifact type
     */
    public final String getType()
    {
        return m_type;
    }

    /**
     * Return the posssibly null version identifier.  The value of the version
     * is an opaque string.
     * @return the artifact version
     */
    public final String getVersion()
    {
        String ver = m_uri.getFragment();
        if( ver == null )
        {
            return null;
        }
        else if( ver.length() == 0 )
        {
            return null;
        }
        else
        {
            return ver;
        }
    }

    /**
     * Create an artifact url backed by the repository.
     *
     * @return the artifact url
     */
    public URL toURL()
    {
        String scheme = getScheme();
        if( "artifact".equals( scheme ) )
        {
            return toURL( new net.dpml.transit.artifact.Handler() );
        }
        else if( "link".equals( scheme ) )
        {
            return toURL( new net.dpml.transit.link.Handler() );
        }
        else if( "local".equals( scheme ) )
        {
            return toURL( new net.dpml.transit.local.Handler() );
        }
        else
        {
            final String error = 
              "URI scheme not recognized: " + m_uri;
            throw new UnsupportedSchemeException( error );
        }
    }

    /**
     * Create an artifact url backed by the repository.
     * @param handler the protocol handler
     * @return the artifact url
     */
    public URL toURL( URLStreamHandler handler )
    {
        try
        {
            return new URL( null, m_uri.toASCIIString(), handler );
        }
        catch( MalformedURLException e )
        {
            // Can not happen!
            final String error =
              "An artifact URI could not be converted to a URL [" 
              + m_uri 
              + "].";
            throw new TransitRuntimeException( error );
        }
    }

    /**
     * Create an artifact url backed by the repository.
     *
     * @return the artifact url
     */
    public URI toURI()
    {
        return m_uri;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    /**
     * Compare this artifact with another artifact.  Artifact comparisom is
     * based on a comparison of the string representation of the artifact with
     * the string representation of the supplied object.
     *
     * @param object the object to compare with this instance
     * @return the comparative order of the supplied object relative to this
     *   artifact
     * @exception NullArgumentException if the supplied object argument is null.
     * @exception ClassCastException if the supplied object is not an Artifact.
     */
    public int compareTo( Object object )
        throws NullArgumentException, ClassCastException
    {
        if( object instanceof Artifact )
        {
            String name = this.toString();
            return name.compareTo( object.toString() );
        }
        else if( null == object )
        {
            throw new NullArgumentException( "object" );
        }
        else
        {
            final String error =
              "Object ["
              + object.getClass().getName()
              + "] does not implement ["
              + this.getClass().getName() + "].";
            throw new ClassCastException( error );
        }
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /**
     * Return a string representation of the artifact.
     * @return the artifact as a uri
     */
    public String toString()
    {
         return m_uri.toString();
    }

    /**
     * Compare this artifact with the supplied object for equality.  This method
     * will return true if the supplied object is an Artifact and has an equal
     * uri.
     *
     * @param other the object to compare with this instance
     * @return TRUE if this artifact is equal to the supplied object
     */
    public boolean equals( Object other )
    {
         if( null == other )
         {
              return false;
         }
         else if( this == other )
         {
              return true;
         }
         else if( other instanceof Artifact )
         {
              Artifact art = (Artifact) other;
              return m_uri.equals( art.m_uri );
         }
         else
         {
             return false;
         }
    }

   /**
    * Return the hashcode for the artifact.
    * @return the hashcode value
    */
    public int hashCode()
    {
        return m_uri.hashCode();
    }
}

