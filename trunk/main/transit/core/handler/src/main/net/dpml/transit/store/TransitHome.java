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

package net.dpml.transit.store;

/**
 * The Home defines the persistent storage home for a collection of persistent transit
 * configurations.  
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface TransitHome
{
   /**
    * Return the inital collection of transit storage unit instances.
    * @return the array of transit starage instances
    */
    TransitStorage[] getInitialTransitStores();

   /**
    * Return a Transit storage profile relative to the supplied identifier. If 
    * the storage identifier is not recognized, an implementation will construct 
    * a new profile using the supplied identifier.
    *
    * @param id the tranist configuration profile identifier
    * @return the corresponding stoarage unit
    */
    TransitStorage getTransitStorage( String id );

   /**
    * Return the set of storage profiles managed by the home implementing this interface.
    * @return the set of available transit profile names
    */
    String[] getProfileNames();
}
