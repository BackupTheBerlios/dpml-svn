/*
 * Copyright 2004 Apache Software Foundation
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

package net.dpml.magic.model;

import java.net.URISyntaxException;

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.Artifact;

/**
 * Immutable data object that holds the name of a type and optionally 
 * an alias used in the construction of a link.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Type
{
   /**
    * The default type name.
    */
    public static final String DEFAULT_TYPE_NAME = "jar";

    private final String m_name;
    private final String m_alias;

    public Type()
    {
        this( DEFAULT_TYPE_NAME );
    }

    public Type( String name )
    {
        this( name, null );
    }

    public Type( String name, String alias )
    {
        if( null == name )
        {
            m_name = DEFAULT_TYPE_NAME;
        }
        else
        {
            m_name = name;
        }
        m_alias = alias;
    }

   /**
    * Return the alias declared for the type.
    * @return the alias
    */
    public String getAlias()
    {
        return m_alias;
    }

   /**
    * Return the name of the artifact.
    * @return the artifact name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return true if this type instance is equal to the supplied object.
    * @param other the object to compare against this instance
    * @return TRUE if equal
    */
    public boolean equals( final Object other )
    {
        if( other instanceof String )
        {
            return m_name.equals( other );
        }
        else if( !( other instanceof Type ) )
        {
            return false;
        }

        final Type type = (Type) other;
        if( ! getName().equals( type.getName() ) )
        {
            return false;
        }
        if( ( m_alias == null ) && null != type.getAlias() )
        {
            return false;
        }
        else
        {
            return m_alias.equals( type.getAlias() );
        }
    }

    public int hashCode()
    {
        int hash = m_name.hashCode();
        if( m_alias != null )
        {
            hash = hash ^ m_alias.hashCode();
        }
        return hash;
    }
}
