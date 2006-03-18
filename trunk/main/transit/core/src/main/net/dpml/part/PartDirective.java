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

package net.dpml.part;

import java.io.Serializable;
import java.net.URI;

import net.dpml.lang.Value;

/**
 * Immutable part creation directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PartDirective implements Serializable
{
    private final URI m_uri;
    private final Value[] m_params;
    
   /**
    * Creation of a new part creation directive.
    * @param uri the part uri
    */
    public PartDirective( URI uri )
    {
        this( uri, null );
    }
    
   /**
    * Creation of a new part creation directive.
    * @param uri the part uri
    * @param params an array of value arguments
    */
    public PartDirective( URI uri, Value[] params )
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        m_uri = uri;
        if( null == params )
        {
            m_params = new Value[0];
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
    public Value[] getValues()
    {
        return m_params;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
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
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
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
