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

package org.apache.avalon.test.testc;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

/**
 * @avalon.component name="test-c" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.testc.C"
 */
public class TestC extends AbstractLogEnabled
  implements Initializable, Serviceable, C
{
    public void initialize() throws Exception
    {
        getLogger().info( "hello from C" );
    }

   /**
    * @avalon.dependency type="org.apache.avalon.test.testa.A" key="a"
    * @avalon.dependency type="org.apache.avalon.test.testa.A" key="a2"
    * @avalon.dependency type="org.apache.avalon.test.testa.B" key="b"
    */
    public void service( ServiceManager manager ) throws ServiceException
    {
        getLogger().info( "service stage" );
        Logger logger = getLogger().getChildLogger( "service" );
        logger.info( "lookup A" );
        manager.lookup( "a" );
        logger.info( "lookup A2" );
        manager.lookup( "a2" );
        logger.info( "lookup B" );
        manager.lookup( "b" );
        logger.info( "ok" );
    }
}
