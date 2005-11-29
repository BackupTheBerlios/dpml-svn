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
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Arrays;

import net.dpml.library.info.AbstractDirective;

/**
 * The ListenerDirective is an immutable descriptor used to define 
 * a build listener that serves to construct an identified type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ListenerDirective  extends AbstractDirective
{
    private final String m_name;
    private final String[] m_dependencies;
    private final URI m_uri;
    private final String m_classname;
    
   /**
    * Creation of a new listener directive.
    * @param name the resource type produced by the listener
    * @param uri the listener codebase
    * @param classname optional classname of the plugin instantiation target
    * @param dependencies array of listener names that the listener depends upon
    * @param properties supplimentary properties
    * @exception NullPointerException if name or dependencies are null
    * @exception IllegalStateException if both classname and urn values are null
    */
    public ListenerDirective( String name, URI uri, String classname, 
      String[] dependencies, Properties properties )
      throws NullPointerException, IllegalStateException
    {
        super( properties );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
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
        m_dependencies = dependencies;
        m_name = name;
        m_uri = uri;
        m_classname = classname;
    }
    
   /**
    * Return the listener resource type name.
    * @return the name of the resource type produced by the listener
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the listener codebase urn.
    * @return the urn
    */
    public URI getURI()
    {
        return m_uri;
    }
    
   /**
    * Return the listener codebase urn.
    * @return the urn
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
    * Return the listener classname to load.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }
    
   /**
    * Return the listener dependencies. If a listener declares a dependency 
    * on another listener, it is in effect saying that the names listeners 
    * must be executed before this listener.  At an operational level listener
    * dependencies effect the order in which listeners are attached to a 
    * an Ant project.
    *
    * @return the array of listener names 
    */
    public String[] getDependencies()
    {
        return m_dependencies;
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
            else if( !equals( m_uri, object.m_uri ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_dependencies, object.m_dependencies );
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
        hash ^= super.hashArray( m_dependencies );
        hash ^= super.hashValue( m_uri );
        return hash;
    }
}
