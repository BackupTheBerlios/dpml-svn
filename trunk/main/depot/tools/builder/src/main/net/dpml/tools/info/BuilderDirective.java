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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.dpml.library.info.AbstractDirective;

import net.dpml.lang.UnknownKeyException;

/**
 * The BuilderDirective class describes the configuration of the build system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class BuilderDirective extends AbstractDirective
{
    private final ListenerDirective[] m_listeners;
    private final String m_phase;
    
   /**
    * Creation of a new library directive.
    * @param listeners an array of listener directives
    * @param phase the default target phase
    * @param processors an array of processor directives
    * @param properties supplimentary properties
    * @exception UnknownKeyException if a unknown key is referenced
    */
    public BuilderDirective( 
      ListenerDirective[] listeners, String phase, Properties properties ) throws UnknownKeyException
    {
        super( properties );
        
        if( null == listeners )
        {
            throw new NullPointerException( "listeners" );
        }
        if( null == phase )
        {
            throw new NullPointerException( "phase" );
        }
        for( int i=0; i<listeners.length; i++ )
        {
            if( null == listeners[i] )
            {
                throw new NullPointerException( "listener" );
            } 
        }
        
        m_phase = phase;
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
    * Return the default phase.
    * @return the default phase name.
    */
    public String getDefaultPhase()
    {
        return m_phase;
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
            if( !equals( m_phase, object.m_phase ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_listeners, object.m_listeners );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash code value.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_phase );
        hash ^= super.hashArray( m_listeners );
        return hash;
    }
}
