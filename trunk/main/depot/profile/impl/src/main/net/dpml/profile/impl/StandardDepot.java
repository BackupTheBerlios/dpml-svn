/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.profile.impl;

import java.rmi.RemoteException;
import java.util.prefs.Preferences;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.UnknownKeyException;

import net.dpml.profile.unit.DepotStorageUnit;

/**
 * Plugin implements of the DepotModel.
 */
public class StandardDepot extends DefaultDepotProfile
{
    public StandardDepot( Logger logger, Preferences prefs ) 
      throws NullPointerException, DuplicateKeyException, RemoteException
    {
         super( logger, new DepotStorageUnit( prefs ) );
    }
}
