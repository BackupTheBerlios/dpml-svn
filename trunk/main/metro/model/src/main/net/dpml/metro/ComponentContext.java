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

package net.dpml.metro;

import java.net.URI;
import java.rmi.RemoteException;

import net.dpml.part.Controller;

/**
 * The ComponentContext interface may be referenced by a component 
 * context inner interface to expose container related services to 
 * a component implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentContext
{
   /**
    * Return the current controller.
    * @return the root system controller
    */
    Controller getController() throws RemoteException;
    
   /**
    * Create a nested component handler.
    * @param anchor the anchor classloader
    * @param uri the component part definition
    * @return the component handler
    * @exception Exception if an error occurs during component loading or establishment
    */
    ComponentHandler createComponentHandler( ClassLoader anchor, URI uri ) throws Exception;
}

