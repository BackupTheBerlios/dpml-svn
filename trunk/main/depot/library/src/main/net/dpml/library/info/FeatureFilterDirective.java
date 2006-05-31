/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.library.info;

import java.io.File;

import net.dpml.lang.Version;

import net.dpml.library.Feature;
import net.dpml.library.FeatureRuntimeException;
import net.dpml.library.Resource;
import net.dpml.library.ResourceNotFoundException;
import net.dpml.library.Type;

import net.dpml.transit.Artifact;

/**
 * Simple value filter.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FeatureFilterDirective extends FilterDirective
{
    private final Feature m_feature;
    private final boolean m_alias;
    private String m_reference;
    private String m_type;
    
   /**
    * Creation of a new anonymous resource directive.
    * @param token the filter token
    * @param ref an external resource refernce (possibly null)
    * @param feature the resource feature
    * @param type the resource type
    * @param alias if the alias flag
    */
    public FeatureFilterDirective( String token, String ref, Feature feature, String type, boolean alias )
    {
        super( token );
        m_reference = ref;
        m_feature = feature;
        m_type = type;
        m_alias = alias;
    }
    
   /**
    * Get the resource ref value.  
    * If null the ref shall be interprited as the enclosing project.
    * @return the ref value
    */
    public String getResourceReference()
    {
        return m_reference;
    }
    
   /**
    * Return the filter value.
    * @param resource the enclosing resource
    * @return the resolved value
    * @exception ResourceNotFoundException if the feature references a 
    *  resource that is unknown
    */
    public String getValue( Resource resource ) throws ResourceNotFoundException
    {
        Resource r = getReferenceResource( resource );
        
        if( null != m_type && !r.isa( m_type ) )
        {
            final String error = 
              "The feature request for the type [" 
              + m_type 
              + "] from the resource ["
              + r 
              + "] cannot be fullfilled because the resource does not declare "
              + "production of the requested type.";
            throw new FeatureRuntimeException( error );
        }
        
        if( m_feature.equals( Feature.NAME ) )
        {
            return r.getName();
        }
        else if( m_feature.equals( Feature.GROUP ) )
        {
            return r.getParent().getResourcePath();
        }
        else if( m_feature.equals( Feature.VERSION ) )
        {
            String version = r.getVersion();
            if( null == version )
            {
                return "";
            }
            else
            {
                return version;
            }
        }
        else if( m_feature.equals( Feature.URI ) )
        {
            return resolveURIFeature( r );
        }
        else if( m_feature.equals( Feature.SPEC ) )
        {
            String path = r.getResourcePath();
            String version = r.getVersion();
            if( null == version )
            {
                return path;
            }
            else
            {
                return path + "#" + version;
            }
        }
        else if( m_feature.equals( Feature.PATH ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the uri attribute.";
                throw new FeatureRuntimeException( error );
            }
            else
            {
                Artifact artifact = r.getArtifact( m_type );
                try
                {
                    File cached = 
                      (File) artifact.toURL().getContent( new Class[]{File.class} );
                    return cached.getCanonicalPath();
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unable to resolve resource path.";
                    throw new FeatureRuntimeException( error, e );
                }
            }
        }
        else if( m_feature.equals( Feature.FILENAME ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the filename attribute.";
                throw new IllegalArgumentException( error );
            }
            return r.getLayoutPath( m_type );
        }
        else
        {
            final String error = 
              "Invalid feature [" + m_feature + "].";
            throw new FeatureRuntimeException( error );
        }
    }
    
    private String resolveURIFeature( Resource resource )
    {
        if( null == m_type )
        {
            final String error = 
              "Type attribute must be supplied in conjuction with the uri attribute.";
            throw new FeatureRuntimeException( error );
        }
        else
        {
            if( m_alias )
            {
                Type type = resource.getType( m_type );
                Version version = type.getVersion();
                if( null != version )
                {
                    Artifact artifact = resource.getArtifact( m_type );
                    String group = artifact.getGroup();
                    String name = artifact.getName();
                    if( Version.NULL_VERSION.equals( version ) )
                    {
                        return "link:" 
                          + m_type 
                          + ":" 
                          + group
                          + "/" 
                          + name; 
                    }
                    else
                    {
                        int major = version.getMajor();
                        int minor = version.getMinor();
                        return "link:" 
                          + m_type 
                          + ":" 
                          + group 
                          + "/" 
                          + name
                          + "#"
                          + major
                          + "."
                          + minor;
                    }
                }
                else
                {
                    final String error = 
                      "Cannot resolve link from resource [" 
                      + resource
                      + "] because the resource does not declare production of an alias for the type ["
                      + type.getID() 
                      + "].";
                    throw new FeatureRuntimeException( error );
                }
            }
            else
            {
                Artifact artifact = resource.getArtifact( m_type );
                return artifact.toURI().toASCIIString();
            }
        }
    }
    
    private Resource getReferenceResource( Resource resource ) throws ResourceNotFoundException
    {
        if( null == m_reference )
        {
            return resource;
        }
        else
        {
            return resource.getLibrary().getResource( m_reference );
        }
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof FeatureFilterDirective ) )
        {
            FeatureFilterDirective directive = (FeatureFilterDirective) other;
            if( !m_feature.equals( directive.m_feature ) )
            {
                return false;
            }
            else if( !m_type.equals( directive.m_type ) )
            {
                return false;
            }
            else if( !m_reference.equals( directive.m_reference ) )
            {
                return false;
            }
            else
            {
                return m_alias == directive.m_alias;
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_feature );
        hash ^= super.hashValue( m_reference );
        hash ^= super.hashValue( m_type );
        if( m_alias )
        {
            hash ^= 1;
        }
        else
        {
            hash ^= -1;
        }
        return hash;
    }
}
