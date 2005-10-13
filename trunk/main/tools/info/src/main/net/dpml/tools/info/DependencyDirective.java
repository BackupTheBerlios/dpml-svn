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
public final class DependencyDirective extends AbstractDirective
{
    private final Scope m_scope;
    private final ResourceIncludeDirective[] m_includes;
    
    public DependencyDirective( Scope scope, ResourceIncludeDirective[] includes, Properties properties )
    {
        super( properties );
        
        if( null == scope )
        {
            throw new NullPointerException( "scope" );
        }
        if( null == includes )
        {
            throw new NullPointerException( "includes" );
        }
        m_scope = scope;
        m_includes = includes;
    }
    
    public Scope getScope()
    {
        return m_scope;
    }
    
    public ResourceIncludeDirective[] getIncludeDirectives()
    {
        return m_includes;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof DependencyDirective ) )
        {
            DependencyDirective dep = (DependencyDirective) other;
            if( !equals( m_scope, dep.m_scope ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_includes, dep.m_includes );
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
        hash ^= super.hashArray( m_includes );
        return hash;
    }
}
