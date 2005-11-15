/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.state;

import java.net.URI;

/**
 * Interface describing a potential operation invocation exposed by 
 * an active state.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Operation extends Action
{  
   /**
    * <p>Return the uri describing the execution criteria.  Recognized uri 
    * schemes include:</p>
    * 
    * <ol>
    *  <li>method:[method-name]
    * </ol>
    *
    * @return the handler uri
    */
    URI getHandlerURI();
}
