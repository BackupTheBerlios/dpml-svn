/*
 * Copyright 2006 Stephen J. McConnell.
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

package dpmlx.lang;

import java.io.Serializable;
import java.net.URI;

import net.dpml.transit.info.ValueDirective;

/**
 * Immutable part creation directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PartDirective implements Serializable
{
    private final URI m_uri;
    private final ValueDirective[] m_params;
    
    public PartDirective( URI uri, ValueDirective[] params )
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        m_uri = uri;
        if( null == params )
        {
            m_params = new ValueDirective[0];
        }
        else
        {
            m_params = params;
        }
    }
    
   /**
    * Get the part uri.
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }
    
   /**
    * Get the values to be used during part instantiation.
    * @return the directives
    */
    public ValueDirective[] getValueDirectives()
    {
        return m_params;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof PartDirective )
        {
            PartDirective part = (PartDirective) other;
            if( !m_uri.equals( part.m_uri ) )
            {
                return false;
            }
            else
            {
                return m_params.equals( part.m_params );
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = m_uri.hashCode();
        for( int i=0; i<m_params.length; i++ )
        {
            hash ^= m_params[i].hashCode();
        }
        return hash;
    }
}
