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

package org.apache.avalon.meta.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * A descriptor describing the Context that the Component
 * is passed to describe information about Runtime environment
 * of Component. It contains information such as;
 * <ul>
 *   <li>classname: the classname of the Context type if it
 *       differs from base Context class (ie BlockContext).</li>
 *   <li>entries: a list of entries contained in context</li>
 * </ul>
 *
 * <p>Also associated with each Context is a set of arbitrary
 * attributes that can be used to store extra information
 * about Context requirements.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContextDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ContextDescriptor extends Descriptor
{
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

   /**
    * The context entry key for accessing a component name.
    */
    public static final String NAME_KEY =
            "urn:avalon:name";

   /**
    * The context entry key for accessing a component partition name.
    */
    public static final String PARTITION_KEY =
            "urn:avalon:partition";

   /**
    * The context entry key for accessing a component home directory.
    */
    public static final String HOME_KEY =
            "urn:avalon:home";

   /**
    * The context entry key for accessing a component temporary directory.
    */
    public static final String TEMP_KEY =
            "urn:avalon:temp";

   /**
    * The context entry key for accessing a component classloader.
    */
    public static final String CLASSLOADER_KEY =
            "urn:avalon:classloader";

   /**
    * Context attribute key used to declare a custom contextualization
    * interface.
    */
    public static final String STRATEGY_KEY =
            "urn:avalon:context.strategy";

   /**
    * Context interface classname.
    */
    public static final String AVALON_CONTEXT_CLASSNAME =
            "org.apache.avalon.framework.context.Context";

    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    private final String m_classname;

    private final EntryDescriptor[] m_entries;

    //---------------------------------------------------------
    // constructors
    //---------------------------------------------------------

    /**
     * Create a standard descriptor without attributes.
     * @param entries the set of entries required within the context
     */
    public ContextDescriptor( final EntryDescriptor[] entries )
    {
        this( null, entries, null );
    }

    /**
     * Create a descriptor without attributes.
     * @param classname the classname of a castable interface 
     * @param entries the set of entries required within the context
     */
    public ContextDescriptor( final String classname,
                              final EntryDescriptor[] entries )
    {
        this( classname, entries, null );
    }

    /**
     * Create a descriptor.
     * @param classname the classname of a castable interface 
     * @param entries the set of entries required within the context
     * @param attributes supplimentary attributes associated with the context
     * @exception NullPointerException if the entries argument is null
     */
    public ContextDescriptor( final String classname,
                              final EntryDescriptor[] entries,
                              final Properties attributes )
            throws NullPointerException, IllegalArgumentException
    {
        super( attributes );

        if ( null == entries )
        {
            throw new NullPointerException( "entries" );
        }

        if ( null == classname )
        {
            m_classname = AVALON_CONTEXT_CLASSNAME;
        }
        else
        {
            m_classname = classname;
        }
        m_entries = entries;
    }

    //---------------------------------------------------------
    // implementation
    //---------------------------------------------------------

    /**
     * Return the classname of the context
     * object interface that the supplied context argument
     * supports under a type-safe cast.
     *
     * @return the reference descriptor.
     */
    public String getContextInterfaceClassname()
    {
        return m_classname;
    }

    /**
     * Return the local entries contained in the context.
     *
     * @return the entries contained in the context.
     */
    public EntryDescriptor[] getEntries()
    {
        return m_entries;
    }

    /**
     * Return the entry with specified alias.  If the entry
     * does not declare an alias the method will return an 
     * entry with the matching key.
     *
     * @param alias the context entry key to lookup
     * @return the entry with specified key.
     */
    public EntryDescriptor getEntry( final String alias )
    {
        if ( null == alias )
        {
            throw new NullPointerException( "alias" );
        }

        for ( int i = 0; i < m_entries.length; i++ )
        {
            final EntryDescriptor entry = m_entries[i];
            if( entry.getAlias().equals( alias ) )
            {
                return entry;
            }
        }

        for ( int i = 0; i < m_entries.length; i++ )
        {
            final EntryDescriptor entry = m_entries[i];
            if( entry.getKey().equals( alias ) )
            {
                return entry;
            }
        }

        return null;
    }

    /**
     * Returns a set of entry descriptors resulting from a merge of the descriptors
     * container in this descriptor with the supplied descriptors.
     *
     * @param entries the entries to merge
     * @return the mergerged set of entries
     * @exception IllegalArgumentException if a entry conflict occurs
     */
    public EntryDescriptor[] merge( EntryDescriptor[] entries )
            throws IllegalArgumentException
    {
        for ( int i = 0; i < entries.length; i++ )
        {
            EntryDescriptor entry = entries[i];
            final String key = entry.getKey();
            EntryDescriptor local = getEntry( entry.getKey() );
            if ( local != null )
            {
                if ( !entry.getClassname().equals( local.getClassname() ) )
                {
                    final String error =
                            "Conflicting entry type for key: " + key;
                    throw new IllegalArgumentException( error );
                }
            }
        }

        return join( entries, getEntries() );
    }

    private EntryDescriptor[] join( EntryDescriptor[] primary, EntryDescriptor[] secondary )
    {
        List list = new ArrayList( primary.length + secondary.length );
        list.addAll( Arrays.asList( primary ) );
        list.addAll( Arrays.asList( secondary ) );
        return (EntryDescriptor[]) list.toArray( new EntryDescriptor[0] );
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        boolean isEqual = super.equals( other );
        if( isEqual ) isEqual = other instanceof ContextDescriptor;
        if( isEqual )
        {
            ContextDescriptor entity = (ContextDescriptor) other;
            isEqual = isEqual && m_classname.equals( entity.m_classname );
            for( int i=0; i<m_entries.length; i++ )
            {
                isEqual = isEqual && m_entries[i].equals( entity.m_entries[i] );
            }
        }
        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash >>>= 7;
        hash ^= m_classname.hashCode();
        for( int i=0; i<m_entries.length; i++ )
        {
            hash >>>= 7;
            hash ^= m_entries[i].hashCode();
        }
        return hash;
    }
}
