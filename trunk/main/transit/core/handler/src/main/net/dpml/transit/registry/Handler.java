/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.transit.registry;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.rmi.registry.Registry;

/** 
 * The <code>registry</code> protocol references an remote resource
 * published under an RMI Registry.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Handler extends URLStreamHandler
{
   /**
    * Creation of a new registry protocol handler.
    */
    public Handler()
    {
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Returns the default registry port.
    * @return the registry default port
    */
    protected int getDefaultPort()
    {
        return Registry.REGISTRY_PORT;
    }

    /**
     * Opens a connection to the specified URL.
     *
     * @param url A URL to open a connection to.
     * @return The established connection.
     * @throws IOException If a connection failure occurs.
     */
    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        return new RegistryURLConnection( url );
    }
}
