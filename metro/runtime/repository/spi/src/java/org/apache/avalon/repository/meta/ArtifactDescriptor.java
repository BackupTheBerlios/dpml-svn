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

package org.apache.avalon.repository.meta;

import java.io.Serializable;
import java.util.NoSuchElementException;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingException;

import org.apache.avalon.repository.Artifact;

/**
 * An abstract descriptor holds attributes about an artifact.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ArtifactDescriptor.java 43330 2004-09-04 10:26:58Z niclas $
 */
public class ArtifactDescriptor implements Serializable
{
    //-----------------------------------------------------------
    // static
    //-----------------------------------------------------------

    public static final String DOMAIN_KEY = "meta.domain";
    public static final String VERSION_KEY = "meta.version";
    public static final String BUILD_KEY = 
      "avalon.artifact.signature";

    //-----------------------------------------------------------
    // immutable state
    //-----------------------------------------------------------

    private final String c_domain;
    private final String c_version;

    private final String m_build;

    private final Artifact m_artifact;

    //-----------------------------------------------------------
    // constructor
    //-----------------------------------------------------------

    /**
     * Creates a new Meta descriptor.
     * 
     * @param attributes the metadata attributes
     * @exception NullPointerException if the supplied 
     *   attributes argument is null
     * @exception MetaException if an attribute is inconsitent
     */
    public ArtifactDescriptor( Attributes attributes ) 
      throws MetaException
    {
        if( null == attributes )
          throw new NullPointerException( "attributes" );

        try
        {
            c_domain = getValue( attributes, DOMAIN_KEY );
            if( null == c_domain ) 
            {
                final String error = 
                  "Missing attribute: " + DOMAIN_KEY;
                throw new MetaException( error );
            }

            c_version = getValue( attributes, VERSION_KEY );
            if( null == c_version ) 
            {
                final String error = 
                  "Missing attribute: " + VERSION_KEY;
                throw new MetaException( error );
            }

            m_build = getAttribute( attributes, BUILD_KEY, "" );

            String group = getAttribute( attributes, Artifact.GROUP_KEY, "" );
            String name = getAttribute( attributes, Artifact.NAME_KEY, "" );
            String version = getAttribute( attributes, Artifact.VERSION_KEY, "" );

            m_artifact = Artifact.createArtifact( group, name, version );
        }
        catch( NamingException e )
        {
            final String error = 
              "Unexpected naming exception during metadata creation.";
            throw new MetaException( error, e );
        }
        catch( NoSuchElementException e )
        {
            final String error = 
              "Unexpected exception during metadata creation.";
            throw new MetaException( error, e );
        }
    }

    //-----------------------------------------------------------
    // public
    //-----------------------------------------------------------

   /**
    * Return the artifact reference.
    * @return the artifact
    */
    public Artifact getArtifact()
    {
        return m_artifact;
    }

   /**
    * Return the meta data domain value.
    * @return the domain
    */
    public String getDomain()
    {
        return c_domain;
    }

   /**
    * Return the meta data version
    * @return the version
    */
    public String getVersion()
    {
        return c_version;
    }

   /**
    * Return the build identifier
    * @return the identifier
    */
    public String getBuild()
    {
        return m_build;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the obhject to compare this object with
    * @return true if the objects are equivalent
    */
    public boolean equals( Object other )
    {
        boolean isEqual = other instanceof ArtifactDescriptor;
        if ( isEqual )
        {
            ArtifactDescriptor meta = (ArtifactDescriptor) other;
            isEqual = isEqual && c_domain.equals( meta.c_domain );
            isEqual = isEqual && c_version.equals( meta.c_version );
            isEqual = isEqual && m_build.equals( meta.m_build );
            isEqual = isEqual && m_artifact.equals( meta.m_artifact );
        }
        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = 1;
        hash >>>= 13;
        hash ^= c_domain.hashCode();
        hash >>>= 13;
        hash ^= c_version.hashCode();
        hash >>>= 13;
        hash ^= m_build.hashCode();
        hash >>>= 13;
        hash ^= m_artifact.hashCode();
        hash >>>= 13;
        return hash;

    }

   /**
    * Return a stringified representation of the instance.
    * @return the string representation
    */
    public String toString()
    {
        return "[artifact: " + getDomain() 
          + "::" + getVersion() + "]";
    }

    //-----------------------------------------------------------
    // utilities
    //-----------------------------------------------------------

    private String getAttribute( Attributes attributes, String key, String def )
    {
        try
        {
            return getValue( attributes, key ); 
        }
        catch( Throwable e )
        {
            return def;
        }
    }

    protected String getValue( Attributes attributes, String key )
      throws NamingException, NoSuchElementException
    {
        Attribute attribute = attributes.get( key );
        if( null == attribute ) return null;
        Object object = attribute.get();
        if( null == object ) return null;
        return object.toString();
    }
}
