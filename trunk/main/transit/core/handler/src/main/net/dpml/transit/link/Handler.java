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

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/** link: URL protocol handler.
 * <p>
 *   The <strong>link:</strong> protocol is similar to the symlink concept
 *   in Linux/Unix, where something that looks like a file points to another
 *   file. In this case, the link URL points to another URL. The resolution
 *   of the link translation is picked up from managed Transit server(s)
 * </p>
 */
public class Handler extends URLStreamHandler
{
   /**
    * Creation of a new transit link: protocol handler.
    */
    public Handler()
    {
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

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
        return new LinkURLConnection( url );
    }
}
