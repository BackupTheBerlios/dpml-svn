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


package org.apache.avalon.examples.simple.impl;

import org.apache.avalon.examples.simple.Simple;
import org.apache.avalon.examples.simple.SimpleClient;

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * a Simple service implementation that implements the basic lifecycles.
 * @avalon.component name="client" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.examples.simple.SimpleClient"
 */
public class SimpleClientImpl
    extends AbstractLogEnabled
    implements SimpleClient, Serviceable, Executable
{

  public Simple m_simple = null;

  public SimpleClientImpl() {
  }

  public String getSimpleMessage() {
    return m_simple.getName();
  }

  /**
   * @avalon.dependency type="Simple" key="simple"
   */
  public void service(ServiceManager serviceManager) throws ServiceException{
    m_simple = (Simple) serviceManager.lookup("simple");
  }

  public void execute() {
    if(getLogger().isInfoEnabled())
      getLogger().info("executing client:  simple message = "+getSimpleMessage());
  }

}