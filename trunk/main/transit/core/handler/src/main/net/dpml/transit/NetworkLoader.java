/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman
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

import net.dpml.transit.Transit;
import net.dpml.transit.TransitException;
import net.dpml.transit.artifact.ArtifactException;
import net.dpml.transit.monitor.NetworkMonitorRouter;
import net.dpml.transit.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Nework loader utility.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class NetworkLoader
{
   /**
    * Network monitor router.
    */
    private NetworkMonitorRouter m_monitor;

   /**
    * Creation of a new network loader.
    * @exception TransitException if an error in transit system establishment occurs
    */
    public NetworkLoader()
        throws TransitException
    {
        m_monitor = Transit.getInstance().getNetworkMonitorRouter();
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

    /**
     * Retrieve a remote resource.
     *
     * @param url the of the file to retrieve
     * @param connection the url connection
     * @param destination where to store it
     * @return the lastModified date of the downloaded artifact.
     * @exception net.dpml.transit.ArtifactException if an artifact related errror occurs
     * @exception IOException if an IO error occurs
     */
    public Date loadResource( URL url, URLConnection connection, OutputStream destination )
        throws ArtifactException, IOException
    {
        if( connection instanceof HttpURLConnection )
        {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int code = httpConnection.getResponseCode();
            // test for 401 result (HTTP only)
            if ( code == HttpURLConnection.HTTP_UNAUTHORIZED )
            {
                throw new IOException( "Not authorized." );
            }
        }
        InputStream in = connection.getInputStream();
        int expected = connection.getContentLength();
        StreamUtils.copyStream( m_monitor, url, expected, in, destination, true );
        long remoteTimestamp = connection.getLastModified();
        return new Date( remoteTimestamp );
    }
}
