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

package net.dpml.part;

import java.net.URI;

/**
 * Runtime exception thrown by a control.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ControlRuntimeException extends RuntimeException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    private final URI m_uri;

   /**
    * Creation of a new <tt>ControlRuntimeException</tt>.
    * @param uri the part controller uri
    * @param message the exception message
    */
    public ControlRuntimeException( URI uri, String message )
    {
        this( uri, message, null );
    }

   /**
    * Creation of a new <tt>ControlRuntimeException</tt>.
    * @param uri the part controller uri
    * @param message the exception message
    * @param cause the causal exception
    */
    public ControlRuntimeException( URI uri, String message, Throwable cause )
    {
        super( message, cause );
        m_uri = uri;
    }
    
   /**
    * Return the controller uri.
    * @return the uri identifying the controller that raised the exception
    */
    public URI getControllerURI()
    {
        return m_uri;
    }

}

