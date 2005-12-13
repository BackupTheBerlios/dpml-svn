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

package net.dpml.metro.part;

import java.net.URI;

/**
 * Exception thrown in response to a request for an unknown component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentNotFoundException extends ControlException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Creation of a new <tt>ComponentNotFoundException</tt>.
    * @param uri the controller uri
    * @param key the requested component key
    */
    public ComponentNotFoundException( URI uri, String key )
    {
        super( uri, key );
    }

   /**
    * Return the requested key.
    * @return the key
    */
    public String getKey()
    {
        return getMessage();
    }
}

