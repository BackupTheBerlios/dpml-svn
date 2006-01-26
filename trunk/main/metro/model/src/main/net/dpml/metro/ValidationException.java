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

package net.dpml.metro;

import java.io.Serializable;
import java.net.URI;

import net.dpml.part.ModelException;

/**
 * Exception that describes a series of validation issues.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ValidationException extends ModelException
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
    
   /**
    * Creation of a new <tt>ValidationException</tt>.
    * @param uri controller uri
    * @param source the source object
    * @param issues the array of issues
    */
    public ValidationException( URI uri, Object source, Issue[] issues )
    {
        super( uri, createMessage( source, issues ) );
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
    
   /**
    * Return the source object.
    * @return the source of the validation errors
    */
    public Object getSource()
    {
        return m_source;
    }
    
   /**
    * Return the array iof issues.
    * @return the issues array
    */
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
    
   /**
    * Issue implementation.
    */
    public static final class Issue implements Serializable
    {
        private final String m_issueKey;
        private final String m_issueMessage;
    
       /**
        * Creation of a new issue.
        * @param key the key
        * @param message the message
        */
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
            m_issueKey = key;
            m_issueMessage = message;
        }
        
       /**
        * Return the issue key.
        * @return the key
        */
        public String getKey()
        {
            return m_issueKey;
        }
        
       /**
        * Return the issue message.
        * @return the message
        */
        public String getMessage()
        {
            return m_issueMessage;
        }
        
       /**
        * Test is the supplied object is equal to this object.
        * @param other the other object
        * @return true if the object are equivalent
        */
        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else if( other instanceof Issue )
            {
                Issue issue = (Issue) other;
                if( !m_issueKey.equals( issue.m_issueKey ) )
                {
                    return false;
                }
                return m_issueMessage.equals( issue.m_issueMessage );
            }
            else
            {
                return false;
            }
        }
        
       /**
        * Return the hashcode for the object.
        * @return the hashcode value
        */
        public int hashCode()
        {
            return m_issueKey.hashCode() + m_issueMessage.hashCode();
        }
        
       /**
        * Return a string representation of the type.
        * @return the stringified type
        */
        public String toString()
        {
            return "issue:[" + m_issueKey + "] " + m_issueMessage;
        }
    }
}
