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

import net.dpml.transit.Artifact;

/** 
 * A Layout abstracts the decoding process of the location
 * of artifacts in various filesystems.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Layout
{
    /**
     * Return the base path for an artifact.  The base path is the location
     * where the file will be found. The base + "/" filename is equal to the
     * full path.
     *
     * @param artifact the Artifact to resolve.
     * @return the base path
     */
    String resolveBase( Artifact artifact );

    /**
     * Returns the full path of the artifact relative to a logical root directory.
     * The base + "/" filename is equal to the full path.
     *
     * @see #resolveBase
     * @see #resolveFilename
     * @param artifact the Artifact to resolve.
     * @return the logical artifact path
     */
    String resolvePath( Artifact artifact );

    /**
     * Return the filename for an artifact.  The base + "/" filename is equal
     * to the full path.
     *
     * @see #resolveBase
     * @see #resolveFilename
     * @param artifact the Artifact to resolve.
     * @return the logical artifact path
     */
    String resolveFilename( Artifact artifact );
}
