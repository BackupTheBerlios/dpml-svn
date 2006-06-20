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

package net.dpml.state;

import java.io.Serializable;

import net.dpml.state.Action;

/**
 * Default implementation of delegating action.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ExecAction implements Action, Serializable
{
    private final String m_id;
    
   /**
    * Creation of a new exec action.
    * @param id operation name
    */
    public ExecAction( final String id )
    {
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        m_id = id;
    }
    
   /**
    * Return the action name.
    * @return the name
    */
    public String getName()
    {
        return "exec:" + m_id;
    }
    
   /**
    * Return the id of the transition to apply.
    * @return the transition id
    */
    public String getID()
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
        else if( other instanceof ExecAction )
        {
            ExecAction action = (ExecAction) other;
            return m_id.equals( action.getID() );
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
        return "exec:" + m_id;
    }
}
