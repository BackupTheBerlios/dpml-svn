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
 * Transit management interface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@MXBean
public interface TransitManager
{
   /**
    * Return the Transit home directory.
    * @return the home directory
    */
    String getHome();
    
   /**
    * Return the Transit data directory.
    * @return the data directory
    */
    String getData();
    
   /**
    * Return the Transit prefs directory.
    * @return the prefs directory
    */
    String getPrefs();
    
   /**
    * Return the Transit share directory.
    * @return the share directory
    */
    String getShare();
    
   /**
    * Return the Transit version.
    * @return the version string
    */
    String getVersion();
    
   /**
    * Return the Transit proxy host
    * @return the host name
    */
    String getProxyHost();
    
}