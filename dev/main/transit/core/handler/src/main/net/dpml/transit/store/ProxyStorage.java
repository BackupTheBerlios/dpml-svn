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
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface ProxyStorage
{
    URL getHost();

    PasswordAuthentication getAuthentication();

    String[] getExcludes();

    void setHost( URL host );

    void setExcludes( String[] excludes );

    void setAuthentication( PasswordAuthentication auth );

    void saveProxySettings( URL host, PasswordAuthentication auth, String[] excludes );
}
