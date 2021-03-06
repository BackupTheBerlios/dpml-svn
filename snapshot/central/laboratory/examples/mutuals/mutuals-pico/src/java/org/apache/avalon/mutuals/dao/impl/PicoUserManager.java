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

import org.apache.avalon.mutuals.dao.impl.AbstractUserManager;
import org.apache.avalon.mutuals.model.User;
import org.apache.avalon.mutuals.dao.exception.DaoException;
import java.util.Map;
import org.apache.avalon.mutuals.dao.lifecycle.LoggingDao;
import org.apache.commons.logging.Log;

/**
 * @version $Id: PicoUserManager.java,v 1.1 2004/02/29 08:41:42 farra Exp $
 */

public class PicoUserManager
    extends AbstractUserManager
    implements LoggingDao
{

  protected Log m_log;

  public PicoUserManager(Map users) {
    m_users = users;
  }

  public void enableLogging(Log log) {
    m_log = log;
    m_log.info("Logging Enabled for PicoUserManager");
  }
}