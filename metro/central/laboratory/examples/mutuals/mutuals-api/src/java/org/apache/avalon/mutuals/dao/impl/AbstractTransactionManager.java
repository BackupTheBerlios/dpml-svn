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

import org.apache.avalon.mutuals.dao.TransactionManager;
import org.apache.avalon.mutuals.model.User;
import org.apache.avalon.mutuals.dao.exception.DaoException;
import java.util.List;
import org.apache.avalon.mutuals.model.Fund;
import org.apache.avalon.mutuals.dao.PortfolioManager;

/**
 * @version $Id: AbstractTransactionManager.java,v 1.1 2004/02/29 08:41:41 farra Exp $
 */

public abstract class AbstractTransactionManager implements TransactionManager {

  protected PortfolioManager m_portfolios;

  public void sell(User user, Fund fund, int amount) throws DaoException {
    m_portfolios.adjustPortfolio(user,fund,0-amount);
  }

  public void buy(User user, Fund fund, int amount) throws DaoException {
    m_portfolios.adjustPortfolio(user,fund,amount);
  }
}