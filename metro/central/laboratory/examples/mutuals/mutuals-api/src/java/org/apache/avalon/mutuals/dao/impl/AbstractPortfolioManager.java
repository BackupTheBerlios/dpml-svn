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

import org.apache.avalon.mutuals.dao.PortfolioManager;
import org.apache.avalon.mutuals.model.User;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.avalon.mutuals.dao.exception.DaoException;
import org.apache.avalon.mutuals.model.Fund;
import java.util.ArrayList;

/**
 * @version $Id: AbstractPortfolioManager.java,v 1.1 2004/02/29 08:41:41 farra Exp $
 */

public abstract class AbstractPortfolioManager implements PortfolioManager {

  protected Map m_balances;
  protected Map m_portfolios;

  public double getAccountBalance(User user) {
    double balance = 0.0;
    if( m_balances.containsKey(user.getId()))
      balance = ((Double) m_balances.get(user.getId())).doubleValue();
    return balance;
  }

  public void adjustAccountBalance(User user, double amount) {
    double balance = getAccountBalance(user);
    balance += amount;
    m_balances.put(user.getId(),new Double(balance));
  }

  public List getPortfolio(User user) throws DaoException {
    ArrayList funds = new ArrayList();
    if(m_portfolios.containsKey(user.getId()))
      funds = (ArrayList) m_portfolios.get(user.getId());
    return funds;
  }

  public void adjustPortfolio(User user, Fund fund, int amount) throws DaoException {
    List funds = getPortfolio(user);
    int index = Collections.binarySearch(funds,fund);
    if(index >= 0){
      Fund userFund = (Fund) funds.get(index);
      int userAmount = userFund.getQty();
      userAmount += amount;
      if(userAmount < 0)
        throw new DaoException("Insufficient funds");
      else{
        double totalPrice = amount * userFund.getPrice();
        adjustAccountBalance(user, totalPrice);
        if(userAmount == 0)
          funds.remove(index);
        else{
          userFund.setQty(userAmount);
          funds.set(index,userFund);
        }
        m_portfolios.put(user.getId(),funds);
      }
    }
  }

}