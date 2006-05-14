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

import org.w3c.dom.Element;

/**
 * Definition of a generic type production assertion.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class GenericTypeDirective extends TypeDirective
{
    private final Properties m_properties;
    
   /**
    * Creation of a new generic type directive.
    * @param element the DOM element definition
    * @param id the type identifier
    */
    public GenericTypeDirective( Element element, String id, Properties properties )
    {
        this( element, id, false, properties );
    }
    
   /**
    * Creation of a new generic type directive.
    * @param element the DOM element definition
    * @param id the type identifier
    * @param alias alias production policy
    */
    public GenericTypeDirective( Element element, String id, boolean alias, Properties properties )
    {
        super( element, id, alias );
        
        if( null == properties )
        {
            m_properties = new Properties();
        }
        else
        {
            m_properties = properties;
        }
    }
    
   /**
    * Return the associated type properties.
    * @return the properties
    */
    public Properties getProperties()
    {
        return m_properties;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof GenericTypeDirective ) )
        {
            GenericTypeDirective object = (GenericTypeDirective) other;
            return m_properties.equals( object.m_properties );
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
        hash ^= m_properties.hashCode();
        return hash;
    }
}
