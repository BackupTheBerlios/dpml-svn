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

import org.apache.avalon.mutuals.dao.impl.AbstractTransactionManager;
import org.apache.avalon.mutuals.model.User;
import org.apache.avalon.mutuals.model.Fund;
import org.apache.avalon.mutuals.dao.exception.DaoException;
import org.apache.avalon.mutuals.dao.PortfolioManager;
import org.apache.avalon.mutuals.dao.lifecycle.LoggingDao;
import org.apache.commons.logging.Log;

/**
 * @version $Id: PicoTransactionManager.java,v 1.1 2004/02/29 08:41:42 farra Exp $
 */

public class PicoTransactionManager
    extends AbstractTransactionManager
    implements LoggingDao
{

  protected Log m_log;

  public PicoTransactionManager(PortfolioManager manager) {
    m_portfolios = manager;
  }

  public void enableLogging(Log log) {
    m_log = log;
    m_log.info("Logging enabled for PicoTansactionManager");
  }

  public void buy(User user, Fund fund, int amount) throws DaoException {
    m_log.info("Performing buy transaction for "+user.getName()+" for "+amount+" shares of "+fund.getName());
    super.buy(user,fund,amount);
  }

  public void sell(User user, Fund fund, int amount) throws DaoException {
    m_log.info("Performing sell transaction for "+user.getName()+" for "+amount+" shares of "+fund.getName());
    super.sell(user,fund,amount);
  }

}