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

package net.dpml.builder.info;

import java.util.Properties;
import java.util.Arrays;

import net.dpml.build.info.AbstractDirective;

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
    private final String m_urn;
    private final String m_classname;

   /**
    * Creation of a new listener directive.
    * @param name the processor name
    */
    public ListenerDirective( String name )
    {
        this( name, null );
    }
    
   /**
    * Creation of a new listener directive.
    * @param name the processor name
    * @param urn the processor codebase
    */
    public ListenerDirective( String name, String urn )
    {
        this( name, urn, new String[0] );
    }
    
   /**
    * Creation of a new listener directive.
    * @param name the resource type produced by the listener
    * @param urn the listener codebase
    * @param dependencies array of processor names that the listener depends upon
    */
    public ListenerDirective( String name, String urn, String[] dependencies )
    {
        this( name, urn, dependencies, null );
    }
    
   /**
    * Creation of a new listener directive.
    * @param name the resource type produced by the listener
    * @param urn the listener codebase
    * @param dependencies array of listener names that the listener depends upon
    * @param properties supplimentary properties
    */
    public ListenerDirective( String name, String urn, String[] dependencies, Properties properties )
    {
        this( name, urn, null, dependencies, properties );
    }
    
   /**
    * Creation of a new listener directive.
    * @param name the resource type produced by the listener
    * @param urn the listener codebase
    * @param classname optional classname of the plugin instantiation target
    * @param dependencies array of listener names that the listener depends upon
    * @param properties supplimentary properties
    */
    public ListenerDirective( String name, String urn, String classname, 
      String[] dependencies, Properties properties )
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
        m_dependencies = dependencies;
        m_name = name;
        m_urn = urn;
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
    public String getURN()
    {
        return m_urn;
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
            else if( !equals( m_urn, object.m_urn ) )
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
        hash ^= super.hashValue( m_urn );
        return hash;
    }
}
