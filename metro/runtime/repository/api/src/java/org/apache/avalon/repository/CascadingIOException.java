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

package org.apache.avalon.repository ;

import java.io.IOException;

/**
 * Exception to indicate that there was a IO exception.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CascadingIOException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class CascadingIOException
        extends IOException
{

    private Throwable m_cause;

    /**
     * Construct a new <code>ApplianceException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public CascadingIOException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>ApplianceException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public CascadingIOException( final String message, final Throwable throwable )
    {
        super( message );
        m_cause = throwable;
    }

    /**
     * Return the cause of the exception.
     * @return the causal exception
     */
    public Throwable getCause()
    {
        return m_cause;
    }
}

