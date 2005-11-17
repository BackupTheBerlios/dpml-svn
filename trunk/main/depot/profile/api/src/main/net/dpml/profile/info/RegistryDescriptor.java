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

package net.dpml.profile.info;

import java.net.URI;
import java.util.Map;
import java.util.Hashtable;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;

/**
 * The RegistryDescriptor is immutable datastructure used to 
 * hold the state of an application registry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RegistryDescriptor extends AbstractDescriptor
{
    public static final String DEFAULT_STORAGE_PATH = "local:xml:dpml/metro/registry";
    
    public static final URI DEFAULT_STORAGE_URI = createDefaultStorageURI();
    
    private final Entry[] m_entries;
    
   /**
    * Creation of a new registry descriptor.
    * @param entries an array of application entries
    */
    public RegistryDescriptor( Entry[] entries )
    {   
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
    
    public static final class Entry
    {
        final String m_key;
        final ApplicationDescriptor m_descriptor;
        
        public Entry( String key, ApplicationDescriptor descriptor )
        {
            m_key = key;
            m_descriptor = descriptor;
        }
        
        public String getKey()
        {
            return m_key;
        }
        
        public ApplicationDescriptor getApplicationDescriptor()
        {
            return m_descriptor;
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
