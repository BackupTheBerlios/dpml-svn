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

import java.net.URI;
import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class TypeDescriptor  extends AbstractDirective
{
    private final String m_name;
    private final String[] m_dependencies;
    private final URI m_uri;

    public TypeDescriptor( String name, URI uri, String[] dependencies, Properties properties )
    {
        super( properties );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        m_dependencies = dependencies;
        m_name = name;
        m_uri = uri;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public URI getURI()
    {
        return m_uri;
    }
    
    public String[] getDependencies()
    {
        return m_dependencies;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof TypeDescriptor ) )
        {
            TypeDescriptor object = (TypeDescriptor) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_uri, object.m_uri ) )
            {
                return false;
            }
            else if( !equals( m_dependencies, object.m_dependencies ) )
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
        hash ^= super.hashArray( m_dependencies );
        hash ^= super.hashValue( m_uri );
        return hash;
    }
}
