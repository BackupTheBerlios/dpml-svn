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
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * a Simple service implementation that implements the basic lifecycles.
 * @avalon.component name="config-example" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.examples.simple.Simple"
 */
public class SimpleConfigurationImpl
    extends AbstractLogEnabled
    implements Simple, Configurable
{

  protected String m_name = "Default Name";

  public String getName() {
    return m_name;
  }

  public void configure(Configuration configuration)
      throws ConfigurationException
  {
    Configuration child = configuration.getChild("name");
    m_name = child.getValue("Another Default Value");

    if(getLogger().isInfoEnabled())
      getLogger().info("configuring simple component with name = "+getName());
  }

}