/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tutorial;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * A configurable component.
 *
 * @avalon.component version="1.0" name="simple"
 */
public class HelloComponent extends AbstractLogEnabled 
    implements Configurable, Initializable
{
    private String m_source = "undefined";

   /**
    * Configuration of the component by the container.  The 
    * implementation get a child element named 'source' and 
    * assigns the value of the element to a local variable.
    *
    * @param config the component configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public void configure( Configuration config ) 
        throws ConfigurationException
    {
        getLogger().info( "configuration stage" );
        m_source = config.getChild( "source" ).getValue( "unknown" );
    }

   /**
    * Initialization of the component by the container.
    * @exception Exception if an initialization error occurs
    */
    public void initialize() 
        throws Exception
    {
        getLogger().info( "initialization stage" );
        final String message = 
          "source: " + m_source;
        getLogger().info( message );
    }
}
