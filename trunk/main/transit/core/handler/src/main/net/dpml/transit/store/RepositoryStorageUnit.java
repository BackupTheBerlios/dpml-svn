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

import java.net.URI;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.rmi.RemoteException;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.Date;

import net.dpml.transit.store.CodeBaseStorage;

/**
 * The RepositoryStorageUnit maintains the persistent settings for the 
 * plugin repository service.
 */
class RepositoryStorageUnit extends CodeBaseStorageUnit implements CodeBaseStorage
{
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new repository stroage unit.
    * @param prefs the preferences node
    */
    RepositoryStorageUnit( Preferences prefs )
    {
        super( prefs );
    }
}

