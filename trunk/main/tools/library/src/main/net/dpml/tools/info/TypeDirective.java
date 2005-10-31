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

import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class TypeDirective  extends AbstractDirective
{
    private final String m_name;
    private final boolean m_alias;

    public TypeDirective( String name )
    {
        this( name, false, null );
    }
    
    public TypeDirective( String name, boolean alias )
    {
        this( name, alias, null );
    }
    
    public TypeDirective( String name, boolean alias, Properties properties )
    {
        super( properties );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        m_alias = alias;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public boolean getAlias()
    {
        return m_alias;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof TypeDirective ) )
        {
            TypeDirective object = (TypeDirective) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( m_alias != object.m_alias )
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
        if( m_alias )
        {
            hash ^= 1298657;
        }
        hash ^= super.hashValue( m_name );
        return hash;
    }
    
    public String toString()
    {
        return "type-desc:" + m_name;
    }
}
