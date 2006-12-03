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

import net.dpml.lang.Version;

import net.dpml.library.Type;

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
    private final Version m_version;
    
   /**
    * Creation of a new type directive.
    * @param name the name
    */
    public TypeDirective( String name )
    {
        this( name, null );
    }
    
   /**
    * Creation of a new type directive.
    * @param name the name
    * @param version alias version
    */
    public TypeDirective( String name, Version version )
    {
        this( (Element) null, name, version );
    }
    
   /**
    * Creation of a new type directive.
    * @param element DOM element defining the type 
    * @param id the type id
    * @param version alias version
    */
    public TypeDirective( Element element, String id, Version version )
    {
        super( element );
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        m_name = id;
        m_version = version;
    }
    
   /**
    * Creation of a new generic type directive.
    * @param id the type id
    * @param version alias version
    * @param properties supplimentary properties 
    */
    public TypeDirective( String id, Version version, Properties properties )
    {
        super( properties );
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        m_name = id;
        m_version = version;
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
    * Return the alias version.
    * @return the alias version
    */
    public Version getVersion()
    {
        return m_version;
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
                return equals( m_version, object.m_version );
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
        hash ^= hashValue( m_name );
        hash ^= hashValue( m_version );
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
