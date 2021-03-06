/*
 * Copyright 2005 Stephen McConnell
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

package org.acme;

import net.dpml.annotation.Context;

/**
 * Sample context iterface.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Context
public interface HelloConfiguration
{
   /**
    * Get the message.
    * @param value the default value
    * @return the message
    */
    String getMessage( String value );
    
   /**
    * Get the port.
    * @param port the default port
    * @return the resolved port
    */
    int getPort( int port );
    
   /**
    * Get the nested context solution.
    * @return the nested context solution
    */
    AnotherContext getTarget();
}
