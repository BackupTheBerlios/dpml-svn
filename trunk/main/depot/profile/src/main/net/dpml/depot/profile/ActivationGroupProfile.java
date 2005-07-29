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

package net.dpml.depot.profile;

import java.rmi.RemoteException;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;


/**
 * A ActivationProfile maintains information about the configuration
 * of an activation profile.
 */
public interface ActivationGroupProfile extends Profile
{
    ActivationProfile[] getActivationProfiles() throws RemoteException;

    ActivationProfile getActivationProfile( String key ) throws UnknownKeyException, RemoteException;

    void addActivationProfile( ActivationProfile profile ) throws DuplicateKeyException, RemoteException;

    void removeActivationProfile( ActivationProfile profile ) throws RemoteException;

    void addActivationGroupListener( ActivationGroupListener listener ) throws RemoteException;

    void removeActivationGroupListener( ActivationGroupListener listener ) throws RemoteException;

}
