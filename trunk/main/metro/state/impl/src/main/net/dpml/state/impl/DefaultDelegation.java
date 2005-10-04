/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.state.impl;

import java.io.Serializable;
import java.net.URI;

import net.dpml.state.Delegation;

/**
 * Default implementation of delegating action.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultDelegation implements Delegation, Serializable
{
    private final URI m_id;
    
    public DefaultDelegation( final URI uri )
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        m_id = uri;
    }
    
    public String getName()
    {
        return "delegate:"+ m_id;
    }
    
    public URI getURI()
    {
        return m_id;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof DefaultDelegation )
        {
            DefaultDelegation action = (DefaultDelegation) other;
            if( !m_id.equals( action.getURI() ) )
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
        return m_id.hashCode();
    }

    public String toString()
    {
        return "action:" + m_id;
    }
}