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

package net.dpml.library.info;

import java.util.Properties;

import net.dpml.library.Type;

import net.dpml.lang.AbstractDirective;

import org.w3c.dom.Element;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypeDirective extends DataDirective implements Type
{
    private final String m_name;
    private final boolean m_alias;
    
   /**
    * Creation of a new type directive.
    * @param name the name
    */
    public TypeDirective( String name )
    {
        this( name, false );
    }
    
   /**
    * Creation of a new type directive.
    * @param name the name
    * @param alias alias production policy
    */
    public TypeDirective( String name, boolean alias )
    {
        this( (Element) null, name, alias );
    }
    
   /**
    * Creation of a new type directive.
    * @param element DOM element defining the type 
    * @param name the name
    * @param alias alias production policy
    */
    public TypeDirective( Element element, String name, boolean alias )
    {
        super( element );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        m_alias = alias;
    }
    
   /**
    * Creation of a new generic type directive.
    * @param element DOM element defining the type 
    * @param name the name
    * @param alias alias production policy
    */
    public TypeDirective( String name, boolean alias, Properties properties )
    {
        super( properties );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        m_alias = alias;
    }
    
   /**
    * Return the type name.
    * @return the name
    */
    public String getID()
    {
        return m_name;
    }
    
   /**
    * Return the alias production policy.
    * @return true if this type is associated with an alias
    */
    public boolean getAlias()
    {
        return m_alias;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof TypeDirective ) )
        {
            TypeDirective object = (TypeDirective) other;
            if( !m_name.equals( object.m_name ) )
            {
                return false;
            }
            else
            {
                return ( m_alias == object.m_alias );
            }
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
        if( m_alias )
        {
            hash ^= 1298657;
        }
        hash ^= m_name.hashCode();
        return hash;
    }
    
   /**
    * Return a string representation of the type.
    * @return the string value
    */
    public String toString()
    {
        return "type:" + m_name;
    }
}
