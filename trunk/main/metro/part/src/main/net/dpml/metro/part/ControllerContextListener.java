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

import java.util.EventListener;

/**
 * An interface implementation by controller concerned with or responsible 
 * for handling changes in a local runtime directory context.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ControllerContextListener extends EventListener
{
   /**
    * Notify the listener that the working directory has changed.
    *
    * @param event the change event
    */
    void workingDirectoryChanged( ControllerContextEvent event );

   /**
    * Notify the listener that the temporary directory has changed.
    *
    * @param event the change event
    */
    void tempDirectoryChanged( ControllerContextEvent event );

}
