/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.runtime;

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.metro.part.ControlException;

/**
 * Exception indicating an controller related error.  A controller exception
 * delcares the URI of the controller form which the exception was initiated.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ControllerException extends ControlException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Creation of a new ControllerRuntimeException.
    *
    * @param message the description of the exception 
    */
    public ControllerException( String message )
    {
        this( message, null );
    }

   /**
    * Creation of a new ControllerRuntimeException.
    *
    * @param message the description of the exception 
    * @param cause the causal exception
    */
    public ControllerException( String message, Throwable cause )
    {
        super( CONTROLLER_URI, message, cause );
    }

    private static final URI CONTROLLER_URI = createControllerURI();
    
    private static URI createControllerURI()
    {
        try
        {
            return new URI( "@PART-HANDLER-URI@" );
        }
        catch( URISyntaxException e )
        {
            return null;
        }
    }
}

