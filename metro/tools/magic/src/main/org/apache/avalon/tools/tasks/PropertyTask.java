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

import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.Policy;
import org.apache.avalon.tools.model.Resource;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Build a set of projects taking into account dependencies within the 
 * supplied fileset. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PropertyTask extends SystemTask
{
    private String m_key;
    private String m_feature;
    private String m_property;
    private boolean m_resolve = true;

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

    public void setResolve( final boolean policy )
    {
        m_resolve = policy;
    }

    public void setFeature( final String feature )
    {
        m_feature = feature;
    }

    public void setProperty( final String property )
    {
        m_property = property;
    }

    public void setName( final String property )
    {
        m_property = property;
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
        if( null == m_property )
        {
            log( "Ignoring request due to missing 'property' attribute." );
            return;
        }

        final ResourceRef ref = new ResourceRef( m_key );
        final Resource resource = getHome().getResource( ref );
        if( m_resolve )
        {
            resource.getArtifact( getProject() );
        }

        final String value = getFeature( resource );

        if( null != value )
        {
            final Property property = (Property) getProject().createTask( "property" );
            property.init();
            property.setName( m_property );
            property.setValue( value );
            property.setTaskName( getTaskName() );
            property.execute();
        }
        else
        {
            log( "Unrecognized or unsupported feature [" + m_feature + "]." );
        }
    }

    private String getFeature( Resource resource )
    {
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
            return resource.getInfo().getURI();
        }
        else if( m_feature.equals( "path" ) )
        {
            return resource.getInfo().getPath();
        }
        else if( m_feature.equals( "spec" ) )
        {
            return resource.getInfo().getSpec();
        }
        else if( m_feature.equals( "filename" ) )
        {
            return resource.getInfo().getFilename();
        }
        else if( m_feature.equals( "short-filename" ) )
        {
            return resource.getInfo().getShortFilename();
        }
        else
        {
            final String error = 
              "Invalid property name [" + m_feature + "].";
            throw new BuildException( error );
        }
    }
}
