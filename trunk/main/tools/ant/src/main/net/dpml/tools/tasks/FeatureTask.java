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

package net.dpml.tools.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.File;

/**
 * Locate a named feature of the a project or resource.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class FeatureTask extends GenericTask
{
    private String m_key;
    private String m_feature;
    private String m_prefix;
    private boolean m_windows = true;
    private boolean m_flag = false;  // os not set
    private String m_type; // optional - used to select type when resolving uris

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
        
        return getDefinition().getProperty( m_feature );

        //if( null == m_key )
        //{
        //    m_key = getContext().getKey();
        //}

        //final ResourceRef ref = new ResourceRef( m_key );
        //final Resource resource = getIndex().getResource( ref );

        /*
        if( m_feature.equals( "name" ) )
        {
            return resource.getInfo().getName();
        }
        else if( m_feature.equals( "group" ) )
        {
            return resource.getInfo().getGroup();
        }
        else if( m_feature.equals( "version" ) )
        {
            final String version = resource.getInfo().getVersion();
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
                return resource.getInfo().getURI( m_type );
            }
        }
        else if( m_feature.equals( "alias" ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the alias attribute.";
                throw new BuildException( error, getLocation() );
            }
            else
            {
                return resource.getInfo().getType( m_type ).getAlias();
            }
        }
        else if( m_feature.equals( "spec" ) )
        {
            return resource.getInfo().getSpec();
        }
        else if( m_feature.equals( "path" ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the path attribute.";
                throw new BuildException( error, getLocation() );
            }
            else
            {
                return convertString( resource.getInfo().getPath( m_type ) );
            }
        }
        else if( m_feature.equals( "docs" ) )
        {
            return convertString( resource.getInfo().getDocPath() );
        }
        else if( m_feature.equals( "classpath" ) )
        {
            return getPath( resource );
        }
        else if( m_feature.equals( "filename" ) )
        {
            if( null == m_type )
            {
                final String error = 
                  "Type attribute must be supplied in conjuction with the filename attribute.";
                throw new BuildException( error, getLocation() );
            }
            else
            {
                return resource.getInfo().getFilename( m_type );
            }
        }
        else if( m_feature.equals( "short-filename" ) )
        {
            return resource.getInfo().getShortFilename();
        }
        else if( m_feature.equals( "api" ) )
        {
            return convertString( resource.getInfo().getJavadocPath() );
        }
        return null;
        */
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
