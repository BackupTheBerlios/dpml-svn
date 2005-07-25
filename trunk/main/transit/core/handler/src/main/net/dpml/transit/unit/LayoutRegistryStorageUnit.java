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

package net.dpml.transit.unit;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.Date;

import net.dpml.transit.store.LayoutRegistryHome;
import net.dpml.transit.store.LayoutStorage;

/**
 * The LayoutRegistryStorageUnit maintains persstent storage of 
 * the layout registry.  The persistent strategy employed by the implementation 
 * is based on the java.util.prefs Preferences object model.
 */
class LayoutRegistryStorageUnit extends CodeBaseStorageUnit implements LayoutRegistryHome
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Construction of a new layout registry storage unit.
    * @param prefs the preferences node
    */
    LayoutRegistryStorageUnit( Preferences prefs )
    {
        super( prefs );
    }

    // ------------------------------------------------------------------------
    // LayoutRegistryHome
    // ------------------------------------------------------------------------

   /**
    * Return the array of inital layout storage units.
    * @return the layout unit storage array
    */
    public LayoutStorage[] getInitialLayoutStores()
    {
        Preferences prefs = getPreferences();
        try
        {
            ArrayList list = new ArrayList();
            String[] names = prefs.childrenNames();
            for( int i=0; i<names.length; i++ )
            {
                String name = names[i];
                LayoutStorage layout = getLayoutStorage( name );
                list.add( layout );
            }
            return (LayoutStorage[]) list.toArray( new LayoutStorage[0] );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while constructing layout registry manager.";
            throw new BuilderException( error, e );
        }
    }

   /**
    * Return a layout storage unit given a storae unit identifier.  If the 
    * stroage unit does not exist an implementation shall create and return a 
    * net storage unit.
    * 
    * @return the layout storage unit
    */
    public LayoutStorage getLayoutStorage( String id ) throws BuilderException
    {
        Preferences prefs = getPreferences().node( id );
        return new LayoutStorageUnit( prefs );
    }
}
