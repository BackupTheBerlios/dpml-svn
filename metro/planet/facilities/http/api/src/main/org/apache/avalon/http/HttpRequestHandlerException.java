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

package org.apache.avalon.http;

/**
 * Exception to indicate that there was a http handler related error.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/04/04 15:00:56 $
 */
public class HttpRequestHandlerException extends java.io.IOException
{

     private final Throwable m_cause;

    /**
     * Construct a new <code>HandlerException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public HttpRequestHandlerException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>HandlerException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */
    public HttpRequestHandlerException( final String message, final Throwable cause )
    {
        super( message );
        m_cause = cause;
    }

   /**
    * Return the causal exception.
    * @return the cause
    */
    public Throwable getCause()
    {
        return m_cause;
    }
}

