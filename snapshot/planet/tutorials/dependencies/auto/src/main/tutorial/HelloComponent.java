/* 
 * Copyright 2004 Apache Software Foundation
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
package tutorial;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

/**
 * The HelloComponent is dependent on a RandomGenerator service.
 * @avalon.component version="1.0" name="simple" lifestyle="singleton"
 */
public class HelloComponent extends AbstractLogEnabled 
  implements Initializable, Serviceable, Disposable
{
    RandomGenerator m_random = null;
    Identifiable m_identifiable = null;

   /**
    * Servicing of the component by the container during 
    * which service dependencies declared under the component
    * can be resolved using the supplied service manager.
    *
    * @param manager the service manager
    * @avalon.dependency type="tutorial.RandomGenerator:1.0"
    *    key="random"
    * @avalon.dependency type="tutorial.Identifiable"
    */
    public void service( ServiceManager manager )
      throws ServiceException
    {
        m_random = (RandomGenerator) manager.lookup( "random" );
        m_identifiable = 
          (Identifiable) manager.lookup( Identifiable.class.getName() );
    }

    public void initialize()
    {
        getLogger().info( "initialization" );
        getLogger().info( "received random value: " + m_random.getRandom() );
        getLogger().info( "using identifiable: " + m_identifiable.getIdentity() );
    }

    public void dispose()
    {
        getLogger().info( "disposal" );
    }

}
