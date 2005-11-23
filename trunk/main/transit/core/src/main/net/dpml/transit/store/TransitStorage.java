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
 * The Home defines the persistent storage home for transit management configurations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface TransitStorage
{
   /**
    * Return the Transit storage instance identifier.
    * @return the transit persistent id
    */
    String getID();

   /**
    * Return the proxy strorage unit.
    * @return the proxy storage
    */
    ProxyStorage getProxyStorage();

   /**
    * Return the cache strorage unit.
    * @return the proxy storage
    */
    CacheHome getCacheHome();

   /**
    * Return the layout registry storage unit.
    * @return the layout registry storage
    */
    LayoutRegistryHome getLayoutRegistryHome();

   /**
    * Return the content registry storage unit.
    * @return the content registry storage
    */
    ContentRegistryHome getContentRegistryHome();

   /**
    * Return the repository codebase storage unit.
    * @return the repository system storage
    */
    //CodeBaseStorage getRepositoryStorage();

}
