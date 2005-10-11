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

/**
 * The IncludeDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class IncludeDirective extends AbstractDirective
{
    private final String m_type;
    private final String m_value;
    
    public IncludeDirective( String type, String value )
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }
        m_type = type;
        m_value = value;
    }
    
    public String getType()
    {
        return m_type;
    }
    
    public String getValue()
    {
        return m_value;
    }

    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof IncludeDirective ) )
        {
            IncludeDirective include = (IncludeDirective) other;
            if( !equals( m_type, include.m_type ) )
            {
                return false;
            }
            else
            {
                return equals( m_value, include.m_value );
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
        hash ^= super.hashValue( m_type );
        hash ^= super.hashValue( m_value );
        return hash;
    }
}
