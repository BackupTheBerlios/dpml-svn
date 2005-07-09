/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004-2005 Niclas Hedhman.
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

import java.net.ProtocolException;

/**
 * Exception to indicate that there was a transit artifact related error.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: TransitException.java 2790 2005-06-08 13:03:50Z mcconnell@dpml.net $
 */
public class TransitException extends ProtocolException
{
    /**
     * Construct a new <code>TransitException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public TransitException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>TransitException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */
    public TransitException( final String message, final Throwable cause )
    {
        super( message );
        if( null != cause )
        {
            initCause( cause );
        }
    }
}

