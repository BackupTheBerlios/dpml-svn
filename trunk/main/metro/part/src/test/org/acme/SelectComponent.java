/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

import net.dpml.util.Logger;

import net.dpml.runtime.Component;
import net.dpml.runtime.Provider;

/**
 * Test ciomponent used to validate the sue of provider and component 
 * class references in a parts interface.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SelectComponent
{
   /**
    * Parts definition.
    */
    public interface Parts
    {
       /**
        * Get all components implemeting the Widget service.
        * @return the widget array
        */
        Widget[] getWidgets();
        
       /**
        * Get an array of providers from sub-components that are 
        * candidate providers for the supplied service type.
        * @param type the service type
        * @return the provider array
        */
        Provider[] getProviders( Class type );
        
       /**
        * Get an array of components from sub-components that are 
        * candidate providers for the supplied service type.
        * @param type the service type
        * @return the component array
        */
        Component[] getComponents( Class type );
        
       /**
        * Get all of the sub-components.
        * @return the component array
        */
        Component[] getAllComponents();
    }
    
    private final Parts m_parts;
    
   /**
    * Component consturctor.
    * @param logger the assigned logging channel
    * @param parts the container provider parts implementation
    */
    public SelectComponent( Logger logger, Parts parts )
    {
        logger.info( "instantiated" );
        m_parts = parts;
    }
    
   /**
    * Get the parts implementation (for the testcase).
    * @return the parts
    */
    public Parts getParts()
    {
        return m_parts;
    }
}
