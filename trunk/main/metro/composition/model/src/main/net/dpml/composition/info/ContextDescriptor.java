/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.composition.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.dpml.part.EntryDescriptor;

/**
 * A descriptor describing the context that a component
 * declares.  The context descriptor contains information about
 * context entries accessable via keys (typically mapped to 
 * get[Name] operations on a Context inner interface).
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ContextDescriptor implements Serializable
{
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    private final EntryDescriptor[] m_entries;

    //---------------------------------------------------------
    // constructors
    //---------------------------------------------------------

    /**
     * Creation of a new context descriptor.
     * @param entries the set of context entries
     */
    public ContextDescriptor( final EntryDescriptor[] entries )
    {
        if( null == entries )
        {
            m_entries = new EntryDescriptor[0];
        }
        else
        {
            m_entries = entries;
        }
    }

    //---------------------------------------------------------
    // implementation
    //---------------------------------------------------------

    /**
     * Return the entries contained in the context.
     *
     * @return the array of entries contained in the context.
     */
    public EntryDescriptor[] getEntryDescriptors()
    {
        return m_entries;
    }

    /**
     * Return the entry with specified key.
     *
     * @param key the context entry key
     * @return the entry matching the key of null if no matching entry
     * @exception NullArgumentException if the key argument is null.
     */
    public EntryDescriptor getEntryDescriptor( final String key )
        throws NullPointerException
    {
        if ( null == key )
        {
            throw new NullPointerException( "key" );
        }

        for ( int i = 0; i < m_entries.length; i++ )
        {
            final EntryDescriptor entry = m_entries[i];
            if( entry.getKey().equals( key ) )
            {
                return entry;
            }
        }

        return null;
    }

    /**
     * Returns a set of entry descriptors resulting from a merge of the descriptors
     * contained in this descriptor with the supplied descriptors.
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
            EntryDescriptor local = getEntryDescriptor( entry.getKey() );
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

        return join( entries, getEntryDescriptors() );
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
        if( null == other )
        {
            return false;
        }
        boolean isEqual = true;
        if( isEqual )
        {
            isEqual = other instanceof ContextDescriptor;
        }
        if( isEqual )
        {
            ContextDescriptor entity = (ContextDescriptor) other;
            for( int i = 0; i < m_entries.length; i++ )
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
        int hash = getClass().hashCode();
        for( int i = 0; i < m_entries.length; i++ )
        {
            hash = hash + 65152197;
            hash ^= m_entries[i].hashCode();
        }
        return hash;
    }
}
