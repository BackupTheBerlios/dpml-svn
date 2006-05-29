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

import java.net.URI;
import java.util.Properties;
import java.util.Arrays;

import net.dpml.library.info.AbstractDirective;

/**
 * The ListenerDirective is an immutable descriptor used to define 
 * a build listener to be attached to an Ant project.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ListenerDirective  extends AbstractDirective implements Comparable
{
    private final String m_name;
    private final int m_priority;
    private final URI m_uri;
    private final String m_classname;
    
   /**
    * Creation of a new listener directive.
    * @param name the listener name
    * @param priority the listener priority
    * @param uri the listener codebase
    * @param classname optional classname of the plugin instantiation target
    * @param properties supplimentary properties
    * @exception NullPointerException if name or dependencies are null
    * @exception IllegalStateException if both classname and urn values are null
    */
    public ListenerDirective( 
      String name, int priority, URI uri, String classname, Properties properties )
      throws NullPointerException, IllegalStateException
    {
        super( properties );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == uri )
        {
            if( null == classname )
            {
                final String error = 
                  "Listener definition [" 
                  + name
                  + "] does not declare a classname or uri value.";
                throw new IllegalStateException( error );
            }
        }
        m_priority = priority;
        m_name = name;
        m_uri = uri;
        m_classname = classname;
    }
    
   /**
    * Return the listener name.
    * @return the listener name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the listener priority.
    * @return the priority value
    */
    public int getPriority()
    {
        return m_priority;
    }
    
   /**
    * Return the listener codebase uri.
    * @return the urn
    */
    public URI getURI()
    {
        return m_uri;
    }
    
   /**
    * Return the listener codebase uri as a string.
    * @return the uri value
    */
    public String getURISpec()
    {
        if( null == m_uri )
        {
            return null;
        }
        else
        {
            return m_uri.toASCIIString();
        }
    }
    
   /**
    * Get the listener classname.
    *
    * @return the classname of the listener
    */
    public String getClassname()
    {
        return m_classname;
    }
    
    /**
     * Compares this <code>ListenerDirective</code> object to another object.
     * If the object is an <code>ListenerDirective</code>, this function behaves
     * like <code>compareTo(Integer)</code>.  Otherwise, it throws a
     * <code>ClassCastException</code>.
     *
     * @param other the <code>Object</code> to be compared.
     * @return the value <code>0</code> if the argument is a 
     * <code>ListenerDirective</code> with a priority numerically equal to this 
     * <code>ListenerDirective</code>; a value less than <code>0</code> 
     * if the argument is a <code>ListenerDirective</code> numerically 
     * greater than this <code>ListenerDirective</code>; and a value 
     * greater than <code>0</code> if the argument is a 
     * <code>ListenerDirective</code> numerically less than this 
     * <code>ListenerDirective</code>.
     * @exception <code>ClassCastException</code> if the argument is not an
     * <code>ListenerDirective</code>.
     * @see java.lang.Comparable
     */
    public int compareTo( Object other )
    {
        ListenerDirective directive = (ListenerDirective) other;
        Integer p1 = new Integer( m_priority );
        Integer p2 = new Integer( directive.getPriority() );
        return p1.compareTo( p2 );
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ListenerDirective ) )
        {
            ListenerDirective object = (ListenerDirective) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( m_priority != object.m_priority )
            {
                return false;
            }
            else
            {
                return equals( m_uri, object.m_uri );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_name );
        hash ^= super.hashValue( m_uri );
        hash ^= m_priority;
        return hash;
    }
}
