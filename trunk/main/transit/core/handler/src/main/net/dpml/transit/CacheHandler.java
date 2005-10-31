/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.dpml.transit.artifact.ArtifactAlreadyExistsException;

/**
 * The CacheHandler interface defines the contract for classes capable of
 * supporting the cache management aspects of a transit system.  Cache management
 * encompasses the retreval of resources based on artifact identifiers.  A cache
 * manager implementations may provide varying levels of quality-of-services.
 * Selection of a cache manager is defined under the transit cache configuration
 * properties.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface CacheHandler
{
   /**
    * Return the cache directory.
    * @param artifact the artifact to resolve
    * @return the working cache directory
    * @exception IOException if an IO error occurs
    */
    File getLocalFile( Artifact artifact ) throws IOException;

   /**
    * Initialization of the cache handler.  This operation is invoked by
    * the transit context following the establishment of bootstrap
    * services.  During initialization the implementation loads any custom
    * resource hosts.
    *
    * @exception IOException if an IO error occurs
    */
    void initialize() throws IOException;

    /**
     * Attempts to download and cache a remote artifact using a set of remote
     * repositories.
     *
     * @param artifact the artifact to retrieve and cache
     * @return a file referencing the local resource
     * @exception IOException if an IO error occurs
     * @exception TransitException is a transit system error occurs
     */
    InputStream getResource( Artifact artifact )
        throws IOException, TransitException;

    /**
     * Attempts to download and cache a remote artifact using a set of remote
     * repositories.
     * <p>
     *   This method allows an internal reference to be passed to the
     *   cache handler and it is expected to return the InputStream of the
     *   internal item inside Jar/Zip files. If this method is called, the
     *   implementation can assume that the artifact is a Zip file.
     * </p>
     *
     * @param artifact the artifact to retrieve and cache
     * @param internalReference referencing a item within the artifact. This
     *        argument may start with "!" or "!/", which should be ignored.
     * @return a file referencing the local resource
     * @exception IOException if an IO error occurs
     * @exception TransitException is a transit system error occurs
     */
    InputStream getResource( Artifact artifact, String internalReference )
        throws IOException, TransitException;

    /** Creates an output stream to where the artifact content can be written
     *  to.
     * <p>
     *   If the artifact already exists, a <code>ArtifactAlreadyExistsException</code>
     *   will be thrown. If the directory doesn't exists, it will be created.
     *   The CacheHandler is responsible to recognize the completion of the
     *   writes through the <code>close()</code> method in the output stream,
     *   and do any post-processing there.
     * </p>
     * @param artifact the artifact
     * @return an output stream
     * @exception NullArgumentException if the artifact argument is null.
     * @exception ArtifactAlreadyExistsException if the artifact already exists in the cache.
     * @exception IOException if an IO error occurs
     */
    OutputStream createOutputStream( Artifact artifact )
        throws NullArgumentException, ArtifactAlreadyExistsException, IOException;

   /**
    * Return the layout used by the cache.
    * @return the cache layout
    */
    Layout getLayout();

   /**
    * Return the layout registry.
    * @return the layout registry.
    */
    LayoutRegistry getLayoutRegistry();
    
   /**
    * Return the current cache directory.
    * @return the cache directory.
    */
    File getCacheDirectory();
}
