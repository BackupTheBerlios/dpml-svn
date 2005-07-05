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

package net.dpml.magic.tasks;

import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.project.ProjectPath;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.File;



/**
 * Build a set of projects taking into account dependencies within the
 * supplied fileset.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class FeatureTask extends ProjectTask
{
    private String m_key;
    private String m_feature;
    private String m_prefix;
    private boolean m_windows = true;
    private boolean m_flag = false;  // os not set
    private String m_type; // optional - used to select type when resolving uris

    public void setKey( final String key )
    {
        m_key = key;
    }

    public void setFeature( final String feature )
    {
        m_feature = feature;
    }

    public void setPrefix( final String prefix )
    {
        m_prefix = prefix;
    }

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

    public void setType( final String type )
    {
        m_type = type;
    }

    protected String getFeature()
    {
        return m_feature;
    }

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

        if( null == m_key )
        {
            m_key = getContext().getKey();
        }

        final ResourceRef ref = new ResourceRef( m_key );
        final Resource resource = getIndex().getResource( ref );

        if( m_feature.equals( "name" ) )
        {
            return resource.getInfo().getName();
        }
        else if( m_feature.equals( "group" ) )
        {
            return resource.getInfo().getGroup();
        }
        else if( m_feature.equals( "type" ) )
        {
            return resource.getInfo().getType();
        }
        else if( m_feature.equals( "version" ) )
        {
            final String version = resource.getInfo().getVersion();
            if( null == version ) return "";
            return version;
        }
        else if( m_feature.equals( "uri" ) )
        {
            if( null == m_type )
            {
                return resource.getInfo().getURI();
            }
            else
            {
                return resource.getInfo().getURI( m_type );
            }
        }
        else if( m_feature.equals( "plugin" ) )
        {
            return resource.getInfo().getURI( "plugin" );
        }
        else if( m_feature.equals( "block" ) )
        {
            return resource.getInfo().getURI( "block" );
        }
        else if( m_feature.equals( "spec" ) )
        {
            return resource.getInfo().getSpec();
        }
        else if( m_feature.equals( "path" ) )
        {
            if( null == m_type )
            {
                return convertString( resource.getInfo().getPath() );
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
            return resource.getInfo().getFilename();
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
    }

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

        for( int i=0; i<translation.length; i++ )
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
        for( int i=0; i<translation.length; i++ )
        {
            String trans = convertString( translation[i] );
            if( i>0 )
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

    private String convertString( String value )
    {
        if( !m_flag ) return value;
        if( m_windows )
        {
            return value.replace( '/', '\\' );
        }
        else
        {
            return value.replace( '\\', '/' );
        }
    }
}
