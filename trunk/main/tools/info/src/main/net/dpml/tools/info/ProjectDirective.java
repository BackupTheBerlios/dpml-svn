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

package net.dpml.tools.info;

import java.util.Arrays;
import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ProjectDirective extends AbstractDirective
{
    private final String m_name;
    private final String m_basedir;
    private final ArtifactDirective[] m_artifacts;
    private final DependencyDirective[] m_dependencies;
    
    public ProjectDirective( 
      String name, String basedir, ArtifactDirective[] artifacts, 
      DependencyDirective[] dependencies, Properties properties )
    {
        super( properties );
        
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == artifacts )
        {
            throw new NullPointerException( "artifacts" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        m_name = name;
        m_basedir = basedir;
        m_artifacts = artifacts;
        m_dependencies = dependencies;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getBasedir()
    {
        return m_basedir;
    }
    
    public ArtifactDirective[] getArtifactDirectives()
    {
        return m_artifacts;
    }
    
    public DependencyDirective[] getDependencyDirectives()
    {
        return m_dependencies;
    }
    
    public DependencyDirective getDependencyDirective( Scope scope )
    {
        for( int i=0; i<m_dependencies.length; i++ )
        {
            DependencyDirective directive = m_dependencies[i];
            if( scope.equals( directive.getScope() ) )
            {
                return directive;
            }
        }
        return null;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ProjectDirective ) )
        {
            ProjectDirective object = (ProjectDirective) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_basedir, object.m_basedir ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_artifacts, object.m_artifacts ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_dependencies, object.m_dependencies ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_name );
        hash ^= super.hashValue( m_basedir );
        hash ^= super.hashArray( m_artifacts );
        hash ^= super.hashArray( m_dependencies );
        return hash;
    }
}
