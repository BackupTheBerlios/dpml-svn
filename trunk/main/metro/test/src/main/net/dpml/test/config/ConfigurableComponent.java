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

package net.dpml.test.config;

import net.dpml.logging.Logger;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

/**
 * Test component used to validate configuration directives.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class ConfigurableComponent
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;
    
   /**
    * Supplied XML configuration.
    */
    private final Configuration m_config;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new startable component instance.
    * @param logger the assigned logging channel
    * @param config the assigned configuration
    */
    public ConfigurableComponent( final Logger logger, Configuration config )
    {
        m_logger = logger;
        m_config = config;
    }
    
    //------------------------------------------------------------------
    // test operations
    //------------------------------------------------------------------
    
   /**
    * Return the name resolved from the configuration.
    * @return the name value
    * @exception ConfigurationException if a configuration error occurs
    */
    public String getName() throws ConfigurationException
    {
        return m_config.getAttribute( "name" );
    }
    
   /**
    * Return the number of item entries in the configuration.
    * @return the item count
    */
    public int getItemCount()
    {
        return m_config.getChildren( "item" ).length;
    }
    
}
