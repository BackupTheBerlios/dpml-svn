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

import java.util.Arrays;
import java.util.Properties;

import net.dpml.library.info.AbstractDirective;

/**
 * The BuilderDirective class describes a collection of modules together
 * with information about type defintions.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class BuilderDirective extends AbstractDirective
{
    private final ListenerDirective[] m_listeners;
    
   /**
    * Creation of a new library directive.
    * @param listeners an array of listener directives
    * @param properties library properties
    */
    public BuilderDirective( ListenerDirective[] listeners, Properties properties )
    {
        super( properties );
        
        if( null == listeners )
        {
            throw new NullPointerException( "listeners" );
        }
        for( int i=0; i<listeners.length; i++ )
        {
            if( null == listeners[i] )
            {
                throw new NullPointerException( "listener" );
            } 
        }
        
        m_listeners = listeners;
    }
    
   /**
    * Return the set of listener directives.
    * @return the listener directive array
    */
    public ListenerDirective[] getListenerDirectives()
    {
        return m_listeners;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof BuilderDirective ) )
        {
            BuilderDirective object = (BuilderDirective) other;
            return Arrays.equals( m_listeners, object.m_listeners );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hascode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashArray( m_listeners );
        return hash;
    }
}
