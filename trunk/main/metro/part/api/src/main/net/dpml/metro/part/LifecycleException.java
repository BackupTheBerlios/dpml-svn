/* 
 * Copyright 2004 Stephen J. McConnell.
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
 * Exception to indicate that there was a lifecycle related error.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LifecycleException extends ControlException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Construct a new <code>LifecycleException</code> instance.
    *
    * @param controller the uri identifying the controller
    * @param message the exception message
    */
    public LifecycleException( final URI controller, final String message )
    {
        this( controller, message, null );
    }

   /**
    * Construct a new <code>LifecycleException</code> instance.
    *
    * @param controller the uri identifying the controller
    * @param message the exception message
    * @param throwable the root cause of the exception
    */
    public LifecycleException( 
      final URI controller, final String message, final Throwable throwable )
    {
        super( controller, message, throwable );
    }
}

