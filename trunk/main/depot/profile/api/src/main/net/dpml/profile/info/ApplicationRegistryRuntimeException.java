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

package net.dpml.station.info;

/**
 * Exception indicating that an internal error occured while loading the 
 * application registry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ApplicationRegistryRuntimeException extends RuntimeException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * Construct a new <code>ProfileException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public ApplicationRegistryRuntimeException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>ApplicationRegistryRuntimeException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param cause the root cause of the exception
     */
    public ApplicationRegistryRuntimeException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}

