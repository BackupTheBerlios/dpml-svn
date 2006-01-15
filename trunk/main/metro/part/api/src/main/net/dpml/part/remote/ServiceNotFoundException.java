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

package net.dpml.part.remote;

import java.net.URI;

import net.dpml.part.ControlException;

/**
 * Exception thrown by a handler in response to a request for an unknown service.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServiceNotFoundException extends ControlException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Creation of a new <tt>ServiceNotFoundException</tt>.
    * @param uri the controller uri
    * @param classname the service classname
    */
    public ServiceNotFoundException( URI uri, String classname )
    {
        super( uri, classname );
    }
}

