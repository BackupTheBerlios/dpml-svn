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

package org.apache.avalon.tools.tasks;

import java.io.File;

import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.Resource;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.avalon.tools.model.MagicPath;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Filter;
import org.apache.tools.ant.types.Path;



/**
 * Build a set of projects taking into account dependencies within the 
 * supplied fileset. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class FilterTask extends SystemTask
{
    private String m_key;
    private String m_feature;
    private String m_token;
    private String m_prefix;
    private boolean m_windows = true;
    private boolean m_flag = false;  // os not set

    public void init()
    {
        if( !isInitialized() )
        {
            super.init();
        }
    }

    public void setKey( final String key )
    {
        m_key = key;
    }

    public void setFeature( final String feature )
    {
        m_feature = feature;
    }

    public void setToken( final String token )
    {
        m_token = token;
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

    public void execute() throws BuildException 
    {
        if( null == m_key )
        {
            m_key = getContext().getKey();
        }
        if( null == m_feature )
        {
            log( "Ignoring request due to missing 'feature' attribute." );
            return;
        }
        if( null == m_token )
        {
            log( "Ignoring request due to missing 'token' attribute." );
            return;
        }

        log( "Processing feature: " + m_feature, Project.MSG_VERBOSE );

        final String value = getFeature();
        if( null != value )
        {
            final Filter filter = (Filter) getProject().createTask( "filter" );
            filter.setTaskName( getTaskName() );
            filter.init();
            filter.setToken( m_token );
            filter.setValue( value );
            filter.execute();
        }
        else
        {
            log( "Unrecognized or unsupported feature [" + m_feature + "]." );
        }
    }

    private String getFeature()
    {
        final ResourceRef ref = new ResourceRef( m_key );
        final Resource resource = getHome().getResource( ref );

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
            if( null == version ) return "";
            return version;
        }
        else if( m_feature.equals( "uri" ) )
        {
            return resource.getInfo().getURI();
        }
        else if( m_feature.equals( "spec" ) )
        {
            return resource.getInfo().getSpec();
        }
        else if( m_feature.equals( "path" ) )
        {
            return convertString( resource.getInfo().getPath() );
        }
        else if( resource instanceof Definition )
        {
            final Definition def = (Definition) resource;
            if( m_feature.equals( "classpath" ) )
            {
                return getPath( def );
            }
        }
        return null;
    }

    private String getPath( final Definition def )
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

        File cache = getHome().getRepository().getCacheDirectory();
        String root = cache.toString();
        MagicPath path = new MagicPath( getProject() );
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
        // do platform convertion
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
    
    public static class Attribute
    {
        private String m_name;
        private String m_value;
       
        public Attribute()
        {
        }

        public Attribute( final String name, final String value )
        {
            m_name = name;
            m_value = value;
        }

        public void setName( final String name )
        {
            m_name = name;
        }

        public void setValue( final String value )
        {
            m_value = value;
        }

        public String getName()
        {
            return m_name;
        }

        public String getValue()
        {
            return m_value;
        }
    }
}
