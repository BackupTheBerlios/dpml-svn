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

package org.apache.avalon.test.testcyclic;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * @avalon.component name="cyclic-b" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.testcyclic.B"
 */
public class TestCyclicB extends AbstractLogEnabled
  implements B
{
   /**
    * @avalon.dependency key="CyclicA" type="org.apache.avalon.test.testcyclic.A"
    */
    public void service( ServiceManager manager ) throws ServiceException
    {
        getLogger().info( "service stage" );
        Logger logger = getLogger().getChildLogger( "service" );
        logger.info( "lookup A" );
        manager.lookup( "CyclicA" );
        logger.info( "ok" );
    }
}
