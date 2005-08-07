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

package net.dpml.transit.link;

import java.net.URI;

import net.dpml.transit.artifact.ArtifactNotFoundException;

/**
 * Exception to indicate that a link could not be located.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: ArtifactNotFoundException.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 */
public class LinkNotFoundException extends ArtifactNotFoundException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Construct a new <code>LinkNotFoundException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param uri the link uri
     */
    public LinkNotFoundException( final String message, final URI uri )
    {
        super( message, uri );
    }
}

