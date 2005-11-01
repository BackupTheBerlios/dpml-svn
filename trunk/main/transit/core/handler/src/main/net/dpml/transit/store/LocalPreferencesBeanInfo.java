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

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.PropertyDescriptor;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * An implementation of Preferences based on java.util.Properties suitable
 * for scenarios where the persistent lifetime is limited to the lifetype 
 * of the JVM.
 */
public final class LocalPreferencesBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
   /**
    * Creation of a bean descriptor.
    * @return the bean descriptor
    */
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
   /**
    * Return the property descriptors.
    * @return the property descriptors
    */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return new PropertyDescriptor[0];
    }
    
    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( LocalPreferences.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new LocalPreferencesPersistenceDelegate() );
        return descriptor;
    }
    
    private static class LocalPreferencesPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Instantiate an expression.
        * @param old the old value
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            if( null == old )
            {
                return null;
            }
            
            try
            {
                Preferences prefs = (Preferences) old;
                Object[] args = new Object[ 4 ];
                args[0] = prefs.absolutePath();
                args[1] = decomposeEntries( prefs );
                args[2] = decomposeNodes( prefs );
                args[3] = new Boolean( !prefs.isUserNode() );
                return new Expression( old, LocalPreferencesFactory.class, "parse", args );
            }
            catch( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
        
       /**
        * Return an object[] in the form { String name, String[][] entries, Object[] nodes, Boolean system ); 
        */
        private Object[] getNodeStructure( Preferences prefs ) throws BackingStoreException
        {
            Object[] array = new Object[3];
            array[0] = prefs.name();
            array[1] = decomposeEntries( prefs );
            array[2] = decomposeNodes( prefs );
            return array;
        }
        
        private Object[] decomposeEntries( Preferences prefs ) throws BackingStoreException
        {
            final String[] keys = prefs.keys();
            int n = keys.length;
            final Object[] args = new Object[n];
            for( int i=0; i<n; i++ )
            {
                final String key = keys[i];
                final String[] pair = new String[2];
                pair[0] = key;
                pair[1] = prefs.get( key, null );
                args[i] = pair;
            }
            return args;
        }
        
        private Object[] decomposeNodes( Preferences prefs ) throws BackingStoreException
        {
            String[] names = prefs.childrenNames();
            int n = names.length;
            Object[] nodes = new Object[ n ];
            for( int i=0; i<n; i++ )
            {
                String name = names[i];
                Preferences p = prefs.node( name );
                nodes[i] = getNodeStructure( p );
            }
            return nodes;
        }
        
    }
    
}

