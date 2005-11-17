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

package net.dpml.metro.state.impl;

import java.io.Serializable;
import java.net.URI;

import net.dpml.metro.state.Operation;

/**
 * Default implementation of an operation.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultOperation implements Operation, Serializable
{
    private final String m_name;
    private final URI m_handler;
    
   /**
    * Creation of a new operation.
    * @param name the operation name
    * @param handler the uri identifying the handler
    */
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
    
   /**
    * Return the action name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * <p>Return the uri describing the execution criteria.  Recognized uri 
    * schemes include:</p>
    * 
    * <ol>
    *  <li>method:[method-name]
    * </ol>
    *
    * @return the handler uri
    */
    public URI getHandlerURI()
    {
        return m_handler;
    }
    
   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "operation:" + m_name;
    }
    
   /**
    * Compare this object to another for equality.
    * @param other the other object
    * @return true if the object is equal to this object
    */
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
            else
            {
                return equals( m_handler, operation.getHandlerURI() );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hashcode for this instance.
    * @return the hashcode value
    */
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

}
