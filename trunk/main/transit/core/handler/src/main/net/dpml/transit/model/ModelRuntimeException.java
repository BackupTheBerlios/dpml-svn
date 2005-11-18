/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.model;

import net.dpml.transit.TransitRuntimeException;

/**
 * Exception to indicate that there was a configuration model related error.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModelRuntimeException extends TransitRuntimeException
{
    /**
     * Construct a new <code>ModelRuntimeException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public ModelRuntimeException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>ModelRuntimeException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */
    public ModelRuntimeException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}

