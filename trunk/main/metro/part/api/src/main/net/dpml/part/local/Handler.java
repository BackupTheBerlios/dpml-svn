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

package net.dpml.part.local;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import net.dpml.part.ControlException;

import net.dpml.part.remote.Component;
import net.dpml.part.remote.Provider;

/**
 * Local interface through which a component implementation may 
 * interact with subsidary parts.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Handler
{
   /**
    * Return a mutible context map.
    *
    * @return the context map
    */
    Map getContextMap();
    
   /**
    * Return a reference to a managed provider.
    * @return the service provider
    * @exception InvocationTargetException if the component instantiation process 
    *  is on demand and an target invocation error occurs
    * @exception ControlException if the component could not be established due to a controller 
    *  related error
    */
    Provider getProvider() throws ControlException, InvocationTargetException;
}

