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

package org.apache.avalon.composition.model;

/**
 * Exception to indicate that a service defintioon is unknown within
 * the scope of a service manager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceUnknownException.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ServiceUnknownException
        extends ServiceException
{

    /**
     * Construct a new <code>ServiceUnknownException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public ServiceUnknownException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>ServiceUnknownException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public ServiceUnknownException( final String message, final Throwable throwable )
    {
        super( message, throwable );
    }
}

