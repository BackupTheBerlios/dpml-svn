/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.transit.artifact;

import java.net.URI;

/**
 * Exception to indicate that ther Artifact could not be located.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ArtifactNotFoundException.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 */
public class ArtifactNotFoundException extends ArtifactException
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The artifact that we not found.
    */
    private final URI m_artifact;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Construct a new <code>ArtifactException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param artifact the subject artifact
     */
    public ArtifactNotFoundException( final String message, final URI artifact )
    {
        super( message );
        m_artifact = artifact;
    }

    /**
     * Returns the uri that could not be found.
     * @return the subject uri
     */
    public URI getURI()
    {
        return m_artifact;
    }
}

