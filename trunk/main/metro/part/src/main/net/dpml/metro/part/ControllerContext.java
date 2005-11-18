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
import java.io.File;

import net.dpml.transit.Logger;

/**
 * The ControllerContext declares the runtime context that a controller
 * is established within. Controller implementations will typically receive   
 * be bsupplied with a context object as a constructor argument.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ControllerContext
{
   /**
    * Return the assigned logging channel.
    *
    * @return the logging channel
    */
    Logger getLogger();

   /**
    * Return the root working directory.
    *
    * @return directory representing the root of the working directory hierachy
    */
    File getWorkingDirectory();

   /**
    * Return the root temporary directory.
    *
    * @return directory representing the root of the temporary directory hierachy.
    */
    File getTempDirectory();

   /**
    * Add the supplied controller context listener to the controller context.  A 
    * controller implementation should not maintain strong references to supplied 
    * listeners.
    *
    * @param listener the controller context listener to add
    */
    void addControllerContextListener( ControllerContextListener listener );

   /**
    * Remove the supplied controller context listener from the controller context.
    *
    * @param listener the controller context listener to remove
    */
    void removeControllerContextListener( ControllerContextListener listener );

}
