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
public final class ResourceDirective extends AbstractDirective
{
    private final String m_name;
    private final String m_version;
    private final TypeDirective[] m_types;
    private final ResourceIncludeDirective[] m_dependencies;
    
    public ResourceDirective( 
      String name, String version, TypeDirective[] types, ResourceIncludeDirective[] dependencies )
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
        m_version = version;
        m_types = types;
        m_dependencies = dependencies;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getVersion()
    {
        return m_version;
    }
    
    public TypeDirective[] getTypeDirectives()
    {
        return m_types;
    }
    
    public ResourceIncludeDirective[] getIncludeDirectives()
    {
        return m_dependencies;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ResourceDirective ) )
        {
            ResourceDirective object = (ResourceDirective) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_version, object.m_version ) )
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
        hash ^= super.hashValue( m_version );
        hash ^= super.hashArray( m_types );
        hash ^= super.hashArray( m_dependencies );
        return hash;
    }
}
