/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.transit.store;

import java.util.Hashtable;
import java.util.Properties;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * An implementation of Preferences based on java.util.Properties suitable
 * for scenarios where the persistent lifetime is limited to the lifetype 
 * of the JVM.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LocalPreferences extends AbstractPreferences
{
    private final Properties m_properties;
    private Hashtable m_table = new Hashtable();
    private final boolean m_system;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Constructs a new local user preferences object.
    * @param parent the parent node
    * @param name the preferences node name
    */
    public LocalPreferences( LocalPreferences parent, String name )
    {
        this( parent, name, false );
    }

   /**
    * Constructs a new local preferences object.
    * @param parent the parent node
    * @param name the preferences node name
    * @param system TRUE if this is a system preference tree
    */
    public LocalPreferences( LocalPreferences parent, String name, boolean system )
    {
        super( parent, name );
        m_properties = new Properties();
        m_system = system;
    }

    // ------------------------------------------------------------------------
    // LocalPreferences
    // ------------------------------------------------------------------------

   /**
    * Return true if this is a user node else false.
    * @return the user node status
    */
    public boolean isUserNode()
    {
        return !m_system;
    }

   /**
    * Set an attribute value.
    * @param key the attribute key
    * @param value the attribute value
    */
    protected void putSpi( String key, String value )
    {
        getProperties().setProperty( key, value );
    }

   /**
    * Get an attribute value.
    * @param key the attribute key
    * @return the attribute value
    */
    protected String getSpi( String key )
    { 
        return getProperties().getProperty( key );
    }

   /**
    * Remove an attribute.
    * @param key the attribute key
    */
    protected void removeSpi( String key )
    {
        getProperties().remove( key );
    }

   /**
    * Remove the preferences node.
    * @exception BackingStoreException if a backing store exception occurs 
    */
    protected void removeNodeSpi() throws BackingStoreException
    {
        LocalPreferences parent = (LocalPreferences) parent();
        if( null != parent )
        {
            parent.removeNode( name() );
        }
    }

   /**
    * Return the set of attribute keys in this preferences node.
    * @return the array of attribute names
    * @exception BackingStoreException if a backing store exception occurs 
    */
    protected String[] keysSpi() throws BackingStoreException 
    {
        return (String[]) m_properties.keySet().toArray( new String[0] );
    }

   /**
    * Return an array of child node names.
    * @return the array of child node names
    * @exception BackingStoreException if a backing store exception occurs 
    */
    protected String[] childrenNamesSpi() throws BackingStoreException 
    {
        return (String[]) m_table.keySet().toArray( new String[0] );
    }

   /**
    * Return a named child node.
    * @param name the the child node name
    * @return the the child node
    */
    protected AbstractPreferences childSpi( String name )
    {
        synchronized( m_table )
        {
            LocalPreferences prefs = (LocalPreferences) m_table.get( name );
            if( null != prefs )
            {
                return prefs;
            }
            else
            {
                LocalPreferences p = new LocalPreferences( this, name, m_system );
                m_table.put( name, p );
                return p;
            }
        }
    }

   /**
    * Synchronize changes.
    * @exception BackingStoreException if a backing store exception occurs 
    */
    protected void syncSpi() throws BackingStoreException
    {
    }

   /**
    * Flush changes.
    * @exception BackingStoreException if a backing store exception occurs 
    */
    protected void flushSpi() throws BackingStoreException
    {
    }

    void removeNode( String name )
    {
        synchronized( m_table )
        {
            m_table.remove( name );
        }
    }

    private Properties getProperties()
    {
        return m_properties;
    }
}