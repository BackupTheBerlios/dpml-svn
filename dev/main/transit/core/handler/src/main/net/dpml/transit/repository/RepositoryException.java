/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.transit.repository;

import java.io.IOException;

/**
 * Exception to indicate that there was a repository related error.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: RepositoryException.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 */
public class RepositoryException extends IOException
{
    /**
     * The causal exception.
     */
     private final Throwable m_cause;

    /**
     * Construct a new <code>RepositoryException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public RepositoryException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>RepositoryException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */
    public RepositoryException( final String message, final Throwable cause )
    {
        super( message );
        m_cause = cause;
    }

   /**
    * Return the causal exception.
    * @return the causal exception
    */
    public Throwable getCause()
    {
        return m_cause;
    }
}


