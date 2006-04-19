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
    private final ProcessorDirective[] m_processors;
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
      ListenerDirective[] listeners, String phase, 
      ProcessorDirective[] processors, Properties properties ) throws UnknownKeyException
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
        if( null == processors )
        {
            throw new NullPointerException( "processors" );
        }
        for( int i=0; i<listeners.length; i++ )
        {
            if( null == listeners[i] )
            {
                throw new NullPointerException( "listener" );
            } 
        }
        
        m_phase = phase;
        m_processors = processors;
        m_listeners = sortListenerDirectives( listeners );
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
    * Return the set of processor directives.
    * @return the processor directive array
    */
    public ProcessorDirective[] getProcessorDirectives()
    {
        return m_processors;
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
            if( !Arrays.equals( m_listeners, object.m_listeners ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_processors, object.m_processors );
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
        hash ^= super.hashArray( m_processors );
        return hash;
    }
    
    private static ListenerDirective[] sortListenerDirectives( ListenerDirective[] directives )
      throws UnknownKeyException
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        for( int i=0; i<directives.length; i++ )
        {
            ListenerDirective directive = directives[i];
            processListenerDirective( directives, visited, stack, directive );
        }
        return (ListenerDirective[]) stack.toArray( new ListenerDirective[0] );
    }
    
    private static void processListenerDirective( 
      ListenerDirective[] directives, List visited, List stack, ListenerDirective directive )
      throws UnknownKeyException
    {
        if( visited.contains( directive ) )
        {
            return;
        }
        else
        {
            visited.add( directive );
            String[] deps = directive.getDependencies();
            ListenerDirective[] providers = getListenerDirectives( directives, deps );
            for( int i=0; i<providers.length; i++ )
            {
                processListenerDirective( directives, visited, stack, providers[i] );
            }
            stack.add( directive );
        }
    }
    
    private static ListenerDirective[] getListenerDirectives( 
      ListenerDirective[] directives, String[] names ) throws UnknownKeyException
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            ListenerDirective directive = getListenerDirective( directives, name );
            list.add( directive );
        }
        return (ListenerDirective[]) list.toArray( new ListenerDirective[0] );
    }

    private static ListenerDirective getListenerDirective( 
      ListenerDirective[] directives, String name ) throws UnknownKeyException
    {
        for( int i=0; i<directives.length; i++ )
        {
            ListenerDirective directive = directives[i];
            if( directive.getName().equals( name ) )
            {
                return directive;
            }
        }
        throw new UnknownKeyException( name );
    }
}
