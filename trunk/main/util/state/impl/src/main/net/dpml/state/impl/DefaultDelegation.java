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
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultDelegation implements Delegation, Serializable
{
    private final URI m_id;
    
   /**
    * Creation of a new delegation action.
    * @param uri the delegation target
    */
    public DefaultDelegation( final URI uri )
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        m_id = uri;
    }
    
   /**
    * Return the action name.
    * @return the name
    */
    public String getName()
    {
        return "delegate:"+ m_id;
    }
    
   /**
    * Return a uri declararing the delegation target.
    * @return the delegation uri
    */
    public URI getURI()
    {
        return m_id;
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
        else if( other instanceof DefaultDelegation )
        {
            DefaultDelegation action = (DefaultDelegation) other;
            return m_id.equals( action.getURI() );
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
        return m_id.hashCode();
    }

   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "action:" + m_id;
    }
}
