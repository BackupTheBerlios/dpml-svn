/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.tasks;

import java.util.ArrayList;

import net.dpml.tools.Context;

/**
 * General initialization during which context establishment is 
 * undertaken (if not already present).
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InitializationTask extends GenericTask
{
    private ArrayList m_list = new ArrayList();
    
   /**
    * Initialize type to processor mapping.  During execution the current
    * resource is consulted with respect to the types it declares it produced.
    * Each type is identified by a unique type id.  The collection of type ids
    * are used to resolve an ordered array of build processors.  The build processor
    * list is established based on the initial type ids combined with any processor 
    * dependencies declared by respective processors.  Finally, the sorted processors 
    * are invoked by the standard build listener to handle type production concerns. 
    * Targets in the project template trigger init, prepare, build, package, test and 
    * install build events which are monitored by the standard listener enabling 
    * sequentially executed phased functionality within the repective processors.
    */
    public void execute()
    {
        Context context = getContext(); // triggers context establishment
    }
}
