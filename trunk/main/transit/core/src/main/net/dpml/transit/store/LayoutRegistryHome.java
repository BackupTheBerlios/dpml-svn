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
 * The LayoutRegistryHome is an interface implemented by objects
 * providing services supporting the management of a collection of 
 * layout configurations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface LayoutRegistryHome extends CodeBaseStorage
{
   /**
    * Return the array of inital layout storage units.
    * @return the layout unit storage array
    */
    LayoutStorage[] getInitialLayoutStores();

   /**
    * Return a layout storage unit given a storage unit identifier.  If the 
    * stroage unit does not exist an implementation shall create and return a 
    * net storage unit.
    *
    * @param id the layout storage unit id
    * @return the layout storage unit
    */
    LayoutStorage getLayoutStorage( String id );
}
