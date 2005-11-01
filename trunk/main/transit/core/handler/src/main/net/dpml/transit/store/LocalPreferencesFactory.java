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

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * <p>A implementation of the Preferences services that provides persistence for 
 * the duration of the JVM.  
 * The implementation holds preferences attribue values in memory using Properties.
 * The implementation is suitable for the construction of preferences that for 
 * usage in non-shared scenarios (such as testcases or short running applications).
 * The preference implementation is established via declaration of the following
 * system property prior to preferences usage:</p>
 *
 * <pre>
 *   System.setProperty( 
 *          "java.util.prefs.PreferencesFactory", 
 *          "net.dpml.transit.store.LocalPreferencesFactory" );
 * </pre>
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class LocalPreferencesFactory implements PreferencesFactory
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    private static final Preferences SYSTEM = new LocalPreferences( null, "", true );
    private static final Preferences USER = new LocalPreferences( null, "", false );
    
    // ------------------------------------------------------------------------
    // PreferencesFactory
    // ------------------------------------------------------------------------

   /**
    * Return the root system preferences node.
    * @return the root node of the system prefs
    */
    public Preferences systemRoot()
    {
        return SYSTEM;
    }

   /**
    * Return the root user preferences node.
    * @return the root node of the user prefs
    */
    public Preferences userRoot()
    {
        return USER;
    }
    
    // ------------------------------------------------------------------------
    // special
    // ------------------------------------------------------------------------
    
   /**
    * Return a preferences instance using the supplied arguments.
    * @param path the preference node path
    * @param entries the preference attribute node array
    * @param system true if declaring the node as system scope
    * @return the new prefernces instance
    */
    public static Preferences parse( String path, Object[] entries, Object[] nodes, boolean system )
    {
        Preferences root = getRootPreferences( system );
        Preferences prefs = root.node( path );
        applyAttributes( prefs, entries );
        applyNodes( prefs, nodes );
        return prefs;
    }
    
    private static Preferences getRootPreferences( boolean system )
    {
        if( system )
        {
            return new LocalPreferences( null, "", true );
        }
        else
        {
            return new LocalPreferences( null, "", false );
        }
    }
    
    private static void applyAttributes( Preferences prefs, Object[] entries )
    {
        //System.out.println( "applying attributes to: " + prefs );
        for( int i=0; i<entries.length; i++ )
        {
            String[] entry = (String[]) entries[i];
            if( entry.length != 2 )
            {
                throw new IllegalArgumentException( 
                    "Invalid attribute array length - expecting 2 but encountered " + entry.length );
            }
            String key = entry[0];
            String value = entry[1];
            prefs.put( key, value );
        }
    }

    private static void applyNodes( Preferences prefs, Object[] nodes )
    {
        //System.out.println( "applying nodes to: " + prefs );
        for( int i=0; i<nodes.length; i++ )
        {
            Object[] node = (Object[]) nodes[i];
            String name = (String) node[0];
            Preferences p = prefs.node( name );
            String[][] entries = (String[][]) node[1];
            applyAttributes( p, entries );
            Object[] subnodes = (Object[]) node[2];
            applyNodes( p, subnodes );
        }
    }
}
