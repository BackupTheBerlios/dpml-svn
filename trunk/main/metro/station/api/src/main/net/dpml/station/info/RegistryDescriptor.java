/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.station.info;

import java.io.Serializable;
import java.net.URI;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.util.Arrays;

/**
 * The RegistryDescriptor is immutable datastructure used to 
 * hold the state of an application registry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RegistryDescriptor extends AbstractDescriptor
{
   /**
    * The default storage path.
    */
    public static final String DEFAULT_STORAGE_PATH = "local:xml:dpml/station/registry";
    
   /**
    * The default storage uri.
    */
    public static final URI DEFAULT_STORAGE_URI = createDefaultStorageURI();
    
    private final Entry[] m_entries;
    
   /**
    * Creation of a new registry descriptor.
    * @param entries an array of application entries
    */
    public RegistryDescriptor( Entry[] entries )
    {   
        if( null == entries )
        {
            throw new NullPointerException( "entries" );
        }
        for( int i=0; i<entries.length; i++ )
        {
            if( null == entries[i] )
            {
                throw new NullPointerException( "entry" );
            }
        }
        
        m_entries = entries;
    }
    
   /**
    * Returns the array of application descriptors.
    * 
    * @return the application entry array
    */
    public Entry[] getEntries()
    {
        return m_entries;
    }
    
    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object equivalent
     */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        else if( !( other instanceof RegistryDescriptor ) )
        {
            return false;
        }
        else
        {
            RegistryDescriptor descriptor = (RegistryDescriptor) other;
            return Arrays.equals( m_entries, descriptor.m_entries );
        }
    }
    
   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashArray( m_entries );
        return hash;
    }
    
   /**
    * Return a string representation of the registry.
    * @return the string value
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "[registry " );
        for( int i=0; i<m_entries.length; i++ )
        {
            if( i != 0 )
            {
                buffer.append( ", " );
            }
            Entry entry = m_entries[i];
            buffer.append( entry.toString() );
        }
        return buffer.toString();
    }
    
    private static URI createDefaultStorageURI()
    {
        try
        {
            return new URI( DEFAULT_STORAGE_PATH );
        }
        catch( Exception e )
        {
            return null; // will not happen
        }
    }
    
   /**
    * Binding of key to descriptor.
    */
    public static final class Entry implements Serializable
    {
        private final String m_key;
        private final ApplicationDescriptor m_descriptor;
      
       /**
        * Creation of a new entry.
        * @param key the profile key
        * @param descriptor the application descriptor
        */
        public Entry( String key, ApplicationDescriptor descriptor )
        {
            if( null == key )
            {
                throw new NullPointerException( "key" );
            }
            if( null == descriptor )
            {
                throw new NullPointerException( "descriptor" );
            }
            m_key = key;
            m_descriptor = descriptor;
        }
        
       /**
        * Return the entry key.
        * @return the key
        */
        public String getKey()
        {
            return m_key;
        }
        
       /**
        * Return the application descriptor.
        * @return the application descriptor
        */
        public ApplicationDescriptor getApplicationDescriptor()
        {
            return m_descriptor;
        }
        
       /**
        * Tests for equality. Two entries are considered equal if 
        * they have the same key and descriptor. 
        *
        * @param other the other object
        * @return the equality status
        */
        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else if( other instanceof Entry )
            {
                Entry entry = (Entry) other;
                if( !m_key.equals( entry.getKey() ) )
                {
                    return false;
                }
                else
                {
                    return m_descriptor.equals( entry.m_descriptor );
                }
            }
            else
            {
                return false;
            }
        }
        
       /** 
        * Compute the hashcode.
        * @return the hashcode value
        */
        public int hashCode()
        {
            int hash = m_key.hashCode();
            hash ^= m_descriptor.hashCode();
            return hash;
        }
        
       /**
        * Return a string representation of the registry.
        * @return the string value
        */
        public String toString()
        {
            return "[entry key=" 
              + m_key 
              + " descriptor=" 
              + m_descriptor.toString() 
              + " ]";
        }
    }
    
   /**
    * Entry bean info.
    */
    public static final class EntryBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
        
       /**
        * Return the bean descriptor.
        * @return the descriptor
        */
        public BeanDescriptor getBeanDescriptor()
        {
            return BEAN_DESCRIPTOR;
        }
        
        private static BeanDescriptor setupBeanDescriptor()
        {
            BeanDescriptor descriptor = new BeanDescriptor( Entry.class );
            descriptor.setValue( 
              "persistenceDelegate", 
              new DefaultPersistenceDelegate( 
                new String[]{"key", "applicationDescriptor"} ) );
            return descriptor;
        }
    }
}
