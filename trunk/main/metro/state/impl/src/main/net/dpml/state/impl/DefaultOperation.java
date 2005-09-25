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

import net.dpml.state.Operation;

/**
 * Default implementation of an operation descriptor.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultOperation implements Operation, Serializable
{
    private final String m_name;
    private final URI m_handler;
    
    public DefaultOperation( final String name, URI handler )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        m_name = name;
        m_handler = handler;
    }
    
    public String getName()
    {
        return m_name;
    }
        
    public URI getHandlerURI()
    {
        return m_handler;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Operation )
        {
            DefaultOperation operation = (DefaultOperation) other;
            if( !m_name.equals( operation.getName() ) )
            {
                return false;
            }
            else if( !equals( m_handler, operation.getHandlerURI() ) )
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
        int hash = getClass().hashCode();
        hash ^= m_name.hashCode();
        if( null != m_handler )
        {
            hash ^= m_handler.hashCode();
        }
        return hash;
    }
    
    private boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return null == b;
        }
        else
        {
            return a.equals( b );
        }
    }

    public String toString()
    {
        return "operation:" + m_name;
    }
}
