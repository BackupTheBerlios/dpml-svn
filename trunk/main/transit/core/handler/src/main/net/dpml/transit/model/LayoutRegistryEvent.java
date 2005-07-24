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

package net.dpml.transit.model;

import java.util.EventObject;

/**
 * An event issued by a layout registry signalling addition or removal
 * of a layout model.
 */
public abstract class LayoutRegistryEvent extends EventObject
{
    private final LayoutModel m_resolver;
    private final LayoutRegistryModel m_system;

   /**
    * Creation of a new LayoutRegistryEvent signalling the addition
    * or removal of a layout model.
    * 
    * @param system the layout model
    * @param resolver the layout resolver model that was added or removed
    */   
    public LayoutRegistryEvent( LayoutRegistryModel system, LayoutModel resolver )
    {
        super( system );
        m_system = system;
        m_resolver = resolver;
    }
    
   /**
    * Return the model initiating the event.
    * @return the layout model
    */
    public LayoutRegistryModel getLayoutRegistryModel()
    {
        return m_system;
    }

   /**
    * Return the resolver model that was added or removed.
    * @return the resolver model
    */
    public LayoutModel getLayoutModel()
    {
        return m_resolver;
    }
}

