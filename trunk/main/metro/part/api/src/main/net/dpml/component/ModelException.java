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

package net.dpml.component;

import java.net.URI;

/**
 * General exception thrown by a context model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModelException extends ControlException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Creation of a new <tt>ModelException</tt>.
    * @param uri the controller uri
    * @param message the exception message
    */
    public ModelException( URI uri, String message )
    {
        super( uri, message );
    }

   /**
    * Creation of a new <tt>ModelException</tt>.
    * @param uri the controller uri
    * @param message the exception message
    * @param cause the causal exception
    */
    public ModelException( URI uri, String message, Throwable cause )
    {
        super( uri, message, cause );
    }
}

