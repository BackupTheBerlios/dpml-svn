/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.repository;

/**
 * Exception to indicate that there was a repository related error.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: RepositoryException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class RepositoryException extends Exception
{
    private Throwable m_cause;

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
    * @return the causal exception (possibly null)
    */
    public Throwable getCause()
    {
        return m_cause;
    }
}

