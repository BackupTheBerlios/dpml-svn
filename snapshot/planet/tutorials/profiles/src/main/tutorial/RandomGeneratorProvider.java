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

import java.util.Random;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * An implementation of a random number generator.
 * 
 * @avalon.component name="randomizer" lifestyle="singleton"
 * @avalon.service type="tutorial.RandomGenerator"
 */
public class RandomGeneratorProvider extends AbstractLogEnabled 
  implements Configurable, RandomGenerator
{

    private Random m_random = null;

   /**
    * Configuration of the component by the container.  The 
    * implementation get a child element named 'source' and 
    * assigns the value of the element to a local variable.
    *
    * @param config the component configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public void configure( Configuration config ) throws ConfigurationException
    {
        getLogger().info( "configuration stage" );
        long seed = config.getChild( "seed" ).getValueAsLong( 0 );
        getLogger().info( "seed: " + seed );
        m_random = new Random( System.currentTimeMillis() * seed );
    }

   /**
    * Return a random integer
    * @return the random number
    */
    public int getRandom()
    {
        return m_random.nextInt();
    }
}
