/* 
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.monitor;
 
/**
 * Generic interface implemented by classes that support multicast distribution
 * of monotor events to registered monitors.
 */
public interface Router
{
   /**
    * Add a monitor to the set of monitors managed by the router.
    * @param monitor the monitor to add
    */
    void addMonitor( Monitor monitor );
    
   /**
    * Remove a monitor from the set of monitors managed by the router.
    * @param monitor the monitor to remove
    */
    void removeMonitor( Monitor monitor );

}
 