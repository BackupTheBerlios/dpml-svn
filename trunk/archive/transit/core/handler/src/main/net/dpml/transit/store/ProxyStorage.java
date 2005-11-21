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

import java.net.PasswordAuthentication; 
import java.net.URL;

/**
 * Contract implemented by objects that provide persistent storage of 
 * Transit proxy settings.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ProxyStorage
{
   /**
    * Return the proxy host URL.
    * @return the proxy host url
    */
    URL getHost();

   /**
    * Return the proxy authentication credentials.
    * @return the credentials
    */
    PasswordAuthentication getAuthentication();

   /**
    * Return the array of proxy host excludes.
    * @return the proxy excludes
    */
    String[] getExcludes();

   /**
    * Set the proxy host value.
    * @param host the new proxy host value
    */
    void setHost( URL host );

   /**
    * Set the proxy excludes value.
    * @param excludes the new proxy excludes value
    */
    void setExcludes( String[] excludes );

   /**
    * Set the proxy host authentication credentials.
    * @param auth the password authentication credentials
    */
    void setAuthentication( PasswordAuthentication auth );

   /**
    * Update the persistent storage object with the aggregates data collection.
    * @param host the new proxy host value
    * @param auth the new proxy password authentication credentials
    * @param excludes the new proxy host excludes value
    */
    void saveProxySettings( URL host, PasswordAuthentication auth, String[] excludes );
}
