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

/**
 * A ApplicationRegistry maintains persistent records of available application profiles.
 */
public interface RegistryStorage
{
   /**
    * Return the inital array of application storage instances within the store.
    * @return the inital storage unit array
    */
    ApplicationStorage[] getInitialApplicationStorageArray();

   /**
    * Get an identified storage unit.
    * @param id the storage unit id
    * @return the storage unit
    */
    ApplicationStorage getApplicationStorage( String id );
}
