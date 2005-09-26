/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.part;

import java.io.Serializable;
import java.beans.Introspector;
import java.lang.reflect.Method;


/**
 * A descriptor that describes a value that must be placed
 * in components Context. It contains information about;
 * <ul>
 *   <li>key: the key that component uses to look up entry</li>
 *   <li>classname: the class/interface of the entry</li>
 *   <li>isOptional: true if entry is optional rather than required</li>
 * </ul>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptor.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public final class EntryDescriptor implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    public static final String GET = "get";

    public static String getEntryKey( Method method )
    {
        String name = method.getName();
        if( name.startsWith( GET ) )
        {
            String remainder = name.substring( GET.length() );
            return Introspector.decapitalize( remainder );
        }
        else
        {
            final String error = 
              "Unrecognized context accessor method signature ["
              + name 
              + "]";
            throw new IllegalArgumentException( error );
        }
    }

    public static final boolean OPTIONAL = true;
    public static final boolean REQUIRED = false;

    /**
     * The name the component uses to lookup entry.
     */
    private final String m_key;

    /**
     * The class/interface of the Entry.
     */
    private final String m_classname;

    /**
     * True if entry is optional, false otherwise.
     */
    private final boolean m_optional;

    /**
     * Immutable state of the entry.
     */
    private final boolean m_volatile;

    /**
     * Construct an non-volotile required EntryDescriptor.
     * @param key the context entry key
     * @param classname the classname of the context entry
     * @exception NullPointerException if the key or type value are null
     */
    public EntryDescriptor( final String key, final String classname ) 
      throws NullPointerException
    {
        this( key, classname, false );
    }

    /**
     * Construct an non-volotile EntryDescriptor.
     * @param key the context entry key
     * @param classname the classname of the context entry
     * @param optional TRUE if this is an optional entry
     * @exception NullPointerException if the key or type value are null
     */
    public EntryDescriptor( final String key,
                            final String classname,
                            final boolean optional ) throws NullPointerException
    {
        this( key, classname, optional, false );
    }

    /**
     * Construct an EntryDescriptor.
     * @param key the context entry key
     * @param classname the classname of the context entry
     * @param optional TRUE if this is an optional entry
     * @param isVolatile TRUE if the entry is consider to be immutable
     * @exception NullPointerException if the key or type value are null
     */
    public EntryDescriptor( final String key,
                            final String classname,
                            final boolean optional,
                            final boolean isVolatile ) throws NullPointerException
    {
        if ( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if ( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        m_key = key;
        m_classname = classname;
        m_optional = optional;
        m_volatile = isVolatile;
    }

    /**
     * Return the key that Component uses to lookup entry.
     *
     * @return the key that Component uses to lookup entry.
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Return the type of value that is stored in the context entry.
     *
     * @return the context entry classname
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return true if entry is optional, false otherwise.
     *
     * @return true if entry is optional, false otherwise.
     */
    public boolean getOptional()
    {
        return m_optional;
    }

    /**
     * Return true if entry is optional, false otherwise.
     *
     * @return true if entry is optional, false otherwise.
     */
    public boolean isOptional()
    {
        return getOptional();
    }

    /**
     * Return true if entry is required, false otherwise.
     *
     * @return true if entry is required, false otherwise.
     */
    public boolean isRequired()
    {
        return !isOptional();
    }

    /**
     * Return true if entry is volotile.
     *
     * @return the volatile state of the entry
     */
    public boolean isVolatile()
    {
        return getVolatile();
    }

    /**
     * Return true if entry is volotile.
     *
     * @return the volatile state of the entry
     */
    public boolean getVolatile()
    {
        return m_volatile;
    }
    
   /**
    * Test is the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        if( other == null )
        {
            return false;
        }

        if( ! ( other instanceof EntryDescriptor ) )
        {
            return false;
        }

        EntryDescriptor entry = (EntryDescriptor) other;

        if( m_optional != entry.m_optional )
        {
            return false;
        }

        if( m_volatile != entry.m_volatile )
        {
            return false;
        }

        if( !m_key.equals( entry.m_key ) )
        {
            return false;
        }

        if( !m_classname.equals( entry.m_classname ) )
        {
            return false;
        }

        return true;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = m_key.hashCode();
        hash ^= m_classname.hashCode();
        if( m_volatile )
        {
            hash =  hash + 1806621635;
        }
        else
        {
            hash =  hash - 1236275651;
        }
        if( m_optional )
        {
            hash =  hash + 1232368545;
        }
        else
        {
            hash =  hash - 923798133;
        }
        return hash;
    }
}
