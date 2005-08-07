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

    private static final Preferences SYSTEM = new LocalPreferences( null, "" );
    private static final Preferences USER = new LocalPreferences( null, "" );

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
}
