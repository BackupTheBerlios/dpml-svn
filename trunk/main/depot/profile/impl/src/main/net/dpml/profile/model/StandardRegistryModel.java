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

package net.dpml.profile.model;

import java.rmi.RemoteException;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.Logger;

import net.dpml.profile.ApplicationRegistry;

/**
 * Plugin implements of the DepotModel.
 */
public class StandardRegistryModel extends RegistryModel implements ApplicationRegistry
{
   /**
    * Plugin class used to handle the establishment of the DepotProfile.  
    * 
    * @param logger the assigned logging channel
    * @exception NullPointerException if the logging channel is null
    * @exception DuplicateKeyException if the registry store contains a duplicate key
    * @exception RemoteException if a remote exception occurs
    */
    public StandardRegistryModel( Logger logger ) 
      throws NullPointerException, DuplicateKeyException, RemoteException
    {
         super( logger, new RegistryStorageUnit() );
    }
}
