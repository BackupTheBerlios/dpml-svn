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

package net.dpml.transit;

/**
 * Exception to indicate that the Artifact already exists in the cache and
 * can therefor not be written to.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArtifactAlreadyExistsException extends ArtifactException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The artifact that we tried to write to.
    */
    private final Artifact m_artifact;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Construct a new <code>ArtifactAlreadyExistsException</code> instance.
     *
     * @param message the exception message
     * @param artifact the subject artifact
     */
    public ArtifactAlreadyExistsException( final String message, final Artifact artifact )
    {
        super( message );
        m_artifact = artifact;
    }

    /**
     * Returns the Artifact that were attempted to be written to.
     * @return the subject artifact
     */
    public Artifact getArtifact()
    {
        return m_artifact;
    }
}

