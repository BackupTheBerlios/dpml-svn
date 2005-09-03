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

import net.dpml.transit.Plugin;
import net.dpml.transit.Plugin.Category;

/**
 * Delcaration of a repository resource reference.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public final class ResourceRef
{
    private String m_key;
    private Policy m_policy;
    private Category m_tag;

   /**
    * Return a classifier int value given a classifier name.
    * @param category the category classifier name
    * @return the classifier value as int
    */
    public static Category getCategory( final String category )
    {
        if( "sys".equals( category ) )
        {
            return Plugin.ANY;
        }
        else if( "system".equals( category ) )
        {
            return Plugin.SYSTEM;
        }
        else if( "api".equals( category ) )
        {
            return Plugin.API;
        }
        else if( "spi".equals( category ) )
        {
            return Plugin.SPI;
        }
        else if( "impl".equals( category ) )
        {
            return Plugin.IMPL;
        }
        else
        {
            final String error = 
              "Unrecognized category [" + category + "].";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Return a classifier name given a classifier value.
    * @param category the category classifier value
    * @return the classifier name
    */
    public static String getCategoryName( final Category category )
    {
        if( category == Plugin.ANY )
        {
            return "any";
        }
        if( category == Plugin.SYSTEM )
        {
            return "system";
        }
        if( category == Plugin.API )
        {
            return "api";
        }
        else if( category == Plugin.SPI )
        {
            return "spi";
        }
        else if( category == Plugin.IMPL )
        {
            return "impl";
        }
        else
        {
            final String error = 
              "Unrecognized category [" + category + "].";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Creation of a new resource reference using ANY classifier nd depault policy.
    * @param key the resource key that the reference references
    */
    public ResourceRef( final String key )
    {
        this( key, new Policy(), Plugin.ANY );
    }

   /**
    * Creation of a new resource reference.
    * @param key the resource key that the reference references
    * @param policy the build/test/runtime policy
    * @param tag the classloader classifier tag
    */
    public ResourceRef( final String key, final Policy policy, final Category tag )
    {
        m_key = key;
        m_policy = policy;
        m_tag = tag;
    }

   /**
    * Return the resource reference key.
    * @return the key that this reference references
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Return the classloader classifier tag.
    * @return the tag value
    */
    public Category getTag()
    {
        return m_tag;
    }

   /**
    * Return the usage policy.
    * @return the policy value
    */
    public Policy getPolicy()
    {
        return m_policy;
    }

   /**
    * Test if this reference matches the supplied category.
    * @param category the category to match against
    * @return TRUE if the category matches
    */
    public boolean matches( final Category category )
    {
        if( ( Plugin.ANY == category ) || ( Plugin.ANY == m_tag ) )
        {
            return true;
        }
        else
        {
            return ( m_tag == category );
        }
    }

   /**
    * Test if this instance is equal to the supplied object.
    * @param other the other object
    * @return TRUE if equal
    */
    public boolean equals( final Object other )
    {
        if( !( other instanceof ResourceRef ) )
        {
            return false;
        }
        ResourceRef ref = (ResourceRef) other;
        if( !m_key.equals( ref.m_key ) )
        {
            return false;
        }
        if( !m_policy.equals( ref.m_policy ) )
        {
            return false;
        }
        if( m_tag != ref.m_tag )
        {
            return false;
        }
        return true;
    }

   /**
    * Return the hashcode for this instance.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = MAGIC_A;
        hash = hash ^ m_key.hashCode();
        hash = hash ^ m_policy.hashCode();
        hash = hash ^ ( MAGIC_B >> m_tag.hashCode() );
        return hash;
    }

   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "[resource key=\"" + getKey() + "\", " + m_policy + "]";
    }

    private static final int MAGIC_A = 926234653;
    private static final int MAGIC_B = 1876872534;

}
