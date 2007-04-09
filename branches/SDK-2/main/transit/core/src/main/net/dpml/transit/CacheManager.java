/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.transit;

import javax.management.MXBean;

/** 
 * Cache management interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@MXBean
public interface CacheManager
{
   /**
    * Return the Transit cache directory path.
    * @return the cache path
    */
    String getPath();
    
   /**
    * Return the Transit cache directory location as a string.
    * @return the cache file value
    */
    String getDirectory();
    
   /**
    * Return the Transit cache layout id.
    * @return the cache layout identifier
    */
    String getLayoutID();
    
   /**
    * Return the hosts assigned in this Transit configuration
    * @return the host manager array
    */
    HostManager[] getHosts();
}