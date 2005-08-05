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

package net.dpml.transit;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.net.URL;

import java.util.Date;

/**
 * An interface that represents locations where the artifacts can be
 * downloaded.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface ResourceHost
{
    /** Downloads the given artifact to the directory indicated.
     * <p>
     *   The cachedir argument is the root cache directory, and the ResourceHost
     *   class is responsible for the creation of the directory structure of the
     *   group if nonexistent.
     * </p>
     * <p>
     *   If the knownOnly argument is true, then only attempt download if the
     *   group is known to exist on this resource host.
     * </p>
     * @param artifact the artifact that is requested to be downloaded.
     * @param dest The output stream where to write the downloaded content.
     * @exception IOException if an IO related error occurs
     * @return the lastModified date of the downloaded artifact.
     */
    Date download( Artifact artifact, OutputStream dest )
        throws IOException;

    /** Uploads the given file to the resource host as an artifact.
     *
     * @param artifact the artifact destination specification.
     * @param source The input stream from where to read the content to be uploaded.
     * @exception IOException if an IO related error occurs
     */
    void upload( Artifact artifact, InputStream source )
        throws IOException;

    /** Checks if the Artifact is present on the resource host.
     *
     * <p>
     *   Performs a check to see if the artifact exists on the resource host. If
     *   <i>knownOnly</i> is set to true, then the implementation will only
     *   consult the knownGroups table, and if found there, it is considered
     *   found without checking at the resource host itself. If <i>knownOnly</i>
     *   is false, however, a connection will be established to the resource
     *   host and a check of the actual resource existence.
     * </p>
     *
     * @param artifact the artifact for which the method checks its presence.
     * @param knownOnly does not perform a remote connection, and instead lookup
     *        the group table, and if not found there it will return false.
     *
     * @return true if the artifact can be located, false otherwise.
     */
    boolean checkPresence( Artifact artifact, boolean knownOnly );

    /** Returns the hostname of the resource host.
     *
     * <p>
     *   This does not include any of the path, but does include any port number
     *   of this resource host.
     * </p>
     * @return the hostname
     */
    String getHostName();

    /**
     * Returns the full host url.
     *
     * @return the host url
     */
    URL getURL();

    /**
     * Return true if the resource host has been enabled.
     *
     * @return true if the resource host is enabled, false if not.
     */
    boolean isEnabled();

    /** Returns true if the ResourceHost is considered trusted.
     * @return true if the host is trusted
     */
    boolean isTrusted();

    /** Returns the priority of the resource host.
     *
     *  A high number indicates a more important host that should take precendence
     *  over a host with lower number.
     * @return the host priority
     */
    int getPriority();
}
