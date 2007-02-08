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

package dpml.library.info;

import java.util.Properties;

import dpml.util.ObjectUtils;

import dpml.library.Type;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypeDirective extends AbstractDirective
{
    private final String m_id;
    private final String m_version;
    private final String m_source;
    private final boolean m_alias;
    private final boolean m_test;
    private final String m_name;
    
   /**
    * Creation of a new type directive.
    * @param id the type id
    */
    public TypeDirective( String id )
    {
        this( id, null );
    }
    
   /**
    * Creation of a new type directive.
    * @param id the type id
    * @param version alias version
    */
    public TypeDirective( String id, String version )
    {
        this( ProductionPolicy.DELIVERABLE, id, null, version, false, null, null );
    }
    
   /**
    * Creation of a new generic type directive.
    * @param id the type id
    * @param version alias version
    * @param properties supplimentary properties 
    */
    public TypeDirective( 
      ProductionPolicy policy, String id, String name, String version, boolean alias, String source, Properties properties )
    {
        super( properties );
        
        if( null == policy )
        {
            throw new NullPointerException( "policy" );
        }
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        
        m_id = id;
        m_name = name;
        m_version = version;
        m_source = source;
        m_alias = alias;
        m_test = ProductionPolicy.TEST.equals( policy );
    }
    
   /**
    * Return the type name.
    * @return the name
    */
    public String getID()
    {
        return m_id;
    }
    
   /**
    * Return the alias version.
    * @return the alias version
    */
    public String getVersion()
    {
        return m_version;
    }
    
   /**
    * Return the type source relative to the project basedir
    * @return the source relative file path
    */
    public String getSource()
    {
        return m_source;
    }
    
   /**
    * Return the test flag.
    * @return the test flag
    */
    public boolean getTest()
    {
        return m_test;
    }
    
   /**
    * Return the alias production flag.
    * @return the alias flag
    */
    public boolean getAliasProduction()
    {
        return m_alias;
    }
    
   /**
    * Return the overriding name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
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
            if( !m_id.equals( object.m_id ) )
            {
                return false;
            }
            if( !equals( m_version, object.m_version ) )
            {
                return false;
            }
            else
            {
                return equals( m_name, object.m_name );
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
        int hash = getClass().hashCode();
        hash ^= hashValue( m_id );
        hash ^= hashValue( m_name );
        return hash;
    }
    
   /**
    * Return a string representation of the type.
    * @return the string value
    */
    public String toString()
    {
        if( null == m_name )
        {
            return "type:" + m_id;
        }
        else
        {
            return "type:" + m_id + ":" + m_name;
        }
    }
    
}
