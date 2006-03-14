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

package net.dpml.tools.tasks;

import java.io.File;

import net.dpml.library.model.ResourceNotFoundException;
import net.dpml.library.model.Resource;

import net.dpml.transit.Artifact;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Locate a named feature of the a project or resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class FeatureTask extends GenericTask
{
    private String m_key;
    private String m_ref;
    private String m_feature;
    private String m_prefix;
    private boolean m_windows = true;
    private boolean m_flag = false;  // os not set
    private String m_type; // optional - used to select type when resolving uris
    private boolean m_alias = false; // used when resolving filenames

   /**
    * Set the key of the target project or resource description from which features will be 
    * resolved from.  If not declared the key defaults to the current defintion.
    *
    * @param key the resource key
    */
    public void setKey( final String key )
    {
        m_key = key;
    }

   /**
    * Set filename resolution switch. If true the filename feature will
    * return an alias path.
    *
    * @param flag the alias switch
    */
    public void setAlias( final boolean flag )
    {
        m_alias = flag;
    }


   /**
    * Set the ref of the target project or resource description from which features will be 
    * resolved from.
    *
    * @param ref the resource reference
    */
    public void setRef( final String ref )
    {
        m_ref = ref;
    }

   /**
    * Set the name of the feature.
    * @param feature the feature name
    */
    public void setFeature( final String feature )
    {
        m_feature = feature;
    }

   /**
    * Set the prefix.
    * @param prefix ??
    */
    public void setPrefix( final String prefix )
    {
        m_prefix = prefix;
    }

   /**
    * Set the platform.
    * @param os a value of 'windows' or 'unix'
    */
    public void setPlatform( final String os )
    {
        m_flag = true;
        if( "windows".equalsIgnoreCase( os ) )
        {
            m_windows = true;
        }
        else if( "unix".equalsIgnoreCase( os ) )
        {
            m_windows = false;
        }
    }

   /**
    * Optionaly set the resource type that the feature is related to.
    * @param type the resource type
    */
    public void setType( final String type )
    {
        m_type = type;
    }

   /**
    * Return the assigned feature name.
    * @return the feature name
    */
    protected String getFeature()
    {
        return m_feature;
    }

   /**
    * Resolve the feature value.
    * @return the feature value
    */
    protected String resolve()
    {
        if( null == m_feature )
        {
            final String error = "Missing 'feature' attribute.";
            throw new BuildException( error );
        }
        else
        {
            log( "Processing feature: " + m_feature, Project.MSG_VERBOSE );
        }
        
        String ref = getRef();
        Resource resource = getResource( ref );
        
        if( null != m_type && !resource.isa( m_type ) )
        {
            final String error = 
              "The feature request for the type [" 
              + m_type 
              + "] from the resource ["
              + resource 
              + "] cannot be fullfilled because the resource does not declare "
              + "production of the requested type.";
            throw new BuildException( error, getLocation() );
        }

        if( m_feature.equals( "name" ) )
        {
            return resource.getName();
        }
        else if( m_feature.equals( "group" ) )
        {
            return resource.getParent().getResourcePath();
        }
        else if( m_feature.equals( "version" ) )
        {
            String version = resource.getVersion();
            if( null == version )
            {
                return "";
            }
            else
            {
                return version;
            }
        }
        else if( m_feature.equals( "uri" ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the uri attribute.";
                throw new BuildException( error, getLocation() );
            }
            else
            {
                Artifact artifact = resource.getArtifact( m_type );
                return artifact.toURI().toString();
            }
        }
        else if( m_feature.equals( "spec" ) )
        {
            String path = resource.getResourcePath();
            String version = resource.getVersion();
            if( null == version )
            {
                return path;
            }
            else
            {
                return path + "#" + version;
            }
        }
        else if( m_feature.equals( "path" ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the uri attribute.";
                throw new BuildException( error, getLocation() );
            }
            else
            {
                Artifact artifact = resource.getArtifact( m_type );
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
                    throw new BuildException( error, e, getLocation() );
                }
            }
        }
        else if( m_feature.equals( "filename" ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the filename attribute.";
                throw new BuildException( error, getLocation() );
            }
            return getContext().getLayoutPath( m_type );
        }
        else
        {
            return resource.getProperty( m_feature );
        }
    }
    
    private String getRef()
    {
        if( null != m_ref )
        {
            return m_ref;
        }
        else if( null != m_key )
        {
            return getResource().getParent().getResourcePath() + "/" + m_key;
        }
        else
        {
            return getResource().getResourcePath();
        }
    }

    private Resource getResource( String ref )
    {
        try
        {
            return getContext().getLibrary().getResource( ref );
        }
        catch( ResourceNotFoundException e )
        {
            final String error = 
              "Feature reference ["
              + ref
              + "] in the project [" 
              + getResource()
              + "] is unknown.";
            throw new BuildException( error, e );
        }
    }
    
    /*
    private String getPath( final Resource def )
    {
        if( null == m_prefix )
        {
            final String error =
              "Filter attribute 'prefix' is not declared.";
            throw new BuildException( error );
        }
        if( !m_flag )
        {
            final String error =
              "Filter attribute 'platform' is not declared.";
            throw new BuildException( error );
        }

        File cache = getCacheDirectory();
        String root = cache.toString();
        ProjectPath path = new ProjectPath( getProject() );
        path.setMode( "RUNTIME" );
        path.setKey( def.getKey() );
        path.setResolve( false );
        String sequence = path.toString();
        String[] translation = Path.translatePath( getProject(), sequence );

        //
        // substitute the cache directory with the prefix symbol
        //

        for( int i=0; i < translation.length; i++ )
        {
            String trans = translation[i];
            if( trans.startsWith( root ) )
            {
                String relativeFilename = trans.substring( root.length() );
                log( relativeFilename, Project.MSG_VERBOSE );
                translation[i] = m_prefix + relativeFilename;
            }
        }

        //
        // do platform conversion
        //

        StringBuffer buffer = new StringBuffer();
        for( int i=0; i < translation.length; i++ )
        {
            String trans = convertString( translation[i] );
            if( i > 0 )
            {
                if( m_windows )
                {
                    buffer.append( ";" );
                }
                else
                {
                    buffer.append( ":" );
                }
            }
            buffer.append( trans );
        }

        return buffer.toString();
    }
    */
    /*
    private String convertString( String value )
    {
        if( !m_flag )
        {
            return value;
        }
        if( m_windows )
        {
            return value.replace( '/', '\\' );
        }
        else
        {
            return value.replace( '\\', '/' );
        }
    }
    */
}
