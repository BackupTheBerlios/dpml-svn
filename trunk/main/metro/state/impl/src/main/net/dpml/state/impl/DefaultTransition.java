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

import net.dpml.state.State;
import net.dpml.state.Transition;

/**
 * Default implemention of a state transition descriptor.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultTransition implements Transition, Serializable
{
    private final String m_name;
    private final String m_target;
    private final URI m_handler;
    
    private transient State m_state;
    
   /**
    * Creation of a new transition.
    * @param name the transition name
    * @param target the transit target state name
    */
    public DefaultTransition( final String name, final String target )
    {
        this( name, target, null );
    }
    
   /**
    * Creation of a new transition.
    * @param name the transition name
    * @param target the transit target state name
    * @param handler a uri of a handler to associate with the transition
    */
    public DefaultTransition( final String name, final String target, final URI handler )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == target )
        {
            throw new NullPointerException( "target" );
        }
        m_name = name;
        m_target = target;
        m_handler = handler;
    }
    
   /**
    * Set the state that this transition is a part of.
    * @param state the owning state
    */
    public void setState( State state )
    {
        if( null == state )
        {
            throw new NullPointerException( "state" );
        }
        if( null == m_state )
        {
            m_state = state;
        }
        else
        {
            throw new IllegalStateException( "State already set." );
        }
    }
    
   /**
    * Return the state that this transition is a part of.
    * @return the owning state
    */
    public State getState()
    {
        if( null != m_state )
        {
            return m_state;
        }
        else
        {
            throw new IllegalStateException( "Enclosing state has not been declared." );
        }
    }
    
   /**
    * Return the transition name
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the transition target state name
    * @return the target state name
    */
    public String getTargetName()
    {
        return m_target;
    }
    
   /**
    * Return the handler uri associated with this state transition.
    * @return the handler uri
    */
    public URI getHandlerURI()
    {
        return m_handler;
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
        else if( other instanceof DefaultTransition )
        {
            DefaultTransition transition = (DefaultTransition) other;
            if( !m_name.equals( transition.getName() ) )
            {
                return false;
            }
            else if( !equals( m_target, transition.getTargetName() ) )
            {
                return false;
            }
            else 
            {
                return equals( m_handler, transition.getHandlerURI() );
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
        int hash = m_name.hashCode();
        hash ^= m_target.hashCode();
        if( m_handler != null )
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
    
   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "transition:" + getName() + " (target=" + m_target + ")";
    }
}
