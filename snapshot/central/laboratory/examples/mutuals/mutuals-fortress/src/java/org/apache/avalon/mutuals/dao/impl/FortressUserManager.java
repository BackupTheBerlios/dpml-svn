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

package org.apache.avalon.mutuals.dao.impl;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import java.util.HashMap;
import org.apache.avalon.mutuals.model.User;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * @version $Id: FortressUserManager.java,v 1.1 2004/02/29 08:41:42 farra Exp $
 */

public class FortressUserManager extends AbstractUserManager implements Configurable, LogEnabled {

  Logger m_log;

  public FortressUserManager() {
  }

  public void configure(Configuration configuration) throws
      ConfigurationException
  {
    HashMap users = new HashMap();

    Configuration[] userConfs = configuration.getChildren("user");
    for(int i=0; i < userConfs.length; i++){
      Configuration c = userConfs[i];
      User user = new User();
      user.setId(c.getAttribute("id"));
      user.setName(c.getAttribute("name"));
      user.setPassword(c.getAttribute("password"));
      users.put(user.getId(),user);
    }

    m_users = users;

  }

  public void enableLogging(Logger logger) {
    m_log = logger;
    m_log.info("Logging enabled for FortressUserManager");
  }
}