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

package org.apache.avalon.test.components;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * This is a component that can be tested relative a set of 
 * assigned permissions.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="anothertest" lifestyle="singleton"
 * @avalon.service 
 *    type="org.apache.avalon.test.components.AnotherTestService"
 */
public class AnotherTestComponent extends AbstractLogEnabled 
  implements AnotherTestService, Serviceable
{
    private TestService m_TestService;
    
    /**
     * Service from the container.
     * 
     * @avalon.dependency 
     *    type="org.apache.avalon.test.components.TestService" 
     *    key="TestService"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        m_TestService = (TestService) man.lookup( "TestService" ); 
    }
    
    public String getJavaVersion()
    {
        return m_TestService.getJavaVersion();
    }

    public void setJavaVersion( String newVersion )
    {
        m_TestService.setJavaVersion( newVersion );
    }
}
