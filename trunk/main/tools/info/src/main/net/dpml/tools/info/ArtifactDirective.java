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
 * An ArtifactDirective describes the production of a types artifact by a project.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ArtifactDirective extends AbstractDirective
{
    private final String m_type;

    public ArtifactDirective( String type )
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        m_type = type;
    }
    
    public String getType()
    {
        return m_type;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ArtifactDirective ) )
        {
            ArtifactDirective object = (ArtifactDirective) other;
            if( !equals( m_type, object.m_type ) )
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
        hash ^= super.hashValue( m_type );
        return hash;
    }
}
