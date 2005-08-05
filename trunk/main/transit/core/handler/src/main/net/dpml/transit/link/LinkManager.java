/*
 * Copyright 2005 Niclas Hedhman
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

package net.dpml.transit.link;

import java.io.IOException;

import java.net.URI;

/** 
 * The LinkManager is responsible for storing the mapping between a Link
 * instance and a URI.
 */
public interface LinkManager
{
   /** 
    * Sets the URI for the provided link URI.
    * The LinkManager is required to persist this information between
    * JVM restarts and should be persisted on a scope larger than a
    * single JVM, typically a host or a local area network. LinkManagers
    * are encouraged to establish other virtual scopes independent of
    * network topologies.
    *
    * @param link the uri of the link
    * @param target the uri of the target that the link redirects to
    * @exception IOException if an IO error occurs
    */
    void setTargetURI( URI link, URI target ) throws IOException;

   /** 
    * Returns the URI that the supplied link URI is referencing.
    * 
    * @param link the link uri
    * @return the target uri
    * @exception IOException if an IO error occurs
    */
    URI getTargetURI( URI link ) throws IOException;

}
