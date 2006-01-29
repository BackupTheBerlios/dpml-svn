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

import java.util.Map;

import net.dpml.part.ComponentOperations;

/**
 * Local interface through which a component implementation may 
 * interact with the assigned model, subsidary parts, and context.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface ComponentHandler extends ComponentOperations, ComponentContext
{
   /**
    * Return a mutable context map.  The map may be used by component
    * implementations to override context entries in the associated 
    * component instance.
    *
    * @return the context map
    */
    Map getContextMap();
    
   /**
    * Return the manager for the assigned component model.
    * @return the component model manager
    */
    ComponentManager getComponentManager();
    
   /**
    * Return the internal parts manager.
    * @return the parts manager
    */
    PartsManager getPartsManager();
}
