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
 * The LibraryDirective class describes a collection of modules together
 * with information about type defintions.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class LibraryDirective extends AbstractDirective
{
    private final TypeDescriptor[] m_types;
    private final ModuleIncludeDirective[] m_modules;
    
    public LibraryDirective(
      TypeDescriptor[] types, ModuleIncludeDirective[] modules, Properties properties )
    {
        super( properties );
        
        if( null == types )
        {
            throw new NullPointerException( "types" );
        }
        if( null == modules )
        {
            throw new NullPointerException( "modules" );
        }

        m_types = types;
        m_modules = modules;
    }
    
    public ModuleIncludeDirective[] getModuleIncludeDirectives()
    {
        return m_modules;
    }
    
    public TypeDescriptor[] getTypeDescriptors()
    {
        return m_types;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof LibraryDirective ) )
        {
            LibraryDirective object = (LibraryDirective) other;
            if( !Arrays.equals( m_types, object.m_types ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_modules, object.m_modules ) )
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
        hash ^= super.hashArray( m_types );
        hash ^= super.hashArray( m_modules );
        return hash;
    }
}
