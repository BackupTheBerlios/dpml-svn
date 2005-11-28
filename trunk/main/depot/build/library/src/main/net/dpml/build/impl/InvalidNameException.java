/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.build.impl;

/**
 * Internal exception throw to indicate a bad name reference.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class InvalidNameException extends RuntimeException
{
   /**
    * Creation of a new InvalidNameException.
    * @param message the exception message
    */
    InvalidNameException( String message )
    {
        this( message, null );
    }

   /**
    * Creation of a new InvalidNameException.
    * @param message the exception message
    * @param cause the causal exception
    */
    InvalidNameException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
