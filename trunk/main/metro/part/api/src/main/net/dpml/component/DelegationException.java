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
 * Exception thrown by a local handler that wraps an exception thrown 
 * by a foreign handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DelegationException extends ControlException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private URI m_target;

   /**
    * Creation of a new delegation exception.
    * @param uri the uri of the controller initiating the delegation
    * @param target the uri of the target handler that raised the causal exception
    * @param message exception message
    * @param cause the causal exception
    */
    public DelegationException( URI uri, URI target, String message, Throwable cause )
    {
        super( uri, message, cause );
        m_target = target;
    }

   /**
    * Return the uri identifying the delegation target handler.
    * @return the URI of the delegate handler
    */
    public URI getDelegationTarget()
    {
        return m_target;
    }

}

