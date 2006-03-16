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

import net.dpml.component.ControlException;

/**
 * Exception thrown when an attempt is made to reference an unknown 
 * or unresolvable controller.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ControllerNotFoundException extends ControlException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    private final URI m_target;

   /**
    * Creation of a new <tt>ControllerNotFoundException</tt>.
    * @param uri the uri of the controller raising the exception
    * @param target the uri of the target controller
    */
    public ControllerNotFoundException( URI uri, URI target )
    {
        this( uri, target, null );
    }

   /**
    * Creation of a new <tt>ControllerNotFoundException</tt>.
    * @param uri the uri of the controller raising the exception
    * @param target the uri of the target controller
    * @param cause the causal exception
    */
    public ControllerNotFoundException( URI uri, URI target, Throwable cause )
    {
        super( uri, createMessage( uri ), cause );
        m_target = target;
    }

   /**
    * Return the uri of the target controller.
    * @return the target controller uri
    */
    public URI getTargetURI()
    {
        return m_target;
    }

    private static String createMessage( URI target )
    {
        return "Unable to locate the target controller [" + target + "].";
    }
}

