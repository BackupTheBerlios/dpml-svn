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
public final class ModuleDirective extends AbstractDirective
{
    private final String m_name;
    private final String m_version;
    private final String m_basedir;
    private final IncludeDirective[] m_refs;
    private final ModuleDirective[] m_modules;
    private final ProjectDirective[] m_projects;
    private final ResourceDirective[] m_resources;
    
    public ModuleDirective(
      String name, String version, String basedir, IncludeDirective[] refs, 
      ModuleDirective[] modules, ProjectDirective[] projects, 
      ResourceDirective[] resources )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == refs )
        {
            throw new NullPointerException( "refs" );
        }
        if( null == modules )
        {
            throw new NullPointerException( "modules" );
        }
        if( null == projects )
        {
            throw new NullPointerException( "projects" );
        }
        if( null == resources )
        {
            throw new NullPointerException( "resources" );
        }
        m_name = name;
        m_version = version;
        m_basedir = basedir;
        m_refs = refs;
        m_modules = modules;
        m_projects = projects;
        m_resources = resources;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getBasedir()
    {
        return m_basedir;
    }
    
    public String getVersion()
    {
        return m_version;
    }
    
    public IncludeDirective[] getIncludeDirectives()
    {
        return m_refs;
    }
    
    public ModuleDirective[] getModuleDirectives()
    {
        return m_modules;
    }
    
    public ProjectDirective[] getProjectDirectives()
    {
        return m_projects;
    }
    
    public ResourceDirective[] getResourceDirectives()
    {
        return m_resources;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ModuleDirective ) )
        {
            ModuleDirective object = (ModuleDirective) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_version, object.m_version ) )
            {
                return false;
            }
            else if( !equals( m_basedir, object.m_basedir ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_refs, object.m_refs ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_modules, object.m_modules ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_projects, object.m_projects ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_resources, object.m_resources ) )
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
        hash ^= super.hashValue( m_version );
        hash ^= super.hashValue( m_basedir );
        hash ^= super.hashArray( m_refs );
        hash ^= super.hashArray( m_modules );
        hash ^= super.hashArray( m_projects );
        hash ^= super.hashArray( m_resources );
        return hash;
    }
}
