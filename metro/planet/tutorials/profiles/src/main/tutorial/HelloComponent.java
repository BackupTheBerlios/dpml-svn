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
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

/**
 * A demonstration component that is dependent on a random number 
 * service that will be located and constructed using a packaged profile.
 *
 * @avalon.component name="hello" lifestyle="singleton"
 */
public class HelloComponent extends AbstractLogEnabled 
  implements Serviceable
{

   /**
    * Servicing of the component by the container during 
    * which service dependencies declared under the component
    * can be resolved using the supplied service manager.
    *
    * @avalon.dependency type="tutorial.RandomGenerator" key="random"
    */
    public void service( ServiceManager manager )
      throws ServiceException
    {
        RandomGenerator random = (RandomGenerator) manager.lookup( "random" );
        getLogger().info( "resolved random: " + random.getRandom() );
    }
}
