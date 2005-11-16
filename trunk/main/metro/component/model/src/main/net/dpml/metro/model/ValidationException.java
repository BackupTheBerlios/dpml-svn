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

package net.dpml.metro.model;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.data.Directive;

import net.dpml.part.ContextException;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Exception that describes a series of validation issues.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public final class ValidationException extends ContextException
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------
    
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------
    
    private final Object m_source;
    private final Issue[] m_issues;
    
    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------
    
    public ValidationException( Object source, Issue[] issues )
    {
        super( createMessage( source, issues ) );
        if( null == source )
        {
            throw new NullPointerException( "source" );
        }
        if( null == issues )
        {
            throw new NullPointerException( "issues" );
        }
        m_source = source;
        m_issues = issues;
    }
    
    public Object getSource()
    {
        return m_source;
    }
    
    public Issue[] getIssues()
    {
        return m_issues;
    }
    
    //--------------------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------------------
    
    private static String createMessage( final Object source, final Issue[] issues )
    {
        if( null == source )
        {
            throw new NullPointerException( "source" );
        }
        if( null == issues )
        {
            throw new NullPointerException( "issues" );
        }
        String message = 
          "Validation failure in ["
            + source.getClass().getName()
            + "#" 
            + System.identityHashCode( source )
            + "] due to ["
            + issues.length
            + "] unresolved issue(s).";
        return message;
    }
    
    public static final class Issue implements Serializable
    {
        private final String m_key;
        private final String m_message;
    
        public Issue( String key, String message )
        {
            if( null == key )
            {
                throw new NullPointerException( "key" );
            }
            if( null == message )
            {
                throw new NullPointerException( "message" );
            }
            m_key = key;
            m_message = message;
        }
        
        public String getKey()
        {
            return m_key;
        }
        
        public String getMessage()
        {
            return m_message;
        }
        
        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else if( other instanceof Issue )
            {
                Issue issue = (Issue) other;
                if( !m_key.equals( issue.m_key ) )
                {
                    return false;
                }
                if( !m_message.equals( issue.m_message ) )
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
            int hash = m_key.hashCode();
            hash = hash ^= m_message.hashCode();
            return hash;
        }
        
        public String toString()
        {
            return "issue:[" + m_key + "] " + m_message;
        }
    }
}
