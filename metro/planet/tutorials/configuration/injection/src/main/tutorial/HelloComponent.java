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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * A configurable component using constructor based injection of 
 * a configuration.
 *
 * @avalon.component version="1.0" name="simple" lifestyle="singleton"
 */
public class HelloComponent
{
   /**
    * Configuration of the component by the container.  The 
    * implementation get a child element named 'source' and 
    * assigns the value of the element to a local variable.
    *
    * @param config the component configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public HelloComponent( 
      final Logger logger, final Configuration config ) 
      throws ConfigurationException
    {
        logger.info( "instantiation" );
        final String source = 
          config.getChild( "source" ).getValue( "unknown" );
        final String message = "source: " + source;
        logger.info( message );
    }
}
