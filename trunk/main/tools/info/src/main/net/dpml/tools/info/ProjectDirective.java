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

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ProjectDirective extends AbstractDirective
{
    private final String m_name;
    private final String m_basedir;
    private final TypeDirective[] m_types;
    private final DependencyDirective[] m_dependencies;
    
    public ProjectDirective( String name, String basedir, TypeDirective[] types, DependencyDirective[] dependencies )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == types )
        {
            throw new NullPointerException( "types" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        m_name = name;
        m_basedir = basedir;
        m_types = types;
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
    
    public TypeDirective[] getTypeDirectives()
    {
        return m_types;
    }
    
    public DependencyDirective[] getDependencyDirectives()
    {
        return m_dependencies;
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
            else if( !Arrays.equals( m_types, object.m_types ) )
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
        hash ^= super.hashArray( m_types );
        hash ^= super.hashArray( m_dependencies );
        return hash;
    }
}
